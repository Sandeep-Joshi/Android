package edu.stevens.cs522.myapplication.Service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.util.Log;
import android.widget.Toast;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Messenger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import edu.stevens.cs522.myapplication.entity.Peer;
import edu.stevens.cs522.myapplication.entity.iMessage;

/**
 * Created by Sandeep on 3/14/2015.
 */
public class ChatReceiverService extends Service implements IChatSendService{
    private DatagramSocket datagramSocket;
    public final IBinder binder = new chatBinder();
    public static final String NEW_MESSAGE_BROADCAST = "edu.stevens.cs522.chat.MessageBroadcast";
    public static final int SEND = 1;
    private HandlerThread thread = null;
    private MessageHandler handler = null;
    Messenger messenger = null;

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

        public IChatSendService getService() {
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
    final static public String TAG = ChatReceiverService.class
            .getCanonicalName();

    public void onCreate(){
        try {
            super.onCreate();

            datagramSocket = new DatagramSocket(Integer.parseInt("6666"));//constants.sendPort));
            thread = new HandlerThread(TAG,android.os.Process.THREAD_PRIORITY_BACKGROUND);
            thread.start();
            Looper looper = thread.getLooper();
            handler = new MessageHandler(looper);
            messenger = new Messenger(handler);
        } catch (SocketException e) {
            Log.e("ChatApp", "Unable to create socket.");
        }
    }

    private class ReceiveMessageTask extends AsyncTask<Void, iMessage, Void> {
        private Intent msgUpdateBroadcast = new Intent(NEW_MESSAGE_BROADCAST);

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                while (true) {
                    iMessage msg = nextMessage();
                    publishProgress(msg);
                    sendBroadcast(msgUpdateBroadcast);

                }
            }catch (IOException e){
                Log.e("ChatApp","Error receiving a message");
            }
            return ((Void)null);
        }


        @Override
        protected void onProgressUpdate(iMessage... values) {
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

    private iMessage nextMessage() throws IOException{
        byte[] data = new byte[1024];
        Long peerId;

        DatagramPacket packet = new DatagramPacket(data,data.length);
        try {
            datagramSocket.receive(packet);
            Log.i("ChatReceiver", "Received a packet.");
        } catch (IOException e) {
            Log.e("ChatReceiver", "Error receiving a packet.");
        }

        InetAddress source = packet.getAddress();
        int port           = packet.getPort();

        String newStr = new String(packet.getData(), 0, packet.getLength());
        //Remove the client name from the message
        String[] splits = newStr.split(":");
        String name    = splits[0];
        String message = splits[1];

        Log.i("ChatApp", "Message received: " + name + ": " + message);
        //String peerId = addP

       // Peer peer = new Peer(0L,splits[0],"1", source, port);
       // peerId = peer.UriInsert(getContentResolver());
        Long tsLong = System.currentTimeMillis()/1000;
       // iMessage newMessage = new iMessage(message, name, "_default",tsLong, 0L, peerId);
       // newMessage.UriInsert(getContentResolver());

       // return newMessage;
        return null;
    }

    class MessageHandler extends Handler{
        public MessageHandler(Looper looper)
        {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case SEND:
                        Bundle bundle = msg.getData();
                        String address = bundle.getString("address"); //address
                        int port = bundle.getInt("port");  //port
                        String message = bundle.getString("messageText"); //messageText
                        InetAddress addr = InetAddress.getByName(address);
                        byte[] messageByte = message.getBytes();

                        DatagramPacket sendPacket = new DatagramPacket(messageByte,messageByte.length, addr, port);

                        datagramSocket.send(sendPacket);
                        ResultReceiver resultReceiver = bundle.getParcelable("Bundle");
                        resultReceiver.send(100, bundle);

                        break;
                    default:
                        super.handleMessage(msg);
                }
            } catch (UnknownHostException e) {
                Log.e(TAG, "Unknown host exception: " + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "Error sending messages");
            }
        }
    }

}
