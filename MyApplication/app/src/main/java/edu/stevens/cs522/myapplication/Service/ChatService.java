package edu.stevens.cs522.myapplication.Service;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import edu.stevens.cs522.myapplication.Service.ChatReceiverService;
import edu.stevens.cs522.myapplication.Service.IChatSendService;

/**
 * Created by Sandeep on 3/15/2015.
 */
public class ChatService  implements ServiceConnection{
    private IChatSendService binder;
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        binder = ((ChatReceiverService.chatBinder)iBinder).getService();
        Log.i("ChatService", "Service connected to " + componentName);
    }

    public void onServiceDisconnected(ComponentName componentName) {
        binder = null;
        Log.i("ChatService", "Service disconnected from " + componentName);    }

    public IChatSendService getBinder(){return binder;}
}
