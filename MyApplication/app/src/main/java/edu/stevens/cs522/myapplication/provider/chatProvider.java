package edu.stevens.cs522.myapplication.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;

import edu.stevens.cs522.myapplication.contracts.ChatRoomContract;
import edu.stevens.cs522.myapplication.contracts.MessageContract;
import edu.stevens.cs522.myapplication.contracts.PeerContract;

/**
 * Created by Sandeep on 3/14/2015.
 */
public class chatProvider extends ContentProvider {
    public static String DATABASE_NAME = "chatProvider11.db";
    public static String MESSAGES_TABLE_INDEX = "MessagesBookIndex";
    public static String MESSAGES_CHATROOM_INDEX = "MessagesChatroomIndex";
    public static final String PEER_MESSAGES = "peer_messages_view";
    public static final String CHAT_MESSAGES = "chat_messages_view";

    public static int DATABASE_VERSION = 5;

    public static String DATABASE_CREATE_PEERS = "create table " + PeerContract.PEERS_TABLE
                + "( " + PeerContract.ID + " integer primary key autoincrement, "
                + PeerContract.CLIENTID + " text,"
                + PeerContract.NAME + " text not null," + PeerContract.REGID + " text,"
                + PeerContract.ADDRESS + " text," + PeerContract.PORT + " text,"
                + PeerContract.LONGITUDE + " text," + PeerContract.LATITUDE + " text);";

    public static String DATABASE_CREATE_MESSAGES = "create table "
                + MessageContract.MESSAGES_TABLE + "( " + MessageContract.ID
                + " integer primary key autoincrement, "
                + MessageContract.MESSAGE_TEXT + " text not null,"
                + MessageContract.SENDER + " text not null,"
                + MessageContract.SEQ + " text,"
                + MessageContract.CHATROOM + " integer not null,"
                + MessageContract.TIMESTAMP + " text,"
                + MessageContract.LONGITUDE + " text," + MessageContract.LATITUDE + " text,"
                + MessageContract.PEER_FK + " integer ," + "FOREIGN KEY ("
                + MessageContract.PEER_FK + ") REFERENCES " + PeerContract.PEERS_TABLE
                + "("+PeerContract.ID+") ON DELETE CASCADE,"
                + "FOREIGN KEY (" + MessageContract.CHATROOM + ") REFERENCES " + ChatRoomContract.CHATROOM_TABLE
            + "("+ChatRoomContract.ID+") ON DELETE CASCADE);";

    public static String DATABASE_CREATE_CHATROOM = "create table "
                + ChatRoomContract.CHATROOM_TABLE + "( " + ChatRoomContract.ID + " integer primary key autoincrement, "
                + ChatRoomContract.NAME + " text not null);";

    public static String INDEX_SCRIPT = "CREATE INDEX " + MESSAGES_TABLE_INDEX
                + " ON " + MessageContract.MESSAGES_TABLE + "(" + MessageContract.PEER_FK + ");";

    public static String INDEX_CHATROOM_SCRIPT = "CREATE INDEX " + MESSAGES_CHATROOM_INDEX
            + " ON " + MessageContract.MESSAGES_TABLE + "(" + MessageContract.CHATROOM + ");";

    public static String CREATE_VIEW_PEER = "create view " + PEER_MESSAGES + " as select " + PeerContract.NAME + "," +
            MessageContract.MESSAGE_TEXT + "," + MessageContract.MESSAGES_TABLE + "." + MessageContract.LONGITUDE + "," +
            MessageContract.MESSAGES_TABLE + "." + MessageContract.LATITUDE + "," +
            PeerContract.PEERS_TABLE + "." + PeerContract.ID + " from " + PeerContract.PEERS_TABLE + " join " + MessageContract.MESSAGES_TABLE + " on " +
            PeerContract.PEERS_TABLE + "." + PeerContract.ID + "=" + MessageContract.MESSAGES_TABLE + "." + MessageContract.PEER_FK +";";

