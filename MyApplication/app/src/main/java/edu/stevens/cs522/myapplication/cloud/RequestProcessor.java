package edu.stevens.cs522.myapplication.cloud;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import edu.stevens.cs522.myapplication.MainActivity;
import edu.stevens.cs522.myapplication.R;
import edu.stevens.cs522.myapplication.cloud.Request.PostMessage;
import edu.stevens.cs522.myapplication.cloud.Request.Register;
import edu.stevens.cs522.myapplication.cloud.Request.Unregister;
import edu.stevens.cs522.myapplication.contracts.ChatRoomContract;
import edu.stevens.cs522.myapplication.contracts.MessageContract;
import edu.stevens.cs522.myapplication.contracts.PeerContract;
import edu.stevens.cs522.myapplication.entity.ChatRoom;
import edu.stevens.cs522.myapplication.entity.Peer;
import edu.stevens.cs522.myapplication.entity.iMessage;

/**
 * Created by Sandeep on 3/25/2015.
 */
public class RequestProcessor {

    public static final String REGISTER_BROADCAST = "edu.stevens.cs522.MyApplication.register";

    public void perform(Request.Register request, Context context) throws Exception {
        RestMethod rest = new RestMethod();
        Response response = rest.perform(request);
        addPeer(request, response, context);
        //broadcast success/ unsuccess
        Intent intent = new Intent(REGISTER_BROADCAST);
        intent.putExtra(MainActivity.RegId, response.getId());
        intent.putExtra(MainActivity.code,response.getResponseCode());
        context.sendBroadcast(intent);
    }

    public void perform(PostMessage request, Context context) throws Exception {
        RestMethod rest = new RestMethod();
        StreamingResponse streaming = rest.perform(request);

        JsonWriter jw = new JsonWriter(new BufferedWriter(
                new OutputStreamWriter(streaming.connection.getOutputStream(), "UTF-8")));

        ArrayList<iMessage> messages = getMessages(request, context);

        writeMessages(jw, messages, request.chatRoom);

        JsonReader rd = new JsonReader(new BufferedReader(
                new InputStreamReader(streaming.connection.getInputStream(), "UTF-8")));

        int responseCode = streaming.connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {

            // delete table contents where seq = 0;
            //delete all messages
            //otherwise we can check for timestamp + seq id
            clearMessagesTable(context);

            // Create a messages list of the response.
            ArrayList<String> client = new ArrayList<String>();
            String chatRoom = null, text = null, sender = null;
            Long timeStamp = null, seqNum = null;
            double latitude = 0, longitude = 0;
            ArrayList<iMessage> messagesList = new ArrayList<iMessage>();
            Long id = 0L;
            try {
                rd.beginObject();
                String label = rd.nextName();
                rd.beginArray();

                while (rd.hasNext()) {
                    rd.beginObject();
                    if(rd.nextName().equals("sender")) {
                        String name = rd.nextString();
                        if(!name.equals(request.client))
                            client.add(name);
                        //latitude
                        rd.nextName();
                        rd.nextString();
                        //longitude
                        rd.nextName();
                        rd.nextString();
                    }
                    rd.endObject();
                }
                rd.endArray();
                //save peers in db and get peer_fk
                List<clientId> cids = addPeers(client, context);
                label = rd.nextName();
                iMessage message = null;
                rd.beginArray();
                while (rd.hasNext()) {
                    rd.beginObject();
                    while (rd.hasNext()) {
                        String name = rd.nextName();
                        if ("chatroom".equals(name)) {
                            chatRoom = rd.nextString();
                        } else if ("timestamp".equals(name)) {
                            timeStamp = rd.nextLong();
                        } else if ("X-latitude".equals(name)) {
                            latitude = rd.nextDouble();
                        } else if ("X-longitude".equals(name)) {
                            longitude = rd.nextDouble();
                        } else if ("seqnum".equals(name)) {
                            seqNum = rd.nextLong();
                        } else if ("text".equals(name)) {
                            text = rd.nextString();
                        } else if ("sender".equals(name)) {
                            sender = rd.nextString();
                            for(int i = 0; i < cids.size(); i++){
                                if(cids.get(i).getName().equals(sender)){
                                    id = cids.get(i).getId();
                                }
                            }
                        }
                    }
                    //if (id!=0) {
                        ChatRoom room = new ChatRoom(0L, chatRoom);
                        Long roomId = room.add(context);
                        message = new iMessage(text, sender, roomId, timeStamp, seqNum, id, latitude, longitude);
                        messagesList.add(message);
                    //}
                    rd.endObject();
                }
                rd.endArray();

                //rd.endObject();
                addMessage(messagesList, context);

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                streaming.connection.disconnect();
            }
        }

    }

