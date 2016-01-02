package edu.stevens.cs522.myapplication.fragment;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import edu.stevens.cs522.myapplication.ChatAppCloudFrag;
import edu.stevens.cs522.myapplication.PeerList;
import edu.stevens.cs522.myapplication.R;
import edu.stevens.cs522.myapplication.dialog.IDialogListener;

/**
 * Created by Sandeep on 4/25/2015.
 */
//chat details single screen
public class ChatRoomDetails extends Activity implements IDialogListener{

    private boolean isLandscape;
    private String client;
    private String url;
    public static String CLIENT_NAME_KEY = "Client";
    public static String URL_KEY = "URL";
    ChatAppCloudFrag fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_details);

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putString("room", getIntent().getStringExtra("room"));
            arguments.putString(ChatAppCloudFrag.CLIENT_NAME_KEY,getIntent().getStringExtra(
                            ChatAppCloudFrag.CLIENT_NAME_KEY));
            arguments.putString(ChatAppCloudFrag.URL_KEY, getIntent().getStringExtra(
                            ChatAppCloudFrag.URL_KEY));
            arguments.putString(ChatAppCloudFrag.CLIENT_REGID, getIntent().getStringExtra(
                    ChatAppCloudFrag.CLIENT_REGID));
            arguments.putString(ChatAppCloudFrag.APPID, getIntent().getStringExtra(
                    ChatAppCloudFrag.APPID));

            fragment = new ChatAppCloudFrag();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction().add(R.id.chatroom_details, fragment).commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.fragmain, menu);
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
            case (R.id.create_chatroom):
                //new ChatRoomDialog().show(getFragmentManager(),"ChatRoomDialog");
                return false;
            case (R.id.send):
                //new ChatSendDialog().show(getFragmentManager(),"ChatSendDialog");
                return false;
            case (R.id.cancel):

        }
        return false;
    }

    @Override
    public void onYes(DialogFragment dialog) {
        fragment.onYes(dialog);
    }

    @Override
    public void onCancel(DialogFragment dialog) {
        dialog.getDialog().cancel();
    }
}
