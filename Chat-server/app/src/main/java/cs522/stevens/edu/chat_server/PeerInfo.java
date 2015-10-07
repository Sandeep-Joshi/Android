package cs522.stevens.edu.chat_server;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import edu.stevens.cs522.chat.oneway.server.contracts.PeerContract;

/**
 * Created by Sandeep Joshi on 3/4/2015.
 */
public class PeerInfo extends Activity{

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
