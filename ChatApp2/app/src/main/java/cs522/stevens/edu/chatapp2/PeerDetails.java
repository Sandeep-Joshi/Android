package cs522.stevens.edu.chatapp2;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * Created by Sandeep on 3/15/2015.
 */
public class PeerDetails extends ListActivity {

    private String _id;
    SimpleCursorAdapter messagesAdapter;
    TextView txtPeerDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.peer_details);
        txtPeerDetails = (TextView) findViewById(R.id.tvPeerDetails);
        _id = this.getIntent().getStringExtra(PeerContract.ID);
        fillText(_id);
        fillList(_id);

    }

    private void fillText(String _id) {
        // TODO Auto-generated method stub
        String[] projection = new String[] { PeerContract.NAME,
                PeerContract.ID, PeerContract.ADDRESS, PeerContract.PORT };

        Cursor c = getContentResolver().query(PeerContract.CONTENT_URI,
                projection, PeerContract.ID + "=?", new String[] { _id }, null);
        if (c.moveToFirst())
        {
            String details = "ID: "
                    + c.getString(c.getColumnIndex(PeerContract.ID)) + " | Name: "
                    + c.getString(c.getColumnIndex(PeerContract.NAME))
                    + " | Address: "
                    + c.getString(c.getColumnIndex(PeerContract.ADDRESS))
                    + " | Port: " + c.getString(c.getColumnIndex(PeerContract.PORT));
            txtPeerDetails.setText(details);
        }


    }

    private void fillList(String _id) {
        // TODO Auto-generated method stub
        String[] from = new String[] { MessageContract.MESSAGE_TEXT,
                MessageContract.ID };
        // Fields on the UI to which we map
        int[] to = new int[] { android.R.id.text1, android.R.id.text2 };

        Cursor c = getContentResolver().query(MessageContract.CONTENT_URI,
                from, MessageContract.PEER_FK + "=?", new String[] { _id }, null);

        messagesAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_2, c, from, to, 0);

        setListAdapter(messagesAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.peer_details, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case (R.id.cancel):
                finish();
                return true;
        }
        return false;
    }

}
