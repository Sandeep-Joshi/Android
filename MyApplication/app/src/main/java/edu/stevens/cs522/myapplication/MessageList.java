package edu.stevens.cs522.myapplication;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import edu.stevens.cs522.myapplication.contracts.MessageContract;
import edu.stevens.cs522.myapplication.contracts.PeerContract;

/**
 * Created by Sandeep Joshi on 3/4/2015.
 */
public class MessageList extends ListActivity {
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
