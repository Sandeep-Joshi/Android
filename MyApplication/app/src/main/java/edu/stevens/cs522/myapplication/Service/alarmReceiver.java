package edu.stevens.cs522.myapplication.Service;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import edu.stevens.cs522.myapplication.cloud.Request;
import edu.stevens.cs522.myapplication.cloud.RequestProcessor;
import edu.stevens.cs522.myapplication.cloud.ServiceHelper;
import edu.stevens.cs522.myapplication.provider.constants;

/**
 * Created by Sandeep on 4/21/2015.
 */
public class alarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        try {
            Bundle bundle = intent.getParcelableExtra(constants.binder);
            Intent alarmIntent = new Intent(context, AlarmService.class);
            alarmIntent.setAction(intent.getAction());
            alarmIntent.putExtra(constants.binder, bundle);

            context.startService(alarmIntent);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}
