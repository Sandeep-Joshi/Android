package edu.stevens.cs522.myapplication;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.net.DatagramSocket;

import edu.stevens.cs522.myapplication.Service.ChatReceiverService;
import edu.stevens.cs522.myapplication.Service.ChatService;
import edu.stevens.cs522.myapplication.Service.IChatSendService;
import edu.stevens.cs522.myapplication.cloud.ServiceHelper;
import edu.stevens.cs522.myapplication.contracts.MessageContract;
import edu.stevens.cs522.myapplication.contracts.PeerContract;
import edu.stevens.cs522.myapplication.entity.ChatRoom;
import edu.stevens.cs522.myapplication.entity.Peer;

/**
 * Created by Sandeep on 3/14/2015.
 */
//not being used now
public class ChatAppCloud extends Activity implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    private static final int msgLoaderID = 1;

    private SimpleCursorAdapter msgCurAdptr;

    private String chatRoom;

    boolean bound;

    final static private String TAG = ChatAppCloud.class.getSimpleName();

    ChatService chatService = new ChatService();

    resultReceiver bReceiver;


    private IChatSendService sendService;

    private ListView list;

    private BroadcastReceiver receiver = new Receiver();

    private EditText destinationHost;

    private EditText destinationPort;

    private EditText messageText;

    private Button sendButton;

    private String name;

    private DatagramSocket clientSocket;

    private Peer peer = null;

    private Cursor cursor = null;

    private String client, url, uuid;

    private String regId;

    private ServiceHelper helper = null;

    public static final String CLIENT_NAME_KEY = "client_name";
    public static final String URL_KEY = "URL";
    public static final String CLIENT_REGID = "regid";
    public static final String APPID = "appid";
    public static final String CONTEXT = "context";

    double longitude = 0, latitude = 0;

    public static final String DEFAULT_CLIENT_NAME = "client";

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(savedInstanceState!=null)
         super.onRestoreInstanceState(savedInstanceState);
        setContentView(R.layout.main_client_cloud);
        bindElements();


        //Read variable from intent and initialize
        Intent sent   = getIntent();
        chatRoom = sent.getStringExtra("room");
        url    = sent.getStringExtra(URL_KEY);
        regId  = sent.getStringExtra(CLIENT_REGID);
        uuid   = sent.getStringExtra(APPID);
        this.setTitle(chatRoom);
        filldata();
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        CursorLoader loader;
        switch (id) {
            case msgLoaderID:
                //Add filter for chat room now
                String[] projection = { MessageContract.ID,
                        MessageContract.MESSAGE_TEXT, MessageContract.SENDER };
                loader = new CursorLoader(this, MessageContract.CONTENT_URI,
                        projection, MessageContract.CHATROOM + "=?",
                        new String[] { chatRoom }, null);
                break;
            default:
                loader = null;
                break;
        }
        return loader;
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
        super.onStop();
       if(bound) {
            unregisterReceiver(receiver);
            bound = false;
       }
    }

    public void onDestroy(){
        super.onDestroy();
//        unbindService(chatService);
        stopService(new Intent(this, ChatReceiverService.class));
    }

    public static void addMessage(Context context, String message, String chatRoom,
                                  String client, String url, String uuid, double lat, double lng){
        Cursor cursor;
        Peer peer = null;
        ServiceHelper helper;

        Uri uri = null;

        helper = new ServiceHelper(context.getApplicationContext(), client, url, uuid, lat, lng);

        //Peer would have been saved at this point
        cursor = context.getContentResolver().query(PeerContract.CONTENT_URI,
                new String[] {PeerContract.ID, PeerContract.CLIENTID, PeerContract.NAME,
                        PeerContract.REGID,PeerContract.ADDRESS,PeerContract.PORT,PeerContract.LONGITUDE, PeerContract.LATITUDE},
                PeerContract.NAME + "=?", new String[]{client}, null); //change from clienId to client as client id could be duplicate
        if(cursor.moveToFirst()) {
            peer = new Peer(cursor);
        }
        //check save chatroom to db and get id
        ChatRoom room = new ChatRoom(chatRoom);
        Long roomid = room.add(context);
        ContentValues values = new ContentValues();

        //save message to db with 0 id
        values.put(MessageContract.MESSAGE_TEXT, message);
        values.put(MessageContract.SENDER, client);
        values.put(MessageContract.SEQ, context.getResources().getString(R.string.defSeq));
        values.put(MessageContract.TIMESTAMP, System.currentTimeMillis());
        values.put(MessageContract.CHATROOM, roomid);
        values.put(MessageContract.PEER_FK, peer.id);

        //set id in helper
        helper.setId(peer.clientId);

        uri = context.getContentResolver().insert(MessageContract.CONTENT_URI, values);
        uri.getFragment();

        //pass message in bundle to request processor
        Bundle bundle = new Bundle();
        bundle.putString("client",client);
        bundle.putString("message",message);
        bundle.putString("room", chatRoom);
        helper.sendMessageToCloud(bundle);
    }

    public void onClick(View view) {
        addMessage(this, messageText.getText().toString(), chatRoom, client, url, uuid, latitude, longitude);
        messageText.setText("");
    }

    public class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            filldata();
        }
    }

    class resultReceiver extends ResultReceiver {

        public resultReceiver(Handler handler) { super(handler); }

        @Override protected void onReceiveResult(int resultCode, Bundle bundle) {
            if (resultCode == 100) {
                Toast.makeText(getApplicationContext(), "Message sent: " + bundle.getString("MESSAGE"),
                    Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void bindElements(){
        messageText     = (EditText) findViewById(R.id.message_text);
        name            = this.getIntent().getStringExtra(CLIENT_NAME_KEY);

        sendButton = (Button) findViewById(R.id.send_button);
        sendButton.setOnClickListener(this);
        list = (ListView) findViewById(R.id.msgList);

        client = this.getIntent().getStringExtra(CLIENT_NAME_KEY);
        url    = this.getIntent().getStringExtra(URL_KEY);
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
