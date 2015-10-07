package edu.stevens.cs522.myapplication.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import edu.stevens.cs522.myapplication.R;

/**
 * Created by Sandeep on 4/26/2015.
 */
public class ChatSendDialog extends DialogFragment {

    IDialogListener mListener;
    private String chatRoom;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (IDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString());
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction

        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View v = factory.inflate(R.layout.dialog_send_message, null);
        View tv = v.findViewById(R.id.chatroom);
        Bundle bundle = getArguments();
        if (bundle!= null)
            ((EditText)tv).setText(bundle.getString("chat"));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.message_edit_text)
                .setView(v)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mListener.onYes(ChatSendDialog.this);
                            }
                        }).setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onCancel(ChatSendDialog.this);
                    }
                });


        return builder.create();

    }
}