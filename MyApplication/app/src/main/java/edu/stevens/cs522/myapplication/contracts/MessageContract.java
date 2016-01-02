package edu.stevens.cs522.myapplication.contracts;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import edu.stevens.cs522.myapplication.provider.chatProvider;

/**
 * Created by Sandeep Joshi on 2/23/2015.
 */
public class MessageContract {
    public static final String ID ="_id";
    public static final String MESSAGE_TEXT="messageText";
    public static final String SENDER="sender";
    public final static String SEQ = "seq";
    public final static String TIMESTAMP = "timestamp";
    public final static String CHATROOM = "chatroom";
    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";
    public final static String PEER_FK = "peer_fk";
    public static String MESSAGES_TABLE = "messages";

    public static String AUTHORITY = "edu.stevens.cs522.myapplication";
    public static String PATH = "/"+ MESSAGES_TABLE;
    public static String PATH2 = "/"+ chatProvider.CHAT_MESSAGES;
    public static String URL = "content://"+AUTHORITY+PATH;
    public static String URL2 = "content://"+AUTHORITY+PATH2;
    public static Uri CONTENT_URI = Uri.parse(URL);
    public static Uri CONTENT_URI2 = Uri.parse(URL2);

    public static Long getId(Cursor cursor) {
        return cursor.getLong(cursor.getColumnIndexOrThrow(ID));
    }
    public static String getMessageText(Cursor cursor){
        return cursor.getString(cursor.getColumnIndexOrThrow(MESSAGE_TEXT));
    }
    public static String getSender(Cursor cursor){
        return cursor.getString(cursor.getColumnIndexOrThrow(SENDER));
    }
    public static Long getPeerFk(Cursor cursor){
        int colIndex = cursor.getColumnIndexOrThrow(SEQ);
        return cursor.getLong(colIndex);
    }

    public static String getTimestamp(Cursor cursor) {
        int colIndex = cursor.getColumnIndexOrThrow(TIMESTAMP);
        return cursor.getString(colIndex);
    }

    public static void putTimestamp(ContentValues values, String timestamp) {
        values.put(TIMESTAMP, timestamp);
    }
    public static Long getChatroom(Cursor cursor) {
        int colIndex = cursor.getColumnIndexOrThrow(CHATROOM);
        return cursor.getLong(colIndex);
    }

    public static void putChatroom(ContentValues values, Long chatroom) {
        values.put(CHATROOM, chatroom);
    }

    public static Long getSeq(Cursor cursor) {
        int colIndex = cursor.getColumnIndexOrThrow(SEQ);
        return cursor.getLong(colIndex);
    }

    public static void putSeq(ContentValues values, String seq) {
        values.put(SEQ, seq);
    }

    public static void putId(ContentValues values,Long id){
        values.put(ID,id);
    }
    public static void putPeerFk(ContentValues values,Long id){
        values.put(PEER_FK,id);
    }
    public static void putMessageText(ContentValues values,String messageText){
        values.put(MESSAGE_TEXT,messageText);
    }
    public static void putSender(ContentValues values,String sender){
        values.put(SENDER,sender);
    }
    public static double getLongitude(Cursor cursor){
        return cursor.getDouble(cursor.getColumnIndexOrThrow(LONGITUDE));
    }
    public static double getLatitude(Cursor cursor){
        return cursor.getDouble(cursor.getColumnIndexOrThrow(LATITUDE));
    }
    public static void putLatitude(ContentValues values,double latitude){
        values.put(LATITUDE,latitude);
    }
    public static void putLongitude(ContentValues values,double longitude){
        values.put(LONGITUDE,longitude);
    }
}