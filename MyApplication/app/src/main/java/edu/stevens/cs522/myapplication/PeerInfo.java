package edu.stevens.cs522.myapplication;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import edu.stevens.cs522.myapplication.Service.address;
import edu.stevens.cs522.myapplication.contracts.PeerContract;

/**
 * Created by Sandeep Joshi on 3/4/2015.
 */
public class PeerInfo extends Activity {

    private TextView txtPeer;
    private SimpleCursorAdapter cursorAdapter;

    double lng, lat;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
        }
        setContentView(R.layout.peer);
        txtPeer = (TextView) findViewById(R.id.info);
        String _id = this.getIntent().getStringExtra(PeerContract.ID);


        String[] projection = new String[]{PeerContract.NAME, PeerContract.CLIENTID, PeerContract.REGID,
                PeerContract.ADDRESS, PeerContract.PORT, PeerContract.LATITUDE,PeerContract.LONGITUDE};

        Cursor c = getContentResolver().query(PeerContract.CONTENT_URI,
                projection, PeerContract.ID + "=?", new String[]{_id}, null);

        if (c.moveToFirst()){

            TextView nameText = (TextView) findViewById(R.id.peer);
            TextView portText = (TextView) findViewById(R.id.port);
            TextView clientId = (TextView) findViewById(R.id.clientId);
            TextView regId    = (TextView) findViewById(R.id.regId);
            TextView addText  = (TextView) findViewById(R.id.address);
            TextView longText  = (TextView) findViewById(R.id.longitude);
            TextView latText  = (TextView) findViewById(R.id.lat);
            TextView geoText  = (TextView) findViewById(R.id.geo);

            nameText.setText(PeerContract.getName(c));
            portText.setText(PeerContract.getPort(c));
            clientId.setText(PeerContract.getClientId(c));
            regId.setText(PeerContract.getRegId(c));
            addText.setText(PeerContract.getAddress(c));
            lat = PeerContract.getLatitude(c);
            lng = PeerContract.getLongitude(c);
            longText.setText(String.valueOf(lng));
            latText.setText(String.valueOf(lat));
            address add = new address(lat, lng, this);
            add.getLocation(geoText);
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
