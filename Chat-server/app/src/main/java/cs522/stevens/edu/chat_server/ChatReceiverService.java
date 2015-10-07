package cs522.stevens.edu.chat_server;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import edu.stevens.cs522.chat.oneway.server.entity.Message;
import edu.stevens.cs522.chat.oneway.server.entity.Peer;
import edu.stevens.cs522.chat.oneway.server.provider.constants;

/**
 * Created by Sandeep on 3/14/2015.
 */
public class ChatReceiverService extends Service implements IChatSendService {
    private DatagramSocket datagramSocket;
    private final IBinder binder = new chatBinder();
    public static final String NEW_MESSAGE_BROADCAST = "edu.stevens.cs522.chat.MessageBroadcast";

    public void send(final DatagramPacket p) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    datagramSocket.send(p);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class chatBinder extends Binder {
        IChatSendService getService() {
            return ChatReceiverService.this;
        }
    }

    @Override
    public void onDestroy(){
        datagramSocket.close();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ReceiveMessageTask recvTask = new ReceiveMessageTask();
        recvTask.execute((Void[]) null);
        return START_STICKY;
        //return super.onStartCommand(intent, flags, startId);
    }

    public void onCreate(){
        try {
            datagramSocket = new DatagramSocket(Integer.parseInt(constants.sendPort));
        } catch (SocketException e) {
            Log.e("ChatApp", "Unable to create socket.");
        }
    }

    private class ReceiveMessageTask extends AsyncTask<Void, Message, Void>{
        private Intent msgUpdateBroadcast = new Intent(NEW_MESSAGE_BROADCAST);

        @Override
        protected Void doInBackground(Void... voids) {
            while (true){
                Message msg = nextMessage();
                publishProgress(msg);
                sendBroadcast(msgUpdateBroadcast);

            }
        }

        @Override
        protected void onProgressUpdate(Message... values) {
            Context context = getApplicationContext();
            String text = values[0].message;
            String title = "M:" + values[0].sender;
            Toast.makeText(context, title + " " + text, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            stopSelf();
        }

    }

    private Message nextMessage(){
        byte[] data = new byte[1024];
        Long peerId;

        DatagramPacket packet = new DatagramPacket(data,data.length);
        try {
            datagramSocket.receive(packet);
            Log.i("ChatReceiver","Received a packet.");
        } catch (IOException e) {
            Log.e("ChatReceiver","Error receiving a packet.");
        }

        InetAddress source = packet.getAddress();
        int port           = packet.getPort();

        String newStr = new String(packet.getData(), 0, packet.getLength());
        //Remove the client name from the message
        String[] splits = newStr.split(":");
        String name    = splits[0];
        String message = splits[1];

        Log.i("ChatApp","Message received: "+name+": "+ message);
        //String peerId = addP

        Peer peer = new Peer(splits[0], source, port);
        peerId = peer.UriInsert(getContentResolver());
        Message newMessage = new Message(message,name,peerId);
        newMessage.UriInsert(getContentResolver());

        return newMessage;
    }

}
