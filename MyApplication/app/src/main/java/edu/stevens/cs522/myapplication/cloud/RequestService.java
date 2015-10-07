package edu.stevens.cs522.myapplication.cloud;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import edu.stevens.cs522.myapplication.MainActivity;
import edu.stevens.cs522.myapplication.R;
import edu.stevens.cs522.myapplication.Service.alarmReceiver;
import edu.stevens.cs522.myapplication.cloud.Request.Register;
import edu.stevens.cs522.myapplication.cloud.Request.Unregister;
import edu.stevens.cs522.myapplication.cloud.Request.PostMessage;
import edu.stevens.cs522.myapplication.provider.constants;

/**
 * Created by Sandeep on 3/25/2015.
 */
public class RequestService extends IntentService {

    public static boolean isRegistered = false;
    public static final String NEW_MESSAGE_BROADCAST = "edu.stevens.cs522.chat.MessageBroadcast";
    public static int alarmtime;

    public RequestService() {
        super("RequestService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // TODO Auto-generated method stub
        //Extract the binder callback object
        Bundle bundle = new Bundle();

        SharedPreferences sharedpreferences = getSharedPreferences(MainActivity.MyPREFERENCES, Context.MODE_PRIVATE);

        if (sharedpreferences.contains(MainActivity.alarm))
            alarmtime = sharedpreferences.getInt(MainActivity.alarm,1000);

        if (intent.getAction().equals(ServiceHelper.ACTION_REGISTER)) {
            ResultReceiver rec = intent.getParcelableExtra(constants.binder);
            Register request = intent.getParcelableExtra(ServiceHelper.registerKey);

            RequestProcessor processor = new RequestProcessor();
            try {
                processor.perform(request, this);
                isRegistered = true;
                rec.send(ServiceHelper.MSG_SUCCESS,bundle);
            } catch (Exception e) {
                e.printStackTrace();
                rec.send(ServiceHelper.MSG_FAIL,bundle);
                //try to set an alarm now
                bundle.putParcelable("rec",rec);
                bundle.putParcelable("req",request);
                setAlarm(bundle);
            }
        } else if (intent.getAction().equals(ServiceHelper.ACTION_SYNC)) {

            if (isRegistered) {

                Bundle bundles2 = intent.getBundleExtra("pack");
                PostMessage sync = bundles2.getParcelable(ServiceHelper.syncKey);
                ResultReceiver rec = intent.getParcelableExtra(constants.binder);
                RequestProcessor processor = new RequestProcessor();
                try {
                    processor.perform(sync, this);
                    rec.send(ServiceHelper.MSG_SUCCESS,bundle);
                } catch (Exception e) {
                    rec.send(ServiceHelper.MSG_FAIL,bundle);
                    bundle.putParcelable("rec",rec);
                    bundle.putParcelable("req",bundles2);
                    setAlarmMess(bundle);
                    e.printStackTrace();

                }
            } else {
            }

        }
        else if (intent.getAction().equals(ServiceHelper.ACTION_UNREGISTER)) {
            if(isRegistered){
                Unregister unreg = intent
                        .getParcelableExtra(ServiceHelper.ACTION_UNREGISTER);
                RequestProcessor processor = new RequestProcessor();
                try {
                    processor.perform(unreg, this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setAlarm(Bundle bundle){
        AlarmManager alarmMgr = (AlarmManager)getApplicationContext().getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, alarmReceiver.class);
        intent.setAction(ServiceHelper.ACTION_REGISTER);
        intent.putExtra(constants.binder, bundle);
        PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(), alarmtime, pIntent);
    }

    private void setAlarmMess(Bundle bundle){
        AlarmManager alarmMgr = (AlarmManager)getApplicationContext().getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, alarmReceiver.class);
        intent.setAction(ServiceHelper.ACTION_SYNC);
        intent.putExtra(constants.binder, bundle);
        PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), alarmtime, pIntent);

    }

}
