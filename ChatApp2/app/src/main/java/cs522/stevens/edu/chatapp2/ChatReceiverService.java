package cs522.stevens.edu.chatapp2;

/**
 * Created by Sandeep on 3/15/2015.
 */
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

public class ChatReceiverService extends Service {

    private DatagramSocket datagramSocket;
    private static final String MESSAGE_SEPARATOR = ",";
    final static public String TAG = ChatReceiverService.class
            .getCanonicalName();
    public static final int MSG_SEND = 1;

    // private final IBinder binder = new ChatBinder();

    private HandlerThread messengerThread = null;
    Messenger messenger = null;
    private MessageHandler handler = null;

    @Override
    public void onCreate() {

        try {
            super.onCreate();
            messengerThread = new HandlerThread(TAG,	android.os.Process.THREAD_PRIORITY_BACKGROUND);
            messengerThread.start();
            Looper messengerLooper = messengerThread.getLooper();
            handler = new MessageHandler(messengerLooper);

            messenger = new Messenger(handler);
            //messenger = new Messenger(new MessageHandler());

            datagramSocket = new DatagramSocket(Integer.parseInt("6666"));

        } catch (IOException e) {
            Log.e(ChatApp.TAG, "Cannot create socket." + e);
        }
    }

    @Override
    public void onDestroy() {
        datagramSocket.close();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(ChatApp.TAG,
                "Started Chat service, running task for receiving messages.");
        ReceiveMessageTask recvTask = new ReceiveMessageTask();
        recvTask.execute((Void[]) null);
        return START_STICKY;
    }


    class MessageHandler extends Handler {

        public MessageHandler(Looper looper)
        {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case MSG_SEND:
                        Bundle b = msg.getData();
                        String message = b.getString("MESSAGE");
                        String address = b.getString("ADDRESS");
                        int port = b.getInt("PORT");

                        InetAddress targetAddr = InetAddress.getByName(address);
                        byte[] messageByte = message.getBytes();

                        DatagramPacket sendPacket = new DatagramPacket(messageByte,
                                messageByte.length, targetAddr, port);

                        datagramSocket.send(sendPacket);
                        //send(sendPacket);

                        ResultReceiver resultReceiver = b.getParcelable("Receiver");
                        resultReceiver.send(100, b);

                        break;
                    default:
                        super.handleMessage(msg);
                }
            } catch (UnknownHostException e) {
                Log.e(TAG, "Unknown host exception: " + e.getMessage());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.e(TAG, "Can't send message!" + e);
            }
        }
    }



    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(getApplicationContext(), "Binding with service", Toast.LENGTH_SHORT)
                .show();
        return messenger.getBinder();
    }

    private class ReceiveMessageTask extends AsyncTask<Void, MessageInfo, Void> {
        @Override
        protected Void doInBackground(Void... params) {

			/*
			 * Main background loop: receiving and saving messages.
			 * "publishProgress" calls back to the UI loop to notify the user
			 * when a message is received.
			 */

            try {
                while (true) {
                    MessageInfo msg = messageHandler();
                    publishProgress(msg);
                }
            } catch (IOException e) {
                Log.e(ChatApp.TAG, "Problem receiving a message: " + e);
            }
            return ((Void) null);
        }

        @Override
        protected void onProgressUpdate(MessageInfo... values) {
            Context context = getApplicationContext();
            String expandedText = "Message "+values[0].messageText;
            String expandedTitle = "Sender " + values[0].sender;

            Toast.makeText(context, expandedTitle + " | " + expandedText,
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(Void result) {
            stopSelf();
        }
    }

    private MessageInfo messageHandler() throws IOException {
        byte[] receiveData = new byte[1024];

        Log.i(ChatApp.TAG, "Waiting for a message.");

        DatagramPacket receivePacket = new DatagramPacket(receiveData,
                receiveData.length);

        datagramSocket.receive(receivePacket);
        Log.i(ChatApp.TAG, "Received a packet.");

        InetAddress sourceIPAddress = receivePacket.getAddress();
        Log.d(ChatApp.TAG, "Source IP Address: " + sourceIPAddress);

        String msgContents[] = new String(receivePacket.getData(), 0,
                receivePacket.getLength()).split(MESSAGE_SEPARATOR);

        String name = msgContents[0];
        String message = msgContents[1];
        InetAddress host = receivePacket.getAddress();
        int port = receivePacket.getPort();

        Log.i(ChatApp.TAG, "Received from " + name + ": " + message);
        String peerId = addPeer(new Peer(name, host.getHostAddress(), port));
        MessageInfo messageInfo = new MessageInfo(message, name);
        addMessage(messageInfo, peerId);

        return messageInfo;

    }

    public static final String MESSAGE_BROADCAST = "cs522.stevens.edu.chat.MessageBroadcast";
    private Intent msgUpdateBroadcast = new Intent(MESSAGE_BROADCAST);

    public void addMessage(MessageInfo msg, String peerId) {

        ContentValues values = new ContentValues();
        values.put(MessageContract.MESSAGE_TEXT, msg.messageText);
        values.put(MessageContract.SENDER, msg.sender);
        values.put(MessageContract.PEER_FK, peerId);
        getContentResolver().insert(MessageContract.CONTENT_URI, values);

        sendBroadcast(msgUpdateBroadcast);
    }

    public String addPeer(Peer peer) {
        String addr = peer.address;
        int port = peer.port;
        String name = peer.name;
        String peerId = "";
        Cursor c = getContentResolver().query(PeerContract.CONTENT_URI, null,
                PeerContract.NAME + "=?", new String[] { name }, null);

        ContentValues values = new ContentValues();
        if (c.getCount() > 0) {
            values.put(PeerContract.ADDRESS, addr);
            values.put(PeerContract.PORT, port);
            getContentResolver().update(PeerContract.CONTENT_URI, values,
                    PeerContract.NAME + "=?", new String[] { name });
            if (c.moveToFirst()) {
                peerId = c.getString(c.getColumnIndex(PeerContract.ID));
            }
        } else {
            values.put(PeerContract.ADDRESS, addr);
            values.put(PeerContract.PORT, port);
            values.put(PeerContract.NAME, name);
            Uri uri = getContentResolver().insert(PeerContract.CONTENT_URI,
                    values);
            peerId = uri.getLastPathSegment();
        }

        return peerId;
    }

}
