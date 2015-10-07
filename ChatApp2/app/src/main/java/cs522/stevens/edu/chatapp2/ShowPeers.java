package cs522.stevens.edu.chatapp2;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;

/**
 * Created by Sandeep on 3/15/2015.
 */
public class ShowPeers extends ListActivity {

    private SimpleCursorAdapter messagesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_peers);
        fillData();
        final Intent intent = new Intent(this, PeerDetails.class);

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Cursor c = messagesAdapter.getCursor();
                String _id = c.getString(c.getColumnIndex(PeerContract.ID));
                intent.putExtra(PeerContract.ID, _id);

                startActivity(intent);

            }
        });
    }

    private void fillData() {
        // TODO Auto-generated method stub
        String[] from = new String[] { PeerContract.NAME, PeerContract.ID };
        // Fields on the UI to which we map
        int[] to = new int[] { android.R.id.text1, android.R.id.text2 };

        Cursor c = getContentResolver().query(PeerContract.CONTENT_URI, from, null, null, null);
        messagesAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_2, c, from, to, 0);

        setListAdapter(messagesAdapter);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.show_peers, menu);
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