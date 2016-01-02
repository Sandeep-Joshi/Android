package edu.stevens.cs522.myapplication.Service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import edu.stevens.cs522.myapplication.MainActivity;
import edu.stevens.cs522.myapplication.cloud.Request;
import edu.stevens.cs522.myapplication.cloud.RequestProcessor;
import edu.stevens.cs522.myapplication.cloud.ServiceHelper;
import edu.stevens.cs522.myapplication.provider.constants;

/**
 * Created by Sandeep on 4/18/2015.
 */
public class AlarmService extends IntentService {

    public static boolean isRegistered = false;

    public AlarmService() {
        super(constants.alarm);
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        try {

            Bundle bundle = intent.getBundleExtra(constants.binder);

            if(intent.getAction().equals(ServiceHelper.ACTION_REGISTER)){
                ResultReceiver rec = bundle.getParcelable("rec");//.getParcelableExtra(constants.binder);
                Request.Register request = bundle.getParcelable("req");//.getParcelableExtra(ServiceHelper.registerKey);
                RequestProcessor processor = new RequestProcessor();

                try {
                    processor.perform(request, this);
                    isRegistered = true;
                    rec.send(ServiceHelper.MSG_SUCCESS,bundle);
                } catch (Exception e) {
                    rec.send(ServiceHelper.MSG_FAIL,bundle);
                    e.printStackTrace();
                }
            }else if (intent.getAction().equals(ServiceHelper.ACTION_SYNC)){
                if (isRegistered) {

                    Bundle bundles2 = bundle.getParcelable("req");//.getBundleExtra("req");
                    Request.PostMessage sync = bundles2.getParcelable(ServiceHelper.syncKey);
                    ResultReceiver rec = bundle.getParcelable("rec");
                    RequestProcessor processor = new RequestProcessor();
                    try {
                        processor.perform(sync, this);
                        rec.send(ServiceHelper.MSG_SUCCESS,bundle);
                    } catch (Exception e) {
                        rec.send(ServiceHelper.MSG_FAIL, bundle);
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}
