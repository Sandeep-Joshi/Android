package cs522.stevens.edu.chatapp2;

/**
 * Created by Sandeep on 3/15/2015.
 */
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class MessageContract {

    public final static String ID =  "_id";
    public final static String MESSAGE_TEXT = "MESSAGE_TEXT";
    public final static String SENDER = "SENDER";
    public final static String PEER_FK = "peer_fk";

    public static String AUTHORITY = "cs522.stevens.edu.chatapp2";
    public static String PATH = "/"+ChatProvider.MESSAGES_TABLE;
    public static String URL = "content://"+AUTHORITY+PATH;
    public static Uri CONTENT_URI = Uri.parse(URL);


    public static String getMessageText(Cursor cursor) {
        int colIndex = cursor.getColumnIndexOrThrow(MESSAGE_TEXT);
        return cursor.getString(colIndex);
    }

    public static void putMessageText(ContentValues values, String MessageText) {
        values.put(MESSAGE_TEXT, MessageText);
    }
    public static String getSender(Cursor cursor) {
        int colIndex = cursor.getColumnIndexOrThrow(SENDER);
        return cursor.getString(colIndex);
    }

    public static void putSender(ContentValues values, String Sender) {
        values.put(SENDER, Sender);
    }
    public static String getId(Cursor cursor) {
        int colIndex = cursor.getColumnIndexOrThrow(ID);
        return cursor.getString(colIndex);
    }

    public static void putId(ContentValues values, String id) {
        values.put(ID, id);
    }
}

