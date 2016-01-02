package edu.stevens.cs522.myapplication.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import edu.stevens.cs522.myapplication.R;
import edu.stevens.cs522.myapplication.Service.address;

/**
 * Created by Sandeep on 4/26/2015.
 */
public class LocationDialog extends DialogFragment {

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

        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View v = factory.inflate(R.layout.dialogloc, null);
        TextView tv = (TextView)v.findViewById(R.id.address);
        Bundle bundle = getArguments();
        if (bundle!= null) {
            address addr = bundle.getParcelable("location");
            TextView t = (TextView)v.findViewById(R.id.lat);
            ((TextView)(v.findViewById(R.id.lat))).setText(addr.getLat());
            ((TextView)(v.findViewById(R.id.lng))).setText(addr.getLng());
            addr.setContext(getActivity());
            addr.getLocation(tv);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Sent from" )
                .setView(v)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return builder.create();

    }
}