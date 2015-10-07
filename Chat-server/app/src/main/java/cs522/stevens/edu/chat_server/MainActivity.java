package cs522.stevens.edu.chat_server;

import android.app.Activity;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class ChatServer extends Activity implements AdapterView.OnItemClickListener {

        final static public String TAG = ChatServer.class.getCanonicalName();

        /*
         * Socket used both for sending and receiving
         */
        private DatagramSocket serverSocket;

        /*
         * True as long as we don't get socket errors
         */
        private boolean socketOK = true;

        private CartDbAdapter DbAdapter;

        private Cursor cursor;

        private ArrayAdapter<String> adapter;
        private int mLastCorrectPosition = -1;
        private int mButtonPosition = 0;

        private ActionMode mActionMode;


        private SimpleCursorAdapter CursorAdapter;
        private ListView listView;


        public List<String> mess = new ArrayList<String>();

        public static String list = "list";

        private ActionMode mAction;

        Button next;
        /*
         * Called when the activity is first created.
         */

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (savedInstanceState != null) {
                mess = savedInstanceState.getStringArrayList(list);
            }
            setContentView(R.layout.main);

            if (DbAdapter != null)
                cursor = DbAdapter.fetchAllMessages();
            else {
                DbAdapter = new CartDbAdapter(this);
                DbAdapter.open();
                cursor = DbAdapter.fetchAllMessages();
            }

            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mess);

            //display messages if available
            listView = (ListView) findViewById(R.id.msgList);
            String[] from = {"name", "messageText"};
            int[] to = {R.id.sender, R.id.message};
            CursorAdapter = new SimpleCursorAdapter(this, R.layout.message_layout, cursor, from, to);
            listView.setAdapter(CursorAdapter);

            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            class MyActionModeCallback implements ActionMode.Callback{

                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    mode.getMenuInflater().inflate(R.menu.peer,menu);
                    return true;
                }

                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.info:
                            //send the intent for peerinfo
                            return true;
                        case R.id.message:
                            //send the intent for peelist
                            return true;
                        default:
                            return false;
                    }
                }

                public void onDestroyActionMode(ActionMode mode) {

                }
            }

            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    MyActionModeCallback callback = new MyActionModeCallback();
                    mAction= startActionMode(callback);
                    mAction.setTitle("Peers");
                    return true;
                }
            });



             /**
             * Let's be clear, this is a HACK to allow you to do network communication on the main thread.
             * This WILL cause an ANR, and is only provided to simplify the pedagogy.  We will see how to do
             * this right in a future assignment (using a Service managing background threads).
             */
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            try {
                /*
                 * Get port information from the resources.
                 */
                int port = Integer.parseInt(this.getString(R.string.app_port));
                serverSocket = new DatagramSocket(port);
                //initialize
                if(serverSocket == null){
                    serverSocket = new DatagramSocket(null);
                    serverSocket.setReuseAddress(true);
                    serverSocket.bind(new InetSocketAddress(port));
                }
            } catch (Exception e) {
                Log.e(TAG, "Cannot open socket" + e.getMessage());
                return;
            }

            /*
             * TODO: Initialize the UI.
             */
            next = (Button)findViewById(R.id.next);
            /*
             * End Todo
             */

        }


        public void onClick(View v) {

            byte[] receiveData = new byte[1024];

            switch (v.getId()){
                case R.id.next:
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                    try {
                        serverSocket.receive(receivePacket);
                        Log.i(TAG, "Received a packet");

                        InetAddress sourceIPAddress = receivePacket.getAddress();
                        Log.i(TAG, "Source IP Address: " + sourceIPAddress);
                /*
                 * TODO: Extract sender and receiver from message and display.
                 */
                        receiveData = receivePacket.getData();
                        String newStr = new String(receiveData, 0, receivePacket.getLength());
                        //Remove the client name from the message
                        String[] splits = newStr.split(":");
                        if(splits.length > 1) {
                            CartDbAdapter dbAdapter = new CartDbAdapter(this);
                            dbAdapter.open();
                            Peer peer = new Peer(splits[0],sourceIPAddress,receivePacket.getPort());
                            dbAdapter.persist(peer,splits[1]);
                            cursor = CursorAdapter.swapCursor(dbAdapter.fetchAllMessages());
                            dbAdapter.close();
                        }
                /*
                 * End Todo
                 */
                    } catch (Exception e) {

                        Log.e(TAG, "Problems receiving packet: " + e.getMessage());
                        socketOK = false;
                    }
            }

        }
        @Override
        public void onStop(){
            closeSocket();
            super.onStop();
        }
        /*
         * Close the socket before exiting application
         */
        public void closeSocket() {
            serverSocket.close();
        }

        /*
         * If the socket is OK, then it's running
         */
        boolean socketIsOK() {
            return socketOK;
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        }
    }

    /**
     * Created by Sandeep Joshi on 3/4/2015.
     */
    public static class PeerList extends ListActivity {
        private ListView list;
        static final private int INFO = 1;
        static final private int MESSAGE = 2;
        private ActionMode mActionMode;


        private SimpleCursorAdapter messagesAdapter;


        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (savedInstanceState != null) {
            }

            fillData();
            setContentView(R.layout.peerlist);
            final Intent intent = new Intent(this, PeerInfo.class);

            /*getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                    Cursor c = messagesAdapter.getCursor();
                    String _id = c.getString(c.getColumnIndex(PeerContract.ID));
                    intent.putExtra(PeerContract.ID, _id);

                    startActivity(intent);
                }
            });*/

            getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                    /*Cursor c = messagesAdapter.getCursor();
                    String _id = c.getString(c.getColumnIndex(PeerContract.ID));
                    intent.putExtra(PeerContract.ID, _id);*/
                    return true;
                }
            });
        }



        @Override
        public boolean onCreateOptionsMenu(Menu menu){
            getMenuInflater().inflate(R.menu.peerlist, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item){
            Intent addIntent;
            String _id, _name;
            Cursor c;
            switch (item.getItemId()) {
                case (R.id.message):
                    c = messagesAdapter.getCursor();
                    _id = c.getString(c.getColumnIndex(PeerContract.ID));
                    _name = c.getString(c.getColumnIndex(PeerContract.NAME));
                    addIntent = new Intent(getApplicationContext(), MessageList.class);
                    addIntent.putExtra(PeerContract.ID, _id);
                    addIntent.putExtra(PeerContract.NAME, _name);
                    startActivity(addIntent);
                    return true;
                case (R.id.info):
                    c = messagesAdapter.getCursor();
                    _id = c.getString(c.getColumnIndex(PeerContract.ID));
                    _name = c.getString(c.getColumnIndex(PeerContract.NAME));
                    addIntent = new Intent(getApplicationContext(), PeerInfo.class);
                    addIntent.putExtra(PeerContract.ID, _id);
                    addIntent.putExtra(PeerContract.NAME, _name);
                    startActivity(addIntent);
                    return true;
                case(R.id.cancel):
                    finish();
                    return true;
            }
            return false;
        }

        private void fillData() {
            String[] from = new String[] { PeerContract.NAME, PeerContract.ID };
            int[] to = new int[] { android.R.id.text1, android.R.id.text2 };

            Cursor c = getContentResolver().query(PeerContract.CONTENT_URI, from, null, null, null);
            messagesAdapter = new SimpleCursorAdapter(this,
                    android.R.layout.simple_list_item_2, c, from, to, 0);

            setListAdapter(messagesAdapter);
        }


    }

    /**
     * Created by Sandeep on 3/14/2015.
     */
    public static class ChatApp extends Activity implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

        private static final int msgLoaderID = 1;

        private SimpleCursorAdapter msgCurAdptr;

        final static private String TAG = ChatApp.class.getSimpleName();

        ChatService chatService = new ChatService();

        private IChatSendService sendService;

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

        public class Receiver extends BroadcastReceiver {

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

    /**
     * Created by Sandeep on 3/15/2015.
     */
    public static class ChatService  implements ServiceConnection {
        private IChatSendService binder;
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = ((ChatReceiverService.chatBinder)iBinder).getService();
            Log.i("ChatService", "Service connected to " + componentName);
        }

        public void onServiceDisconnected(ComponentName componentName) {
            binder = null;
            Log.i("ChatService", "Service disconnected from " + componentName);    }

        public IChatSendService getBinder(){return binder;}
    }

    /**
     * Created by Sandeep on 3/14/2015.
     */
    public static interface IChatSendService {
        public void send(DatagramPacket p); //Binders service method
    }

    /**
     * Created by Sandeep on 3/14/2015.
     */
    public static class ChatReceiverService extends Service implements IChatSendService {
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

        private class ReceiveMessageTask extends AsyncTask<Void, Message, Void> {
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

            Peer peer = new Peer(splits[0], source, port);
            peerId = peer.UriInsert(getContentResolver());
            Message newMessage = new Message(message,name,peerId);
            newMessage.UriInsert(getContentResolver());

            return newMessage;
        }

    }

    /**
     * Created by Sandeep Joshi on 3/4/2015.
     */
    public static class PeerInfo extends Activity {

        private TextView txtPeer;
        private SimpleCursorAdapter cursorAdapter;

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (savedInstanceState != null) {
            }
            setContentView(R.layout.peer);
            txtPeer = (TextView) findViewById(R.id.info);
            String _id = this.getIntent().getStringExtra(PeerContract.ID);


            String[] projection = new String[]{PeerContract.NAME,
                    PeerContract.ADDRESS, PeerContract.PORT};

            Cursor c = getContentResolver().query(PeerContract.CONTENT_URI,
                    projection, PeerContract.ID + "=?", new String[]{_id}, null);

            if (c.moveToFirst()){

                TextView nameText = (TextView) findViewById(R.id.peer);
                TextView portText = (TextView) findViewById(R.id.port);
                TextView addText = (TextView) findViewById(R.id.address);

                nameText.setText(c.getString(c.getColumnIndexOrThrow(PeerContract.NAME)));
                portText.setText(c.getString(c.getColumnIndexOrThrow(PeerContract.PORT)));
                addText.setText(c.getString(c.getColumnIndexOrThrow(PeerContract.ADDRESS)));
            }

        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.peer, menu);
            return true;
        }
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            super.onOptionsItemSelected(item);

            switch (item.getItemId()) {
                case (R.id.info):
                case (R.id.msg):
                    finish();
                    return true;
                case (R.id.cancel):
                    finish();
                    return true;
            }
            return false;
        }
    }

    /**
     * Created by Sandeep Joshi on 3/4/2015.
     */
    public static class MessageList extends ListActivity {
        private ListView list;

        private SimpleCursorAdapter messagesAdapter;


        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (savedInstanceState != null) {
            }
            setContentView(R.layout.message_list);
            fillData();
        }


        private void fillData() {

            String[] from = new String[] { MessageContract.ID, MessageContract.MESSAGE_TEXT };
            int[] to = new int[] { android.R.id.text1,android.R.id.text2 };

            String _id = this.getIntent().getStringExtra(PeerContract.ID);
            TextView peerText = (TextView)findViewById(R.id.sender);
            peerText.setText(this.getIntent().getStringExtra(PeerContract.NAME));

            Cursor c = getContentResolver().query(MessageContract.CONTENT_URI, from,
                    MessageContract.PEER_FK + "=?", new String[ ] {_id}, null);
            c.moveToFirst();
            messagesAdapter = new SimpleCursorAdapter(this,
                    android.R.layout.simple_list_item_2, c, from, to, 0);

            setListAdapter(messagesAdapter);
        }
    }

    /**
     * Created by Sandeep on 3/14/2015.
     */
    public static class ChatSenderService extends Service implements IChatSendService {

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
}