    public static String CREATE_VIEW_ROOM = "create view " + CHAT_MESSAGES + " as select " +
            MessageContract.MESSAGES_TABLE + "." + MessageContract.ID + "," +
            ChatRoomContract.CHATROOM_TABLE + "." + ChatRoomContract.NAME + "," +
            MessageContract.MESSAGES_TABLE + "." + MessageContract.MESSAGE_TEXT + "," +
            MessageContract.MESSAGES_TABLE + "." + MessageContract.LONGITUDE + "," +
            MessageContract.MESSAGES_TABLE + "." + MessageContract.LATITUDE + "," +
            MessageContract.MESSAGES_TABLE + "." + MessageContract.TIMESTAMP + "," +
            PeerContract.PEERS_TABLE + "." + PeerContract.NAME + " as peer " +
            " from " + ChatRoomContract.CHATROOM_TABLE + " join " + MessageContract.MESSAGES_TABLE +
            " on " + ChatRoomContract.CHATROOM_TABLE + "." + ChatRoomContract.ID + " = " +
            MessageContract.MESSAGES_TABLE + "." + MessageContract.CHATROOM + " join "+
            PeerContract.PEERS_TABLE + " on " + MessageContract.MESSAGES_TABLE + "." +
            MessageContract.PEER_FK + "=" + PeerContract.PEERS_TABLE + "." + PeerContract.ID  + ";";

    public static String DATABASE_UPGRADE = "DROP TABLE IF EXISTS";
    public static String VIEW_UPGRADE = "DROP VIEW IF EXISTS";
    private SQLiteDatabase db;
    private static HashMap<String, String> PROJECTION_MAP;

    private static final int ALL_ROWS = 1;
    private static final int SINGLE_ROW = 2;
    private static final int ALL_ROWS_M = 3;
    private static final int SINGLE_ROW_M = 4;
    private static final int ALL_ROWS_C = 5;
    private static final int SINGLE_ROW_C = 6;
    private static final int ALL_GROUP_CHATS = 7;

