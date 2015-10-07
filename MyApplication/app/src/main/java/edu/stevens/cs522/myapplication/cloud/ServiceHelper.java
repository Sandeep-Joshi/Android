package edu.stevens.cs522.myapplication.cloud;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import edu.stevens.cs522.myapplication.MainActivity;
import edu.stevens.cs522.myapplication.R;
import edu.stevens.cs522.myapplication.Service.alarmReceiver;
import edu.stevens.cs522.myapplication.entity.Peer;
import edu.stevens.cs522.myapplication.provider.constants;

/**
 * Created by Sandeep on 3/29/2015.
 */
public class ServiceHelper {
    private static String client;
    private static String url;
    private static String uuid;
    private static long id;
    private static String room;

    public static String clientId;
    private static Context context;
    public static String receiverKey = "Receiver";
    public static String registerKey = "Register";
    public static String responseKey = "Response";

    public static String syncKey = "Sync";
    public static String ContextKey = "Context";
    public static final int MSG_SUCCESS = 100;
    public static final int MSG_FAIL = 200;
    public static String sharedPreferenceClientId;
    public static String ACTION_REGISTER = "REGISTER";
    public static String ACTION_SYNC = "SYNC";
    public static String ACTION_UNREGISTER = "UNREGISTER";
    private double lat, lng;

    public ServiceHelper(Context context, String client, String url, String uuid, double lat,double lng) {
        this.context = context;
        this.client = client;
        this.url = url;
        this.uuid = uuid;
        this.lat = lat;
        this.lng = lng;
    }

    public void setId(String clientId){
        this.clientId = clientId;
    }
    public void setRoom(String room){
        this.room = room;
    }

    public void registerToCloud() {

        Request.Register register = new Request.Register(id, uuid, url, client);
        register.setLocation(lng,lat);
        Intent request = new Intent(context, RequestService.class);
        request.setAction(ACTION_REGISTER);
        request.putExtra(registerKey, register);
        //send a binder object
        request.putExtra(constants.binder, new ResultReceiver(null){
            @Override
            protected void onReceiveResult(int resultCode, Bundle data){
                if(resultCode==MSG_SUCCESS) {
                    showNotification(true);

                    //stop alarms
                    Intent intent = new Intent(context, alarmReceiver.class);
                    PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    manager.cancel(pIntent);
                    pIntent.cancel();
                } else
                    showNotification(false);
                //pass on to the Main or CloudChatApp
            }
        });
        context.startService(request);
    }

    public void sendMessageToCloud(Bundle bundle) {
        Request.PostMessage sync = new Request.PostMessage(Long.parseLong(clientId), uuid, url, bundle.getString("room"), bundle.getString("client"),bundle.getString("message"));
        sync.setLocation(lng,lat);
        Intent request = new Intent(context, RequestService.class);
        request.setAction(ACTION_SYNC);
        Bundle pack = new Bundle();
        pack.putParcelable(syncKey,sync);
        request.putExtra("pack",pack);
        request.putExtra(constants.binder,  new ResultReceiver(null){
            @Override
            protected void onReceiveResult(int resultCode, Bundle data){
                if(resultCode==MSG_SUCCESS) {
                    showNotification(true);
                    //check if alarm and cancel
                    //stop alarms
                    Intent intent = new Intent(context, alarmReceiver.class);
                    PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    manager.cancel(pIntent);
                    pIntent.cancel();
                } else
                    showNotification(false);
                //pass on to the Main or CloudChatApp
            }
        });
        context.startService(request);
    }


    public void unregisterToCloud(Peer peer) {
        Request.Unregister unreg = new Request.Unregister(peer.id, peer.regId, url, peer.name);
        unreg.setLocation(lng,lat);
        Intent request = new Intent(context, RequestService.class);
        request.setAction(ACTION_UNREGISTER);
        request.putExtra(ACTION_UNREGISTER, unreg);
        //send a binder object
        request.putExtra(constants.binder, new ResultReceiver(null){
            @Override
            protected void onReceiveResult(int resultCode, Bundle data){
                //pass on to the Main or CloudChatApp
            }
        });
        context.startService(request);
    }

    private void showNotification(boolean registered) {
        String header, title, text;
        if (registered) {
            header = "Registeration succeeded";
            title = "Registeration";
            text = "Registered to server";
        } else {
            header = "Registeration failed";
            title = "Registeration";
            text = "Could not register to server";
        }
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = new Notification(R.mipmap.ic_launcher,header, System.currentTimeMillis());

        Intent intent1 = new Intent(context, MainActivity.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(context, 0, intent1,
                PendingIntent.FLAG_UPDATE_CURRENT);

        notification.setLatestEventInfo(context, text, title, pendingNotificationIntent);
        notificationManager.notify(0, notification);

    }

    class resultReceiver extends ResultReceiver {

        public resultReceiver(Handler handler) { super(handler); }

        @Override
        protected void onReceiveResult(int resultCode, Bundle data){
            //pass on to the Main or CloudChatApp
            if(resultCode==MSG_SUCCESS)
                showNotification(true);
            else
                showNotification(false);
            //pass on to the Main or CloudChatApp
        }
    }
}
