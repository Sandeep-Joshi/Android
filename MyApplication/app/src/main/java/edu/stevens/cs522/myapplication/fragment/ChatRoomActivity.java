package edu.stevens.cs522.myapplication.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import edu.stevens.cs522.myapplication.ChatAppCloudFrag;
import edu.stevens.cs522.myapplication.PeerList;
import edu.stevens.cs522.myapplication.R;

/**
 * Created by Sandeep on 4/26/2015.
 */
public class ChatRoomActivity extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Check if the device is in landscape mode
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            finish(); //kill it as we don't need this activity now and previous one should suffice
            return;
        }

        // Check if we have any hero data saved
        if (savedInstanceState == null) {
            ChatAppCloudFrag details = new ChatAppCloudFrag();
            details.setArguments(getIntent().getExtras());
            getFragmentManager().beginTransaction().add(android.R.id.content, details).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.chatroom, menu);
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
