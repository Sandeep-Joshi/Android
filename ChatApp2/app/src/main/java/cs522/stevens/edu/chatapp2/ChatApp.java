package cs522.stevens.edu.chatapp2;

import android.app.Activity;
import android.app.LoaderManager;
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
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Sandeep on 3/15/2015.
 */
public class ChatApp extends Activity implements View.OnClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    final static public String TAG = ChatApp.class.getCanonicalName();

    private static final String MESSAGE_SEPARATOR = ",";

    private SimpleCursorAdapter messagesAdapter;
    private static final int MESSAGES_LOADER_ID = 2;
    public static final String CLIENT_NAME_KEY = "client_name";

    Messenger mService = null;
    boolean mBound;
    MyResultReceiver resultReceiver;
    BroadcastReceiver updater;

    ListView list;
    ArrayAdapter<String> array;
    EditText destHost;
    EditText destPort;
    EditText msg;
    Button send;

    String name = "";

    ChatConn newConnection = new ChatConn();

    private class ChatConn implements ServiceConnection {
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
            mBound = true;
            Log.i(TAG, "Service connected to: " + name);
        }

        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
            Log.i(TAG, "Service disconnected from: " + name);
        }
    }

    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            fillData();
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        destHost = (EditText) findViewById(R.id.destination_host);
        destPort = (EditText) findViewById(R.id.destination_port);
        msg = (EditText) findViewById(R.id.message_text);
        name = this.getIntent().getStringExtra(CLIENT_NAME_KEY);

        send = (Button) findViewById(R.id.send_button);
        send.setOnClickListener(this);

        list = (ListView) findViewById(R.id.msgList);
        fillData();

        registerForContextMenu(list);

        Intent intent = new Intent(this, ChatReceiverService.class);

        startService(intent);
        bindService(intent, newConnection, BIND_AUTO_CREATE);

        updater = new Receiver();
        registerReceiver(updater, new IntentFilter(
                ChatReceiverService.MESSAGE_BROADCAST));
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(newConnection);
            mBound = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        Intent intent;

        switch (item.getItemId()) {
            case (R.id.show_peers):
                intent = new Intent(this, ShowPeers.class);
                startActivity(intent);
                return true;
        }
        return false;
    }

    private void fillData() {
        // TODO Auto-generated method stub
        String[] from = new String[] { MessageContract.MESSAGE_TEXT,
                MessageContract.SENDER };
        // Fields on the UI to which we map
        int[] to = new int[] { android.R.id.text1, android.R.id.text2 };

        getLoaderManager().initLoader(MESSAGES_LOADER_ID, null, this);
        messagesAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_2, null, from, to, 0);

        list.setAdapter(messagesAdapter);
    }

    public void onClick(View v) {

        sendMessage();

    }

    private void sendMessage() {
        try {

            InetAddress targetAddr = InetAddress.getByName(destHost.getText()
                    .toString());
            int targetPort = Integer.parseInt(destPort.getText().toString());
            String newMessage = msg.getText().toString();
            String messageString = name + MESSAGE_SEPARATOR + newMessage;

            Bundle b = new Bundle();
            b.putString("ADDRESS", targetAddr.getHostAddress());
            b.putInt("PORT", targetPort);
            b.putString("MESSAGE", messageString);

            resultReceiver = new MyResultReceiver(new Handler());
            b.putParcelable("Receiver", resultReceiver);

            if (!mBound)
                return;
            // Create and send a message to the service, using a supported
            // 'what' value
            Message msg = Message.obtain(null, ChatReceiverService.MSG_SEND, 0,
                    0);
            msg.setData(b);
            try {
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            Log.i(TAG, "Packet sent: " + messageString);

        } catch (UnknownHostException e) {
            Log.e(TAG, "Unknown host exception: " + e.getMessage());
        }
    }


    class MyResultReceiver extends ResultReceiver {

        public MyResultReceiver(Handler handler) { super(handler); }

        @Override protected void onReceiveResult(int resultCode, Bundle
                resultData) {

            /*if (resultCode == 100) { Toast.makeText(getApplicationContext(),
                    "Acknowledgement for message: " + resultData.getString("MESSAGE"),
                    Toast.LENGTH_SHORT).show(); } } */
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(newConnection);
        unregisterReceiver(updater);
        stopService(new Intent(this, ChatReceiverService.class));
    }

    public Loader<Cursor> onCreateLoader(int loaderId, Bundle arg1) {
        // TODO Auto-generated method stub

        CursorLoader loader;
        switch (loaderId) {
            case MESSAGES_LOADER_ID:

                String[] projection = { MessageContract.ID,
                        MessageContract.MESSAGE_TEXT, MessageContract.SENDER };
                loader = new CursorLoader(this, MessageContract.CONTENT_URI,
                        projection, null, null, null);
                break;
            default:
                loader = null;
                break;

        }
        return loader;
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // TODO Auto-generated method stub
        switch (loader.getId()) {
            case MESSAGES_LOADER_ID:
                messagesAdapter.swapCursor(data);
                break;

        }
    }

    public void onLoaderReset(Loader<Cursor> arg0) {
        // TODO Auto-generated method stub
        // data is not available anymore, delete reference
        messagesAdapter.swapCursor(null);
    }

}