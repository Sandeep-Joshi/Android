package edu.stevens.cs522.chat.oneway.server;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import edu.stevens.cs522.chat.oneway.server.provider.constants;

/**
 * Created by Sandeep on 3/14/2015.
 */
public class ChatSenderService extends Service implements IChatSendService {

    private DatagramSocket dataSocket;
    private final IBinder binder = new MyBinder();

    public void send(final DatagramPacket p) {
        //start anew thread for the sending from UI
        new Thread(new Runnable() {
            public void run() {
                try {
                    dataSocket.send(p);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
    @Override
    public void onCreate(){
        try {
            dataSocket = new DatagramSocket(Integer.parseInt(constants.sendPort));
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy(){
        dataSocket.close();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public class MyBinder extends Binder {
        ChatSenderService getService(){
            return ChatSenderService.this;
        }
    }
}
