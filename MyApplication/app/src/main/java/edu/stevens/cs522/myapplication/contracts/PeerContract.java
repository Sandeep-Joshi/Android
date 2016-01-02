package edu.stevens.cs522.myapplication.contracts;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;


/**
 * Created by Sandeep Joshi on 2/23/2015.
 */
public class PeerContract {
    public static final String ID = "_id";
    public static final String REGID = "regId";
    public static final String CLIENTID = "clientId";
    public static final String NAME = "name";
    public static final String ADDRESS ="address";
    public static final String PORT = "port";
    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";
    public static String PEERS_TABLE = "peers";

    public static String AUTHORITY = "edu.stevens.cs522.myapplication";
    public static String PATH = "/" + PEERS_TABLE;
    public static String URL = "content://"+AUTHORITY+PATH;
    public static Uri CONTENT_URI = Uri.parse(URL);

    public static String getId(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(ID));
    }
    public static String getName(Cursor cursor){
        return cursor.getString(cursor.getColumnIndexOrThrow(NAME));
    }
    public static String getRegId(Cursor cursor){
        return cursor.getString(cursor.getColumnIndexOrThrow(REGID));
    }
    public static String getClientId(Cursor cursor){
        return cursor.getString(cursor.getColumnIndexOrThrow(CLIENTID));
    }
    public static String getAddress(Cursor cursor){
        return cursor.getString(cursor.getColumnIndexOrThrow(ADDRESS));
    }
    public static String getPort(Cursor cursor){
        return cursor.getString(cursor.getColumnIndexOrThrow(PORT));
    }
    public static double getLongitude(Cursor cursor){
        return cursor.getDouble(cursor.getColumnIndexOrThrow(LONGITUDE));
    }
    public static double getLatitude(Cursor cursor){
        return cursor.getDouble(cursor.getColumnIndexOrThrow(LATITUDE));
    }
    public static void putId(ContentValues values,String id){
        values.put(ID,id);
    }
    public static void putName(ContentValues values,String name){
        values.put(NAME,name);
    }
    public static void putRegId(ContentValues values,String regId){
        values.put(REGID,regId);
    }
    public static void putClientId(ContentValues values,String clientId){
        values.put(CLIENTID,clientId);
    }
    public static void putAddress(ContentValues values,String address){
        values.put(ADDRESS,address);
    }
    public static void putPort(ContentValues values,String port){
        values.put(PORT,port);
    }
    public static void putLatitude(ContentValues values,String latitude){
        values.put(LATITUDE,latitude);
    }
    public static void putLongitude(ContentValues values,String longitude){
        values.put(LONGITUDE,longitude);
    }
}
