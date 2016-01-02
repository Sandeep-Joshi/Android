package edu.stevens.cs522.myapplication;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.net.DatagramSocket;

import edu.stevens.cs522.myapplication.Service.ChatService;
import edu.stevens.cs522.myapplication.Service.IChatSendService;
import edu.stevens.cs522.myapplication.Service.address;
import edu.stevens.cs522.myapplication.cloud.ServiceHelper;
import edu.stevens.cs522.myapplication.contracts.ChatRoomContract;
import edu.stevens.cs522.myapplication.contracts.MessageContract;
import edu.stevens.cs522.myapplication.contracts.PeerContract;
import edu.stevens.cs522.myapplication.dialog.ChatRoomDialog;
import edu.stevens.cs522.myapplication.dialog.ChatSendDialog;
import edu.stevens.cs522.myapplication.dialog.IDialogListener;
import edu.stevens.cs522.myapplication.dialog.LocationDialog;
import edu.stevens.cs522.myapplication.entity.ChatRoom;
import edu.stevens.cs522.myapplication.entity.Peer;

/**
 * Created by Sandeep on 3/14/2015.
 */
//message send screen
public class ChatAppCloudFrag extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener, IDialogListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener{

    private static final int msgLoaderID = 10;

    private SimpleCursorAdapter msgCurAdptr;

    private String chatRoom;

    public Activity mActivity;
    public Context context;
    boolean bound;

    final static private String TAG = ChatAppCloudFrag.class.getSimpleName();

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

    MenuItem menuItem;

    private DatagramSocket clientSocket;

    public Peer peer = null;

    private Cursor cursor = null;

    private String client, url, uuid;

    private String regId;

    private ServiceHelper helper = null;

    public static final String CLIENT_NAME_KEY = "client_name";
    public static final String URL_KEY = "URL";
    public static final String CLIENT_REGID = "regid";
    public static final String APPID = "appid";
    public static final String LONGITUDE = "long";
    public static final String LATITUDE = "lat";
    public static final String CONTEXT = "context";

    public static final String DEFAULT_CLIENT_NAME = "client";

    LocationRequest locationRequest;
    GoogleApiClient locationClient;

    double longitude = 0, latitude = 0;

    // Create a DetailsFragment that contains the data for the correct index
    public static ChatAppCloudFrag newInstance(Bundle args) {
        ChatAppCloudFrag f = new ChatAppCloudFrag();
        f.setArguments(args);
        return f;
    }

    public String getChatRoom(){
        if(chatRoom==null)
            chatRoom = getArguments().getString("room");
        return chatRoom;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mActivity = getActivity();
        context = mActivity.getApplicationContext();
        //Read variable from intent and initialize
        Bundle sent   = getArguments();
        chatRoom = sent.getString("room");
        url    = sent.getString(URL_KEY);
        regId  = sent.getString(CLIENT_REGID);
        uuid   = sent.getString(APPID);
        client = sent.getString(CLIENT_NAME_KEY);
        latitude = sent.getDouble(LATITUDE);
        longitude = sent.getDouble(LONGITUDE);

        locationClient = new GoogleApiClient.Builder(getActivity()).addApi(LocationServices.API)
                .addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.main_client_cloud, container, false);
        context = rootView.getContext().getApplicationContext();

        sendButton = (Button) rootView.findViewById(R.id.send_button);
        sendButton.setOnClickListener(this);
        messageText = (EditText) rootView.findViewById(R.id.message_text);
        list = (ListView)rootView.findViewById(android.R.id.list);
        //list = getListView();
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //read location from the db
                Cursor c = msgCurAdptr.getCursor();
                double lat  = MessageContract.getLatitude(c);
                double lng = MessageContract.getLongitude(c);
                address addr = new address(lat,lng,getActivity());

                Bundle bundle = new Bundle();
                bundle.putParcelable("location",addr);
                LocationDialog dialog = new LocationDialog();
                dialog.setArguments(bundle);
                dialog.show(getFragmentManager(),"Location");

