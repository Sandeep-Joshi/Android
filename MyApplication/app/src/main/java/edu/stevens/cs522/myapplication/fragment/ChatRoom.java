package edu.stevens.cs522.myapplication.fragment;

import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import edu.stevens.cs522.myapplication.ChatAppCloudFrag;
import edu.stevens.cs522.myapplication.R;
import edu.stevens.cs522.myapplication.contracts.ChatRoomContract;
import edu.stevens.cs522.myapplication.contracts.MessageContract;
import edu.stevens.cs522.myapplication.dialog.ChatRoomDialog;
import edu.stevens.cs522.myapplication.dialog.ChatSendDialog;

/**
 * Created by Sandeep on 4/25/2015.
 */
//Chat room list.. invokes message window
public class ChatRoom extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>{
    boolean mDuelPane;
    private static final int LOADER_ID = 1;
    private static final int msgLoaderID = 10;
    private SimpleCursorAdapter msgCurAdptr;
    private SimpleCursorAdapter msgCurAdptr2;
    String room = null;

    // Currently selected item in the ListView
    int mCurCheckPosition = 0;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String[] from = new String[] { ChatRoomContract.NAME };
        // Fields on the UI to which we map
        int[] to = new int[] { android.R.id.text1 };

        getLoaderManager().initLoader(LOADER_ID, null, this);
        msgCurAdptr = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_2, null, from, to, 0);
        setListAdapter(msgCurAdptr);

        // Check if the FrameLayout with the id details exists in current mode
        View detailsFrame = getActivity().findViewById(R.id.details);

        mDuelPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;

        // If the screen is rotated onSaveInstanceState() below will restore
        // most recently selected. Get the value attached to curChoice and store it in
        // mCurCheckPosition
        if (savedInstanceState != null) {
            mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);
        }

        if (mDuelPane) {
            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            showDetails(mCurCheckPosition);
        } else{
            showDetails(mCurCheckPosition);
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        Intent intent;

        switch (item.getItemId()) {
            case (R.id.show_peers):
                return false;
            case (R.id.create_chatroom):
                new ChatRoomDialog().show(getFragmentManager(),"ChatRoomDialog");
                return true;
            case (R.id.send):
                new ChatSendDialog().show(getFragmentManager(),"ChatSendDialog");
                return true;
        }
        return false;
    }


    // Called every time the screen orientation changes or Android kills an Activity
    // to conserve resources
    // We save the last item selected in the list here and attach it to the key curChoice
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("curChoice", mCurCheckPosition);
    }

    // When a list item is clicked we want to change chat room
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        showDetails(position);
    }

    void showDetails(int index) {

            // The most recently selected hero in the ListView is sent
            mCurCheckPosition = index;
            getListView().setItemChecked(index, true);
            Cursor cursor = (Cursor) getListView().getItemAtPosition(index);
            if (cursor != null) {
                room = ChatRoomContract.getName(cursor);
                Intent in = getActivity().getIntent();
                if (mDuelPane) {

                    ChatAppCloudFrag details = (ChatAppCloudFrag) getFragmentManager().findFragmentById(R.id.details);

                    if (details == null || details.getChatRoom() != room) {
                        Bundle args = new Bundle();
                        args.putString("room", room);
                        args.putString(ChatAppCloudFrag.URL_KEY, in.getStringExtra(ChatAppCloudFrag.URL_KEY));
                        args.putString(ChatAppCloudFrag.CLIENT_REGID, in.getStringExtra(ChatAppCloudFrag.CLIENT_REGID));
                        args.putString(ChatAppCloudFrag.APPID, in.getStringExtra(ChatAppCloudFrag.APPID));
                        args.putString(ChatAppCloudFrag.CLIENT_NAME_KEY, in.getStringExtra(ChatAppCloudFrag.CLIENT_NAME_KEY));
                        details = ChatAppCloudFrag.newInstance(args);
                        // Start Fragment transactions
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.replace(R.id.details, details);
                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        ft.commit();
                    }

                } else {
                    // Launch a new Activity to show our DetailsFragment
                    Intent intent = new Intent();

                    // Define the class Activity to call
                    intent.setClass(getActivity(), ChatRoomDetails.class);
                    // Pass along the currently selected index assigned to the keyword index
                    intent.putExtra("room", room);
                    String client = in.getStringExtra(ChatAppCloudFrag.CLIENT_NAME_KEY);
                    //String client = getArguments().getString(ChatAppCloudFrag.CLIENT_NAME_KEY);
                    intent.putExtra("bundle", in.getExtras());
                    intent.putExtra(ChatAppCloudFrag.CLIENT_NAME_KEY, in.getStringExtra(ChatAppCloudFrag.CLIENT_NAME_KEY));
                    intent.putExtra(ChatAppCloudFrag.CLIENT_REGID, in.getStringExtra(ChatAppCloudFrag.CLIENT_REGID));
                    intent.putExtra(ChatAppCloudFrag.APPID, in.getStringExtra(ChatAppCloudFrag.APPID));
                    intent.putExtra(ChatAppCloudFrag.URL_KEY, in.getStringExtra(ChatAppCloudFrag.URL_KEY));
                    // Call for the Activity to open
                    startActivity(intent);
                }
            }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = null;

        switch (id) {

            case LOADER_ID:
                String[] projection = { ChatRoomContract.ID, ChatRoomContract.NAME};
                loader = new CursorLoader(getActivity(), ChatRoomContract.CONTENT_URI,
                        projection, null,null, null);
                break;
            case msgLoaderID:
                //Add filter for chat room now
                String[] projection2 = { MessageContract.TIMESTAMP,
                        MessageContract.MESSAGE_TEXT, "peer" };
                loader = new CursorLoader(getActivity(), MessageContract.CONTENT_URI2,
                        projection2, ChatRoomContract.NAME + "=?",
                        new String[] { room }, null);
                break;
            default:
                break;
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LOADER_ID:
                msgCurAdptr.swapCursor(data);
                break;
            case msgLoaderID:
                if(data.moveToFirst())
                    msgCurAdptr.swapCursor(data);
                break;
            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case msgLoaderID:
                msgCurAdptr.swapCursor(null);
                break;
            case LOADER_ID:
                msgCurAdptr.swapCursor(null);
        }
    }
}
