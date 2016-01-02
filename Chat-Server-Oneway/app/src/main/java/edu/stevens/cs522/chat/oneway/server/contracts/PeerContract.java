package edu.stevens.cs522.chat.oneway.server.contracts;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import java.net.InetAddress;

import edu.stevens.cs522.chat.oneway.server.provider.chatProvider;


/**
 * Created by Sandeep Joshi on 2/23/2015.
 */
public class PeerContract {
    public static final String ID = "_id";
    public static final String NAME = "name";
    public static final String ADDRESS ="address";
    public static final String PORT = "port";

    public static String AUTHORITY = "edu.stevens.cs522.chatapp";
    public static String PATH = "/" + chatProvider.PEERS_TABLE;
    public static String URL = "content://"+AUTHORITY+PATH;
    public static Uri CONTENT_URI = Uri.parse(URL);

    public static String getId(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(ID));
    }
    public static String getName(Cursor cursor){
        return cursor.getString(cursor.getColumnIndexOrThrow(NAME));
    }
    public static String getAddress(Cursor cursor){
        return cursor.getString(cursor.getColumnIndexOrThrow(ADDRESS));
    }
    public static String getPort(Cursor cursor){
        return cursor.getString(cursor.getColumnIndexOrThrow(PORT));
    }

    public static void putId(ContentValues values,String id){
        values.put(ID,id);
    }
    public static void putName(ContentValues values,String name){
        values.put(NAME,name);
    }
    public static void putAddress(ContentValues values,String address){
        values.put(ADDRESS,address);
    }
    public static void putPort(ContentValues values,String port){
        values.put(PORT,port);
    }
}
