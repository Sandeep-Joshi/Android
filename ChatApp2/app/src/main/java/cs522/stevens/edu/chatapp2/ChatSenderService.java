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
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;

public class ChatSenderService extends Service{

    private DatagramSocket datagramSocket;
    final static public String TAG = ChatReceiverService.class.getCanonicalName();
    public static final int MSG_SEND = 1;


    @Override
    public void onDestroy() {
        datagramSocket.close();
    }

    class  IncomingHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            try
            {
                switch (msg.what) {
                    case MSG_SEND:
                        Bundle b = msg.getData();
                        String message = b.getString("MESSAGE");
                        String address = b.getString("ADDRESS");
                        int port = b.getInt("PORT");

                        InetAddress targetAddr = InetAddress.getByName(address);
                        byte[] messageByte = message.getBytes();

                        DatagramPacket sendPacket = new DatagramPacket(messageByte,messageByte.length, targetAddr, port);
                        send(sendPacket);



                        Toast.makeText(getApplicationContext(), "hello! "+message, Toast.LENGTH_SHORT).show();

                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
            catch (UnknownHostException e) {
                Log.e(TAG, "Unknown host exception: " + e.getMessage());
            }
        }
    }

    final Messenger mMessenger = new Messenger(new IncomingHandler());

    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(getApplicationContext(), "Binding with service", Toast.LENGTH_SHORT).show();
        return mMessenger.getBinder();
    }

    public void send(final DatagramPacket p) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    //Log.i(ChatApp.TAG, "Sending a message..");
                    datagramSocket = new DatagramSocket(Integer.parseInt("6666"));
                    datagramSocket.send(p);
                } catch (IOException e) {
                    Log.e(ChatApp.TAG, "Can't send message!"+e);
                }
            }
        }).start();
    }


}
