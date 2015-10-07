package edu.stevens.cs522.chat.oneway.server;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import edu.stevens.cs522.chat.oneway.server.contracts.PeerContract;

/**
 * Created by Sandeep Joshi on 3/4/2015.
 */
public class PeerList extends ListActivity {
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
