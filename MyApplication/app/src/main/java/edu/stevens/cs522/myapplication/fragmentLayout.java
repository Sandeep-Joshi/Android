package edu.stevens.cs522.myapplication;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import edu.stevens.cs522.myapplication.dialog.ChatNotificationDialog;
import edu.stevens.cs522.myapplication.dialog.IDialogListener;
import edu.stevens.cs522.myapplication.entity.ChatRoom;

/**
 * Created by Sandeep on 4/23/2015.
 */
//Two fragments - 1. Chat list - 2. Message window
 public class fragmentLayout extends Activity implements IDialogListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);
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
               // new ChatRoomDialog().show(getFragmentManager(),"ChatRoomDialog");
                return false;
            case (R.id.send):
                //new ChatSendDialog().show(getFragmentManager(),"ChatSendDialog");
                return false;
        }
        return false;
    }

    @Override
    public void onYes(DialogFragment dialog) {
        if (dialog.getTag().equals("ChatRoomDialog")) {
            EditText editText = (EditText) dialog.getDialog().findViewById(R.id.chatroom);
            String room = editText.getText().toString();
            try {
                ChatRoom chat = new ChatRoom(room);
                Long id = chat.get(this);
                if(id!=null) { //already exists
                    ChatNotificationDialog notify = new ChatNotificationDialog();
                    notify.show(getFragmentManager(),"notify");
                } else{
                    id = chat.add(this);
                }
                chat.add(this);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else if (dialog.getTag().equals("ChatSendDialog")) {
            EditText editText = (EditText) dialog.getDialog().findViewById(R.id.chatroom);
            EditText editMsg  = (EditText) dialog.getDialog().findViewById(R.id.message);

            try {
                ChatAppCloudFrag.addMessage(this,editMsg.getText().toString(),editText.getText().toString(),
                        getIntent().getStringExtra(ChatAppCloudFrag.CLIENT_NAME_KEY),
                        getIntent().getStringExtra(ChatAppCloudFrag.URL_KEY),
                        getIntent().getStringExtra(ChatAppCloudFrag.APPID),
                        getIntent().getDoubleExtra(ChatAppCloudFrag.LATITUDE,0),
                        getIntent().getDoubleExtra(ChatAppCloudFrag.LONGITUDE,0));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onCancel(DialogFragment dialog) {
        dialog.getDialog().cancel();
    }
}