    private void clearMessagesTable(Context context) {

        String selection = MessageContract.SEQ + "=?";
       // context.getContentResolver().delete(MessageContract.CONTENT_URI, selection, new String[] {"0"});
        //remove all as server pushes all messages every single time
        context.getContentResolver().delete(MessageContract.CONTENT_URI, null, null);
    }

    public void perform(Unregister request,Context context) {
        RestMethod rest = new RestMethod();
        try {
            rest.perform(request);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private List<clientId> addPeers(List<String> clients, Context context){
        List<clientId> cids = new ArrayList<clientId>();
        List<Peer> peers = new ArrayList<Peer>();
        try {
            //check if the peer name is already in the database or not
            String[] clientsArr = clients.toArray(new String[clients.size()]);
//            context.getContentResolver().notify();
            /*Cursor c = context.getContentResolver().query(PeerContract.CONTENT_URI,
                    new String[] { PeerContract.ID },
                    PeerContract.NAME + "=?", new String[] { request.client }, null);*/

            Cursor c = context.getContentResolver().query(PeerContract.CONTENT_URI,
                    new String[] { PeerContract.ID, PeerContract.NAME },
                    PeerContract.NAME + "=?", clientsArr, null);

            ContentValues values = new ContentValues();
            if (c.moveToFirst()) {
                do {
                    String name = PeerContract.getName(c);
                    if (clients.contains(name)) {
                        clients.remove(name);
                        cids.add(new clientId(name, PeerContract.getId(c)));
                    }
                } while (c.moveToNext());
            }

                //save remaining in the db and capture ids

                for(int i = 0; i < clients.size(); i++) {
                    values.put(PeerContract.NAME, clients.get(i));
                    //values.put(PeerContract.ID, response.getId());
                    //values.put(PeerContract.CLIENTID, response.getId());
                    //values.put(PeerContract.REGID, 0L);
                    values.put(PeerContract.ADDRESS, context.getString(R.string.destination_host_default));
                    values.put(PeerContract.PORT, context.getString(R.string.destination_port_default));
                    //values.put(PeerContract.LONGITUDE,);
                    //values.put(PeerContract.LATITUDE, );
                    context.getContentResolver().insert(PeerContract.CONTENT_URI, values);
                }

                String[] clientsArr2 = clients.toArray(new String[clients.size()]);
                c = context.getContentResolver().query(
                        PeerContract.CONTENT_URI, new String[] { PeerContract.NAME, PeerContract.ID }, PeerContract.NAME + "=?", clientsArr2, null);
                if(c.moveToFirst()){
                    do {
                      cids.add(new clientId(PeerContract.getName(c),PeerContract.getId(c)));
                    } while (c.moveToNext());
                }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return cids;
    }

    private void addPeer(Register request, Response response, Context context) {
        try {
            //check if the peer name is already in the database or not
            Cursor c = context.getContentResolver().query(PeerContract.CONTENT_URI,
                    new String[] { PeerContract.ID,  PeerContract.NAME},
                    PeerContract.NAME + "=?", new String[] { request.client }, null);

            ContentValues values = new ContentValues();
            //if(response.getId()!=null){
                if (c.getCount() > 0) {
                    values.put(PeerContract.REGID, request.registrationID);
                    values.put(PeerContract.CLIENTID, response.getId());
                    values.put(PeerContract.NAME, request.client);
                    values.put(PeerContract.ADDRESS, context.getString(R.string.destination_host_default));
                    values.put(PeerContract.PORT, context.getString(R.string.destination_port_default));
                    values.put(PeerContract.LATITUDE, request.latitude);
                    values.put(PeerContract.LONGITUDE, request.longitude);
                    context.getContentResolver().update(PeerContract.CONTENT_URI, values,
                               PeerContract.NAME + "=?", new String[] { request.client });
                } else {
                    values.put(PeerContract.NAME, request.client);
                    values.put(PeerContract.CLIENTID, response.getId());
                    values.put(PeerContract.REGID, request.registrationID);
                    values.put(PeerContract.ADDRESS, context.getString(R.string.destination_host_default));
                    values.put(PeerContract.PORT, context.getString(R.string.destination_port_default));
                    values.put(PeerContract.LATITUDE, request.latitude);
                    values.put(PeerContract.LONGITUDE, request.longitude);
                    context.getContentResolver().insert(PeerContract.CONTENT_URI, values);
                }
            //}

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList<iMessage> getMessages(PostMessage request, Context context) {
        Long roomfk = null;

        //get chat room id and filter messages.. may use view created later
        Cursor cursor = context.getContentResolver().query(ChatRoomContract.CONTENT_URI, null,
                ChatRoomContract.NAME + "=?", new String[] { request.chatRoom }, null);
        if (cursor.moveToFirst()) {
            roomfk = ChatRoomContract.getId(cursor);
        }
        String[] projection = new String[] { MessageContract.SENDER, MessageContract.ID,
                MessageContract.MESSAGE_TEXT, MessageContract.CHATROOM, MessageContract.TIMESTAMP,
                MessageContract.SEQ, MessageContract.LATITUDE, MessageContract.LONGITUDE };

        Cursor c = context.getContentResolver().query(
                MessageContract.CONTENT_URI,
                projection,
                MessageContract.SENDER + "=? and " + MessageContract.SEQ
                        + "= 0 and " + MessageContract.CHATROOM + " = " +   roomfk,
                new String[] { request.client }, null);
        ArrayList<iMessage> messages = new ArrayList<iMessage>();
        if (c.moveToFirst()) {
            for (int i = 0; i < c.getCount(); i++) {
                messages.add(new iMessage(c));
                c.moveToNext();
            }
        }
        return messages;
    }

    private void addMessage( ArrayList<iMessage> messagesList, Context context) throws Exception {
        for (int i = 0; i < messagesList.size(); i++) {
            String peerId = getPeerId(context, messagesList.get(i).sender);
            ContentValues values = new ContentValues();
            values.put(MessageContract.MESSAGE_TEXT, messagesList.get(i).message);
            values.put(MessageContract.SENDER, messagesList.get(i).sender);
            values.put(MessageContract.SEQ, messagesList.get(i).seq);
            values.put(MessageContract.CHATROOM, messagesList.get(i).chatRoom);
            values.put(MessageContract.TIMESTAMP, messagesList.get(i).timeStamp);
            values.put(MessageContract.PEER_FK, peerId);
            values.put(MessageContract.LONGITUDE, messagesList.get(i).longitude);
            values.put(MessageContract.LATITUDE, messagesList.get(i).latitude);
            context.getContentResolver().insert(MessageContract.CONTENT_URI, values);

            //update the peer location
            values = new ContentValues();
            values.put(PeerContract.LATITUDE,messagesList.get(i).latitude);
            values.put(PeerContract.LONGITUDE,messagesList.get(i).longitude);
            context.getContentResolver().update(PeerContract.CONTENT_URI,values, PeerContract.ID +"=?", new String[] {peerId});
        }
    }

    private String getPeerId(Context context, String client) {
        // TODO Auto-generated method stub

        Cursor c = context.getContentResolver().query(PeerContract.CONTENT_URI,
                   new String[] { PeerContract.NAME, PeerContract.ID },
                   PeerContract.NAME + "=?", new String[] { client }, null);
        String peerId = "";
        if (c.moveToFirst()) {
            peerId = c.getString(c.getColumnIndex(PeerContract.ID));
        }
        return peerId;
    }

    private void writeMessages(JsonWriter jw, ArrayList<iMessage> messages, String chatRoom)
            throws IOException {

        jw.beginArray();
        if (messages.size() <= 0) {   //to avoid errors
            jw.beginObject();
            jw.endObject();
        } else {
            for (int i = 0; i < messages.size(); i++) {
                jw.beginObject();
                //jw.name("chatroom").value(messages.get(i).chatRoom);
                jw.name("chatroom").value(chatRoom);
                jw.name("timestamp").value(messages.get(i).timeStamp);
                jw.name("X-latitude").value(messages.get(i).latitude);
                jw.name("X-longitude").value(messages.get(i).longitude);
                jw.name("text").value(messages.get(i).message);
                jw.endObject();
            }
        }
        jw.endArray();
        jw.flush();
        jw.close();
    }


    private class clientId{
        private String name;
        private Long id;

        public clientId(String name, Long id){
            this.name = name;
            this.id   = id;
        }
        public clientId(String name, String id){
            this.name = name;
            this.id   = Long.parseLong(id);
        }

        public String getName(){
            return name;
        }
        public Long getId(){
            return id;
        }
    }
}
