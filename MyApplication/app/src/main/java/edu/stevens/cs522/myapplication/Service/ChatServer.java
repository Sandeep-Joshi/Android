/*********************************************************************

 Chat server: accept chat messages from clients.

 Sender name and GPS coordinates are encoded
 in the messages, and stripped off upon receipt.

 Copyright (c) 2012 Stevens Institute of Technology

 **********************************************************************/
package edu.stevens.cs522.myapplication.Service;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import edu.stevens.cs522.myapplication.R;
import edu.stevens.cs522.myapplication.database.CartDbAdapter;
import edu.stevens.cs522.myapplication.entity.Peer;

public class ChatServer extends Activity implements AdapterView.OnItemClickListener {

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
                        //Peer peer = new Peer(0L, splits[0], "1",sourceIPAddress, receivePacket.getPort());
                        //dbAdapter.persist(peer,splits[1]);
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