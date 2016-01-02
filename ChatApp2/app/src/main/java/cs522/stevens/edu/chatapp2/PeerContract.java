package cs522.stevens.edu.chatapp2;

/**
 * Created by Sandeep on 3/15/2015.
 */
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class PeerContract {

    public final static String ID =  "_id";
    public final static int ID_KEY = 0;

    public final static String NAME = "NAME";
    public final static int NAME_KEY = 1;

    public final static String ADDRESS = "ADDRESS";
    public final static int ADDRESS_KEY = 2;

    public final static String PORT = "PORT";
    public final static int ID_PORT = 3;

    public static String AUTHORITY = "cs522.stevens.edu.chatapp2";
    public static String PATH = "/"+ChatProvider.PEERS_TABLE;
    public static String URL = "content://"+AUTHORITY+PATH;
    public static Uri CONTENT_URI = Uri.parse(URL);

    public static String getName(Cursor cursor) {
        int colIndex = cursor.getColumnIndexOrThrow(NAME);
        return cursor.getString(colIndex);
    }

    public static void putName(ContentValues values, String Name) {
        values.put(NAME, Name);
    }
    public static String getAddress(Cursor cursor) {
        int colIndex = cursor.getColumnIndexOrThrow(ADDRESS);
        return cursor.getString(colIndex);
    }

    public static void putAddress(ContentValues values, String Address) {
        values.put(ADDRESS, Address);
    }
    public static String getPort(Cursor cursor) {
        int colIndex = cursor.getColumnIndexOrThrow(PORT);
        return cursor.getString(colIndex);
    }

    public static void putPort(ContentValues values, String Port) {
        values.put(PORT, Port);
    }
    public static String getId(Cursor cursor) {
        int colIndex = cursor.getColumnIndexOrThrow(ID);
        return cursor.getString(colIndex);
    }

    public static void putId(ContentValues values, String id) {
        values.put(ID, id);
    }
}

