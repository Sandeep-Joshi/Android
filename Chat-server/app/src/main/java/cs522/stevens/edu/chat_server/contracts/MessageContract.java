package cs522.stevens.edu.chat_server.contracts;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import edu.stevens.cs522.chat.oneway.server.provider.chatProvider;

/**
 * Created by Sandeep Joshi on 2/23/2015.
 */
public class MessageContract {
    public static final String ID ="_id";
    public static final String MESSAGE_TEXT="messageText";
    public static final String SENDER="sender";
    public final static String PEER_FK = "peer_fk";

    public static String AUTHORITY = "edu.stevens.cs522.chatapp";
    public static String PATH = "/"+ chatProvider.MESSAGES_TABLE;
    public static String URL = "content://"+AUTHORITY+PATH;
    public static Uri CONTENT_URI = Uri.parse(URL);

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
        return cursor.getLong(cursor.getColumnIndexOrThrow(PEER_FK));
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
}