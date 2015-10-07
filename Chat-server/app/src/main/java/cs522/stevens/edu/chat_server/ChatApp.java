package cs522.stevens.edu.chat_server;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Sandeep on 3/15/2015.
 */
public class ChatApp {

        private static final int msgLoaderID = 1;

        private SimpleCursorAdapter msgCurAdptr;

        final static private String TAG = ChatApp.class.getSimpleName();

        ChatService chatService = new ChatService();

        private MainActivity.IChatSendService sendService;

        private ListView list;

        private BroadcastReceiver receiver = new Receiver();

        private EditText destinationHost;

        private EditText destinationPort;

        private EditText messageText;

        private Button sendButton;

        private String name;

        private DatagramSocket clientSocket;

        public static final String CLIENT_NAME_KEY = "client_name";

        public static final String DEFAULT_CLIENT_NAME = "client";

        @Override
        public void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            if(savedInstanceState!=null)
                super.onRestoreInstanceState(savedInstanceState);
            setContentView(R.layout.main_client);
            bindElements();
            filldata();

            Intent intent = new Intent(this,ChatReceiverService.class);
            startService(intent);   //starting the chat receiving as a servive
            bindService(intent,chatService,BIND_AUTO_CREATE);
            registerReceiver(receiver,new IntentFilter(ChatReceiverService.NEW_MESSAGE_BROADCAST));
        }
        public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
            CursorLoader loader;
            switch (id) {
                case msgLoaderID:

                    String[] projection = { MessageContract.ID,
                            MessageContract.MESSAGE_TEXT, MessageContract.SENDER };
                    loader = new CursorLoader(this, MessageContract.CONTENT_URI,
                            projection, null, null, null);
                    break;
                default:
                    loader = null;
                    break;        }
            return loader;
        }


        private class ChatService implements ServiceConnection {
            public void onServiceConnected(ComponentName name, IBinder service) {
                sendService = ((ChatReceiverService.chatBinder) service)
                        .getService();
                Log.i(TAG, "Service connected to: " + name);
            }

            public void onServiceDisconnected(ComponentName name) {
                sendService = null;
                Log.i(TAG, "Service disconnected from: " + name);
            }
        }


        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
            switch (cursorLoader.getId()) {
                case msgLoaderID:
                    msgCurAdptr.swapCursor(cursor);
                    break;
            }
        }

        public void onLoaderReset(Loader<Cursor> cursorLoader) {
            msgCurAdptr.swapCursor(null);
        }

        public void onStop(){
            unregisterReceiver(receiver);
            super.onStop();
        }

        public void onDestroy(){
            super.onDestroy();
            unbindService(chatService);
            stopService(new Intent(this, ChatReceiverService.class));
        }

        public void onClick(View view) {
            try {
                InetAddress destAddr = InetAddress.getByName(destinationHost.getText().toString());
                int destPort         = Integer.parseInt(destinationPort.getText().toString());
                byte[] sendData      = new byte[1024];  // Combine sender and message text; default encoding is UTF-8

                // TODO get data from UI
                String message = name + ":" + messageText.getText().toString();
                sendData = message.getBytes();
                // End todo

                DatagramPacket sendPacket = new DatagramPacket(sendData,
                        sendData.length, destAddr, destPort);

                sendService.send(sendPacket);
                //clientSocket.send(sendPacket);

                Log.i(TAG, "Sent packet: " + messageText);


            } catch (UnknownHostException e) {
                Log.e(TAG, "Unknown host exception: ", e);
            } catch (IOException e) {
                Log.e(TAG, "IO exception: ", e);
            }

            messageText.setText("");
        }

        public class Receiver extends BroadcastReceiver{

            @Override
            public void onReceive(Context context, Intent intent) {
                filldata();
            }
        }

        public void bindElements(){
            destinationHost = (EditText) findViewById(R.id.destination_host);
            destinationPort = (EditText) findViewById(R.id.destination_port);
            messageText     = (EditText) findViewById(R.id.message_text);
            name            = this.getIntent().getStringExtra(CLIENT_NAME_KEY);

            sendButton = (Button) findViewById(R.id.send_button);
            sendButton.setOnClickListener(this);
            list = (ListView) findViewById(R.id.msgList);
        }

        public void filldata(){
            String[] from = new String[] { MessageContract.MESSAGE_TEXT,
                    MessageContract.SENDER };
            int[] to = new int[] { android.R.id.text1, android.R.id.text2 };

            getLoaderManager().initLoader(msgLoaderID, null, this);
            msgCurAdptr = new SimpleCursorAdapter(this,
                    android.R.layout.simple_list_item_2, null, from, to, 0);

            list.setAdapter(msgCurAdptr);
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.main_client, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            super.onOptionsItemSelected(item);
            Intent intent;

            switch (item.getItemId()) {
                case (R.id.show_peers):
                    intent = new Intent(this, PeerList.class);
                    startActivity(intent);
                    return true;
            }
            return false;
        }

    }