                return true;
            }
        });

        helper = new ServiceHelper(mActivity, client, url,chatRoom, latitude, longitude);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        filldata();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        //inflater.inflate(R.menu.main_client, menu);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        CursorLoader loader;
        switch (id) {
            case msgLoaderID:
                //Add filter for chat room now
                String[] projection = { MessageContract.ID, MessageContract.TIMESTAMP,
                        MessageContract.MESSAGE_TEXT, "peer", MessageContract.LATITUDE, MessageContract.LONGITUDE };
                loader = new CursorLoader(context, MessageContract.CONTENT_URI2,
                        projection, ChatRoomContract.NAME + "=?",
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
                if(cursor.moveToFirst())
                    msgCurAdptr.swapCursor(cursor);
                break;
        }
    }

    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        switch (cursorLoader.getId()) {
            case msgLoaderID:
                msgCurAdptr.swapCursor(null);
                break;
        }
    }

    @Override
    public void onStop(){
        locationClient.disconnect();
        super.onStop();
        //if(bound) {
            /*unregisterReceiver(receiver);
            bound = false;*/
        //}
    }
    @Override
    public void onStart(){
        super.onStart();
        locationClient.connect();
    }

    /*public void onDestroy(){
        super.onDestroy();
//        unbindService(chatService);
        stopService(new Intent(this, ChatReceiverService.class));
    }*/

    public static Peer addMessage(Context context, String message, String chatRoom,
                                  String client, String url, String uuid, double latitude, double longitude){
        Cursor cursor;
        ServiceHelper helper;

        Peer peer = null;

        Uri uri = null;

        helper = new ServiceHelper(context.getApplicationContext(), client, url, uuid, latitude, longitude);

        //Peer would have been saved at this point
        peer = getPeer(client, context);

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
        values.put(MessageContract.LATITUDE, latitude);
        values.put(MessageContract.LONGITUDE, longitude);

        //set id in helper
        helper.setId(peer.clientId);

        uri = context.getContentResolver().insert(MessageContract.CONTENT_URI, values);

        //pass message in bundle to request processor
        Bundle bundle = new Bundle();
        bundle.putString("client",client);
        bundle.putString("message",message);
        bundle.putString("room", chatRoom);
        helper.sendMessageToCloud(bundle);
        return  peer;
    }

    public void onClick(View view) {
        peer = addMessage(context, messageText.getText().toString(), chatRoom, client, url, uuid, latitude, longitude);
        messageText.setText("");
    }

    @Override
    public void onLocationChanged(Location location) {
        longitude = location.getLongitude();
        latitude = location.getLatitude();
    }

    @Override
    public void onConnected(Bundle bundle) {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(locationClient,locationRequest,this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Map connection has been suspend");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Map connection has failed");
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
                Toast.makeText(context.getApplicationContext(), "Message sent: " + bundle.getString("MESSAGE"),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }




    public void filldata(){
        String[] from = new String[] { MessageContract.MESSAGE_TEXT,
                "peer" };
        int[] to = new int[] { android.R.id.text1, android.R.id.text2 };

        getLoaderManager().initLoader(msgLoaderID, null, this);
        msgCurAdptr = new SimpleCursorAdapter(mActivity,
                android.R.layout.simple_list_item_2, null, from, to, 0);

        //list.setAdapter(msgCurAdptr);
        setListAdapter(msgCurAdptr);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        Intent intent;

        switch (item.getItemId()) {
            case (R.id.show_peers):
                /*intent = new Intent(mActivity, PeerList.class);
                startActivity(intent);*/
                return false;
            case (R.id.cancel):
                helper = new ServiceHelper(context.getApplicationContext(), client, url, uuid, latitude, longitude);
                if(peer==null)
                    peer = getPeer(client,context);
                helper.unregisterToCloud(peer);
            case (R.id.create_chatroom):
                Bundle bundle = new Bundle();
                bundle.putString("chat",getChatRoom());
                ChatRoomDialog dialog = new ChatRoomDialog();
                dialog.setArguments(bundle);
                dialog.show(getFragmentManager(),"ChatRoomDialog");
                return false;
            case (R.id.send):
                new ChatSendDialog().show(getFragmentManager(),"ChatSendDialog");
                return false;
        }
        return false;
    }

    public void onYes(DialogFragment dialog){
        EditText editText = (EditText) dialog.getDialog().findViewById(R.id.message);
        EditText editRoom = (EditText) dialog.getDialog().findViewById(R.id.chatroom);
        try {
            peer = addMessage(context, editText.getText().toString(), editRoom.getText().toString(), client, url, uuid, latitude, longitude);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static Peer getPeer(String client, Context context){
        Cursor cursor = context.getContentResolver().query(PeerContract.CONTENT_URI,
                new String[] {PeerContract.ID, PeerContract.CLIENTID, PeerContract.NAME,
                        PeerContract.REGID,PeerContract.ADDRESS,PeerContract.PORT,PeerContract.LATITUDE,PeerContract.LONGITUDE},
                PeerContract.NAME + "=?", new String[]{client}, null); //change from clienId to client as client id could be duplicate
        if((cursor!=null) &&(cursor.moveToFirst())) {
            return new Peer(cursor);
        } else
            return null;
    }

    public void onCancel(DialogFragment dialog) {
        dialog.getDialog().cancel();
    }

}
