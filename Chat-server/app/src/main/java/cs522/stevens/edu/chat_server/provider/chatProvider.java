package cs522.stevens.edu.chat_server.provider;

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

import java.util.HashMap;

import edu.stevens.cs522.chat.oneway.server.contracts.MessageContract;
import edu.stevens.cs522.chat.oneway.server.contracts.PeerContract;

/**
 * Created by Sandeep on 3/14/2015.
 */
public class chatProvider extends ContentProvider {
    public static String DATABASE_NAME = "chatProvider.db";
    public static String MESSAGES_TABLE = "messages";
    public static String PEERS_TABLE = "peers";
    public static String MESSAGES_TABLE_INDEX = "MessagesBookIndex";
    public static int DATABASE_VERSION = 5;

    public static String DATABASE_CREATE_PEERS = "create table " + PEERS_TABLE
            + "( " + PeerContract.ID + " integer primary key autoincrement, "
            + PeerContract.NAME + " text not null," + PeerContract.ADDRESS
            + " text not null," + PeerContract.PORT + " text not null);";

    public static String DATABASE_CREATE_MESSAGES = "create table "
            + MESSAGES_TABLE + "( " + MessageContract.ID
            + " integer primary key autoincrement, "
            + MessageContract.MESSAGE_TEXT + " text not null,"
            + MessageContract.SENDER + " text not null,"
            + MessageContract.PEER_FK + " integer," + "FOREIGN KEY ("
            + MessageContract.PEER_FK + ") REFERENCES " + PEERS_TABLE
            + "("+PeerContract.ID+") ON DELETE CASCADE);";

    public static String INDEX_SCRIPT = "CREATE INDEX " + MESSAGES_TABLE_INDEX
            + " ON " + MESSAGES_TABLE + "(" + MessageContract.PEER_FK + ");";

    public static String DATABASE_UPGRADE = "DROP TABLE IF EXISTS";
    private SQLiteDatabase db;

    private static final int ALL_ROWS = 1;
    private static final int SINGLE_ROW = 2;
    private static final int ALL_ROWS_M = 3;
    private static final int SINGLE_ROW_M = 4;

    private static HashMap<String, String> PROJECTION_MAP;

    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PeerContract.AUTHORITY, PEERS_TABLE, ALL_ROWS);
        uriMatcher.addURI(PeerContract.AUTHORITY, PEERS_TABLE + "/#",
                SINGLE_ROW);
        uriMatcher.addURI(PeerContract.AUTHORITY, MESSAGES_TABLE, ALL_ROWS_M);
        uriMatcher.addURI(PeerContract.AUTHORITY, MESSAGES_TABLE + "/#",
                SINGLE_ROW_M);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE_PEERS);
            db.execSQL(DATABASE_CREATE_MESSAGES);
            db.execSQL(INDEX_SCRIPT);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(DATABASE_UPGRADE + " " + PEERS_TABLE);
            db.execSQL(DATABASE_UPGRADE + " " + MESSAGES_TABLE);
            onCreate(db);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)) {
            case ALL_ROWS:
            case ALL_ROWS_M:
                count = db.delete(MESSAGES_TABLE, selection, selectionArgs);
                db.delete(PEERS_TABLE, selection, selectionArgs);
                break;
            case SINGLE_ROW:
            case SINGLE_ROW_M:
                String id = uri.getPathSegments().get(1);
                count = db.delete(MESSAGES_TABLE, MessageContract.PEER_FK
                        + " = "
                        + id
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection
                        + ')' : ""), selectionArgs);
                db.delete(
                        PEERS_TABLE,
                        PeerContract.ID
                                + " = "
                                + id
                                + (!TextUtils.isEmpty(selection) ? " AND ("
                                + selection + ')' : ""), selectionArgs);
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
                        + PEERS_TABLE;
            case ALL_ROWS_M:
                return "vnd.android.cursor/vnd." + MessageContract.AUTHORITY
                        + "." + MESSAGES_TABLE;
            case SINGLE_ROW:
                return "vnd.android.cursor/vnd.edu.stevens.cs522.chatoneway.peers";
            case SINGLE_ROW_M:
                return "vnd.android.cursor/vnd.edu.stevens.cs522.chatoneway.messages";
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
                rowID = db.insert(PEERS_TABLE, "", values);
                if (rowID > 0)
                    newUri = ContentUris.withAppendedId(PeerContract.CONTENT_URI,
                            rowID);
                break;
            case ALL_ROWS_M:
            case SINGLE_ROW_M:
                rowID = db.insert(MESSAGES_TABLE, "", values);
                if (rowID > 0)
                    newUri = ContentUris.withAppendedId(MessageContract.CONTENT_URI,
                            rowID);
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
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (uriMatcher.match(uri)) {
            case ALL_ROWS:
                qb.setTables(PEERS_TABLE);
                qb.setProjectionMap(PROJECTION_MAP);
                break;
            case ALL_ROWS_M:
                qb.setTables(MESSAGES_TABLE);
                qb.setProjectionMap(PROJECTION_MAP);
                break;
            case SINGLE_ROW:
                qb.setTables(PEERS_TABLE);
                qb.appendWhere(MessageContract.ID + "="
                        + uri.getPathSegments().get(1));
                break;
            case SINGLE_ROW_M:
                qb.setTables(MESSAGES_TABLE);
                qb.appendWhere(MessageContract.ID + "="
                        + uri.getPathSegments().get(1));
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (sortOrder == null || sortOrder == "") {
            sortOrder = MessageContract.ID;
        }

        Cursor c = qb.query(db, projection, selection, selectionArgs, null,
                null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)) {
            case ALL_ROWS:
                    count = db.update(PEERS_TABLE, values, selection, selectionArgs);
                break;
            case ALL_ROWS_M:
                count = db.update(MESSAGES_TABLE, values, selection, selectionArgs);
                break;
            case SINGLE_ROW:
                count = db.update(PEERS_TABLE, values, PeerContract.ID + " = "
                                    + uri.getPathSegments().get(1)
                                    + (!TextUtils.isEmpty(selection)? " AND ("
                                    + selection + ')' : ""), selectionArgs);
                break;
            case SINGLE_ROW_M:
                count = db.update(
                        MESSAGES_TABLE,
                        values,
                        MessageContract.PEER_FK + " = "
                                + uri.getPathSegments().get(1)
                                + (!TextUtils.isEmpty(selection) ? " AND ("
                                + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
