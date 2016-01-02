package edu.stevens.cs522.myapplication.contracts;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by Sandeep on 4/25/2015.
 */
public class ChatRoomContract {
    public static final String ID ="_id";
    public static final String NAME ="name";
    public static final String CHATROOM_TABLE = "chatroom";

    public static String AUTHORITY = "edu.stevens.cs522.myapplication";
    public static String PATH = "/"+ CHATROOM_TABLE;
    public static String URL = "content://"+AUTHORITY+PATH;
    public static Uri CONTENT_URI = Uri.parse(URL);

    public static String getName(Cursor cursor){
       int index = cursor.getColumnIndexOrThrow(NAME);
       return cursor.getString(index);
    }

    public static void putName(ContentValues values, String name){
        values.put(NAME, name);
    }

    public static Long getId(Cursor cursor){
        int index = cursor.getColumnIndexOrThrow(ID);
        return cursor.getLong(index);
    }

    public static void putId(ContentValues values, Long id){
        values.put(ID, id);
    }
}
