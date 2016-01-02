package edu.stevens.cs522.myapplication.dialog;

import android.app.DialogFragment;

/**
 * Created by Sandeep on 4/26/2015.
 */
public interface IDialogListener {
    public void onYes(DialogFragment dialog);
    public void onCancel(DialogFragment dialog);
}
