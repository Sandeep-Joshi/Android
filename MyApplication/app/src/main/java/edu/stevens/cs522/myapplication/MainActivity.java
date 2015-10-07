package edu.stevens.cs522.myapplication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.UUID;

import edu.stevens.cs522.myapplication.cloud.RequestProcessor;
import edu.stevens.cs522.myapplication.cloud.ServiceHelper;

//Frist screen
public class MainActivity extends Activity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static String reg_Id;  //returned from server
    private static String uuid;  //installation id
    private int Alarm;  //installation id

    //Shared pref keys
    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String Client = "client";
    public static final String AppId = "appid";
    public static final String RegId = "regid";
    public static final String Url = "url";
    public static final String code = "code";
    public static final String alarm = "alarm";

    private ServiceHelper helper = null;
    MyBroadcastReceiver brec;
    final static private String TAG = MainActivity.class.getSimpleName();

    String client, url;

    SharedPreferences sharedpreferences;

    EditText editClient;
    EditText editSite;
    EditText editAlarm;
    SharedPreferences.Editor editor;

    double longitude = 0, latitude = 0;
    LocationRequest locationRequest;
    GoogleApiClient locationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editClient = (EditText) findViewById(R.id.edit_message);
        editSite = (EditText) findViewById(R.id.edit_url);
        editAlarm = (EditText) findViewById(R.id.alarm);

        //read name from shared preference... we will check it on send
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        if (sharedpreferences.contains(AppId)) {
            uuid = sharedpreferences.getString(AppId, "");
        } else {
            //generate a UUID and save it
            uuid = UUID.randomUUID().toString();
            editor = sharedpreferences.edit();
            editor.putString(AppId, uuid);
            editor.commit();
        }

        if (sharedpreferences.contains(alarm)) {
            int time = sharedpreferences.getInt(alarm, 1000);
            editAlarm.setText(String.valueOf(time));
        }
        //read and restore client name from shared pref
        if (sharedpreferences.contains(Client)) {
            editClient.setText(sharedpreferences.getString(Client, ""));
        }

        if (sharedpreferences.contains(Url)) {
            editSite.setText(sharedpreferences.getString(Url, ""));
        }

        locationClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API)
                .addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
    }

    public void onResume(){
        super.onResume();
        //register a Broadcastreceiver on receiving broadcast proceed to next screen wait until then
        brec = new MyBroadcastReceiver();
        getApplicationContext().registerReceiver(brec, new IntentFilter(RequestProcessor.REGISTER_BROADCAST));
    }


    public void onStop(){
        locationClient.disconnect();
        super.onStop();
    }

    @Override
    public void onStart(){
        super.onStart();
        locationClient.connect();
    }

     @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void sendMessage(View view) {
        client = editClient.getText().toString();
        url = editSite.getText().toString();
        Alarm = Integer.parseInt(editAlarm.getText().toString());

        //save this in shared pref
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt(alarm, Alarm);
        editor.putString(Client, client);
        editor.putString(Url, url);
        editor.putString(RegId, reg_Id);
        editor.commit();

        //Check if the name is already in the sharedpreference
        if (sharedpreferences.contains(Client)) {
            if (sharedpreferences.getString(Client, "").equals(client)) {
                //read the id from share pref as well
                if (sharedpreferences.contains(RegId)) {
                    reg_Id = sharedpreferences.getString(RegId, "");
                }
            } else
                reg_Id = null;
        }

        //just get location once as this activity is happening just once


        //send request for server and wait for response
        helper = new ServiceHelper(getApplicationContext(), client, url, uuid, latitude, longitude);
        helper.registerToCloud();
        Toast.makeText(this, "Waiting for registration to complete.", Toast.LENGTH_SHORT).show();
    }

    public void onClick(View view) {
        //call subsequent screen
        switch (view.getId()){
            case R.id.next:
                sendMessage(view);
                break;
            default:
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(locationClient,locationRequest,this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Map connection has been suspend");

    }

    @Override
    public void onLocationChanged(Location location) {
        longitude = location.getLongitude();
        latitude = location.getLatitude();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Map connection has failed");
    }

    public class MyBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent data) {
            int responseCode = data.getIntExtra(code,0);
            reg_Id = data.getStringExtra(RegId);

            if((responseCode>=200)&&(responseCode<400)){

                //Save name in shared preference
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(Client, client);
                editor.putString(Url, url);
                editor.putString(RegId, reg_Id);
                editor.commit();

                //Intent intent = new Intent(context.getApplicationContext(), ChatAppCloud.class);
                Intent intent = new Intent(context.getApplicationContext(), fragmentLayout.class);

                intent.putExtra(ChatAppCloudFrag.CLIENT_NAME_KEY, client);
                intent.putExtra(ChatAppCloudFrag.CLIENT_REGID, reg_Id);
                intent.putExtra(ChatAppCloudFrag.APPID, uuid);
                intent.putExtra(ChatAppCloudFrag.URL_KEY, url);
                intent.putExtra(ChatAppCloudFrag.LONGITUDE, longitude);
                intent.putExtra(ChatAppCloudFrag.LATITUDE, latitude);
                startActivity(intent);
        } else{
                //registration failed
               Toast.makeText(context, "Registration failed.", Toast.LENGTH_SHORT).show();
        }
    }
    }
}