    //uri for message view
    Uri.Builder builder = new Uri.Builder();
    Uri viewUri = builder.authority(MessageContract.AUTHORITY).appendPath(CHAT_MESSAGES).build();

    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PeerContract.AUTHORITY, PeerContract.PEERS_TABLE, ALL_ROWS);
        uriMatcher.addURI(PeerContract.AUTHORITY, PeerContract.PEERS_TABLE + "/#", SINGLE_ROW);
        uriMatcher.addURI(MessageContract.AUTHORITY, MessageContract.MESSAGES_TABLE, ALL_ROWS_M);
        uriMatcher.addURI(MessageContract.AUTHORITY, MessageContract.MESSAGES_TABLE + "/#",SINGLE_ROW_M);
        uriMatcher.addURI(ChatRoomContract.AUTHORITY, ChatRoomContract.CHATROOM_TABLE, ALL_ROWS_C);
        uriMatcher.addURI(ChatRoomContract.AUTHORITY, ChatRoomContract.CHATROOM_TABLE + "/#",SINGLE_ROW_C);
        uriMatcher.addURI(MessageContract.AUTHORITY, CHAT_MESSAGES,ALL_GROUP_CHATS);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE_PEERS);
            db.execSQL(DATABASE_CREATE_MESSAGES);
            db.execSQL(DATABASE_CREATE_CHATROOM);
            db.execSQL(INDEX_SCRIPT);
            db.execSQL(INDEX_CHATROOM_SCRIPT);
            db.execSQL(CREATE_VIEW_PEER);
            db.execSQL(CREATE_VIEW_ROOM);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(DATABASE_UPGRADE + " " + PeerContract.PEERS_TABLE);
            db.execSQL(DATABASE_UPGRADE + " " + MessageContract.MESSAGES_TABLE);
            db.execSQL(DATABASE_UPGRADE + " " + ChatRoomContract.CHATROOM_TABLE);
            db.execSQL(VIEW_UPGRADE + " " + CHAT_MESSAGES);
            db.execSQL(VIEW_UPGRADE + " " + PEER_MESSAGES);
            onCreate(db);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)) {
            case ALL_ROWS:
                count = db.delete(PeerContract.PEERS_TABLE, selection, selectionArgs);
                break;
            case ALL_ROWS_M:
                count = db.delete(MessageContract.MESSAGES_TABLE, selection, selectionArgs);
                //db.delete(PEERS_TABLE, selection, selectionArgs);
                break;
            case ALL_ROWS_C:
                count = db.delete(ChatRoomContract.CHATROOM_TABLE, selection, selectionArgs);
                break;
            case SINGLE_ROW:
                String id1 = uri.getPathSegments().get(1);
                count = db.delete(PeerContract.PEERS_TABLE, PeerContract.ID + " = " + id1
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            case SINGLE_ROW_M:
                String id = uri.getPathSegments().get(1);
                count = db.delete(MessageContract.MESSAGES_TABLE, MessageContract.PEER_FK + " = " + id
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                db.delete(PeerContract.PEERS_TABLE, PeerContract.ID + " = " + id
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            case SINGLE_ROW_C:
                String id2 = uri.getPathSegments().get(1);
                count = db.delete(ChatRoomContract.CHATROOM_TABLE, ChatRoomContract.ID+ " = " + id2
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {

            case ALL_ROWS:
                return "vnd.android.cursor/vnd." + PeerContract.AUTHORITY + "."
                        + PeerContract.PEERS_TABLE;
            case ALL_ROWS_M:
                return "vnd.android.cursor/vnd." + MessageContract.AUTHORITY
                        + "." + MessageContract.MESSAGES_TABLE;
            case ALL_ROWS_C:
                return "vnd.android.cursor/vnd." + ChatRoomContract.AUTHORITY
                        + "." + ChatRoomContract.CHATROOM_TABLE;
            case SINGLE_ROW:
                return "vnd.android.cursor/vnd.edu.stevens.cs522.myapplication.peers";
            case SINGLE_ROW_M:
                return "vnd.android.cursor/vnd.edu.stevens.cs522.myapplication.messages";
            case SINGLE_ROW_C:
                return "vnd.android.cursor/vnd.edu.stevens.cs522.myapplication.chatroom";
            case ALL_GROUP_CHATS:
                return "vnd.android.cursor/vnd.edu.stevens.cs522.myapplication.messages";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO Auto-generated method stub
        long rowID = 0;
        Uri newUri = null;


        switch (uriMatcher.match(uri)){
            case ALL_ROWS:
            case SINGLE_ROW:
                rowID = db.insert(PeerContract.PEERS_TABLE, "", values);
                if (rowID > 0)
                    newUri = ContentUris.withAppendedId(PeerContract.CONTENT_URI, rowID);
                break;
            case ALL_ROWS_M:
            case SINGLE_ROW_M:
                rowID = db.insert(MessageContract.MESSAGES_TABLE, "", values);
                if (rowID > 0)
                    newUri = ContentUris.withAppendedId(MessageContract.CONTENT_URI, rowID);

                getContext().getContentResolver().notifyChange(newUri, null);
                getContext().getContentResolver().notifyChange(viewUri, null);

                break;
            case ALL_ROWS_C:
            case SINGLE_ROW_C:
                rowID = db.insert(ChatRoomContract.CHATROOM_TABLE, "", values);
                if (rowID > 0)
                    newUri = ContentUris.withAppendedId(ChatRoomContract.CONTENT_URI, rowID);
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        if (rowID == 0)
            throw new SQLException("Failed to add a record into " + uri);

        getContext().getContentResolver().notifyChange(newUri, null);
        return newUri;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        /**
         * Create a write able database which will trigger its creation if it
         * doesn't already exist.
         */
        db = dbHelper.getWritableDatabase();
        return (db == null) ? false : true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        int id = 0;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (uriMatcher.match(uri)) {
            case ALL_ROWS:
                qb.setTables(PeerContract.PEERS_TABLE);
                qb.setProjectionMap(PROJECTION_MAP);
                id = 1;
                break;
            case ALL_ROWS_M:
                qb.setTables(MessageContract.MESSAGES_TABLE);
                qb.setProjectionMap(PROJECTION_MAP);
                id = 2;
                break;
            case ALL_ROWS_C:
                qb.setTables(ChatRoomContract.CHATROOM_TABLE);
                qb.setProjectionMap(PROJECTION_MAP);
                id = 3;
                break;
            case SINGLE_ROW:
                qb.setTables(PeerContract.PEERS_TABLE);
                qb.appendWhere(PeerContract.ID + "=" + uri.getPathSegments().get(1));
                id = 1;
                break;
            case SINGLE_ROW_M:
                qb.setTables(MessageContract.MESSAGES_TABLE);
                qb.appendWhere(MessageContract.ID + "=" + uri.getPathSegments().get(1));
                id = 2;
                break;
            case SINGLE_ROW_C:
                qb.setTables(ChatRoomContract.CHATROOM_TABLE);
                qb.appendWhere(ChatRoomContract.ID + "=" + uri.getPathSegments().get(1));
                id = 3;
                break;
            case ALL_GROUP_CHATS:
                qb.setTables(CHAT_MESSAGES);
                qb.setProjectionMap(PROJECTION_MAP);
                id = 4;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (sortOrder == null || sortOrder == "") {
            switch (id) {
                case 1:
                    sortOrder = PeerContract.ID;
                    break;
                case 2:
                    sortOrder = MessageContract.ID;
                    break;
                case 3:
                    sortOrder = ChatRoomContract.ID;
                    break;
                case 4:
                    sortOrder = MessageContract.TIMESTAMP;
            }
            if (selectionArgs==null)
                selection = null;
            Cursor c = null;
            try {
                c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

                if (uriMatcher.match(uri)== ALL_GROUP_CHATS){
                    c.setNotificationUri(getContext().getContentResolver(), uri);

                }else{
                    c.setNotificationUri(getContext().getContentResolver(), uri);

                }
            }catch (RuntimeException ex){
                Log.e("buggy",qb.toString()+" "+db.toString()+" "+projection.toString()+" "+selection
                        +" "+selectionArgs.toString());
            }
            return c;
        } else
            return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)) {
            case ALL_ROWS:
                count = db.update(PeerContract.PEERS_TABLE, values, selection, selectionArgs);
                break;
            case ALL_ROWS_M:
                count = db.update(MessageContract.MESSAGES_TABLE, values, selection, selectionArgs);
                break;
            case ALL_ROWS_C:
                count = db.update(ChatRoomContract.CHATROOM_TABLE, values, selection, selectionArgs);
                break;
            case SINGLE_ROW:
                count = db.update(PeerContract.PEERS_TABLE, values, PeerContract.ID + " = "
                        + uri.getPathSegments().get(1) + (!TextUtils.isEmpty(selection)? " AND ("
                        + selection + ')' : ""), selectionArgs);
                break;
            case SINGLE_ROW_M:
                count = db.update(MessageContract.MESSAGES_TABLE, values, MessageContract.PEER_FK + " = "
                        + uri.getPathSegments().get(1) + (!TextUtils.isEmpty(selection) ? " AND ("
                        + selection + ')' : ""), selectionArgs);
                break;
            case SINGLE_ROW_C:
                count = db.update(ChatRoomContract.CHATROOM_TABLE, values, ChatRoomContract.ID + " = "
                        + uri.getPathSegments().get(1) + (!TextUtils.isEmpty(selection) ? " AND ("
                        + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
