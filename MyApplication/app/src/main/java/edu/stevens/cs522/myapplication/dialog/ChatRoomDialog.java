package edu.stevens.cs522.myapplication.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

import edu.stevens.cs522.myapplication.R;

/**
 * Created by Sandeep on 4/26/2015.
 */
public class ChatRoomDialog extends DialogFragment {
    IDialogListener mListener;

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
        LayoutInflater inflater = getActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(R.string.create_chatroom).setView(inflater.inflate
                (R.layout.diaglog_create_room, null)).setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mListener.onYes(ChatRoomDialog.this);
                            }
                        }).setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mListener.onCancel(ChatRoomDialog.this);
                            }
                        });

        return builder.create();
    }
}
