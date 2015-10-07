package edu.stevens.cs522.chat.oneway.server.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import edu.stevens.cs522.chat.oneway.server.contracts.PeerContract;
import edu.stevens.cs522.chat.oneway.server.entity.Peer;


/**
 * Created by Sandeep Joshi on 2/23/2015.
 */
public class CartDbAdapter {
    private static final String PEERS_TABLE   = "peers";
    private static final String ID            = "_id";
    private static final String ID1           = "id";
    private static final String NAME          = "name";
    private static final String ADDRESS       = "address";
    private static final String PORT          = "port";
    private static final String MESSAGE_TABLE = "messages";
    private static final String MESSAGE_TEXT  = "messageText";
    private static final String SENDER        = "sender";
    private static final String PEER_FK       = "peer_fk";
    private static final String PEER_MESSAGES = "peer_messages_view";
    private static final String DATABASE_NAME = "peers.db";
    private static final int DATABASE_VERSION = 3;

    private static final String DATABASE_CREATE = "create table " + PEERS_TABLE + " (" + ID + " integer primary key autoincrement," +
            NAME + " text not null," + ADDRESS + " text not null," + PORT + " integer);";

    private static final String DATABASE_CREATE1 = "create table " + MESSAGE_TABLE + " (" + ID1 +
            " integer primary key autoincrement," + MESSAGE_TEXT + " text," + SENDER + " text," +
            PEER_FK + " integer, foreign key (" + PEER_FK + ") references " + PEERS_TABLE +
            "(" + ID + ") on delete cascade);";

    public static String INDEX = "Create index MessagesPeerIndex on "+ MESSAGE_TABLE + "(" + PEER_FK + ");";

    public static String CREATE_VIEW = "create view " + PEER_MESSAGES + " as select " + NAME + "," +
            MESSAGE_TEXT + "," + ID + " from " + PEERS_TABLE + " join " + MESSAGE_TABLE + " on " +
            PEERS_TABLE + "." + ID + "=" + MESSAGE_TABLE + "." + PEER_FK +";";

    public static String row_id = "select last_insert_rowid() from "+ PEERS_TABLE;

    private SQLiteDatabase db1;
    private Context context;
    DatabaseHelper dbHelper;
    Cursor c;

    public CartDbAdapter(Context _context){
        context = _context;
        dbHelper = new DatabaseHelper(context);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context c){
            super(c,DATABASE_NAME,null,DATABASE_VERSION);
        }
        public void onCreate(SQLiteDatabase database) {
            database.execSQL(DATABASE_CREATE);
            database.execSQL(DATABASE_CREATE1);
            database.execSQL(CREATE_VIEW);
        }

        public void onUpgrade(SQLiteDatabase database,int oldVersion,int newVersion){
            Log.w(DatabaseHelper.class.getName(), "Upgrading database from version " + oldVersion + " to "
                            + newVersion + ", which will destroy all old data");
            database.execSQL("DROP TABLE IF EXISTS "+PEERS_TABLE);
            database.execSQL("DROP TABLE IF EXISTS "+MESSAGE_TABLE);
            database.execSQL("DROP VIEW IF EXISTS "+PEER_MESSAGES);
            onCreate(database);
        }

    }

    public CartDbAdapter open() throws SQLException {
        db1=dbHelper.getWritableDatabase();
        db1.execSQL("PRAGMA foreign_keys=ON;");
        return this;
    }


    final static public String TAG = CartDbAdapter.class.getCanonicalName();
    public Cursor fetchAllMessages(){
        String []ch =  {NAME,MESSAGE_TEXT};
        Log.e(TAG,PEER_MESSAGES+ch[0]+" "+ch[1]+" ");
        return db1.query(PEER_MESSAGES,new String[]{NAME,MESSAGE_TEXT,ID},null,null,null,null,null);
    }

    public void persist(Peer peer,String message){
        ContentValues contentValues = new ContentValues();
        contentValues.put(NAME,peer.name);
        contentValues.put(ADDRESS,peer.address.toString());
        contentValues.put(PORT,peer.port);
        db1.insert(PEERS_TABLE,null,contentValues);
        contentValues.clear();
        Cursor c = db1.rawQuery(row_id,null);
        c.moveToFirst();
        contentValues.put(MESSAGE_TEXT,message);
        contentValues.put(SENDER,peer.name);
        contentValues.put(PEER_FK,c.getInt(0));
        db1.insert(MESSAGE_TABLE,null,contentValues);

    }

    public Peer fetchName(String name){
        Cursor cur = db1.query(PEERS_TABLE,new String[]{PeerContract.ID,PeerContract.NAME,PeerContract.ADDRESS,PeerContract.PORT},
                PeerContract.NAME+"=?",new String[]{name},null,null,null);
        if(cur!=null)
            cur.moveToFirst();
        Peer peer = new Peer(cur);
        return peer;

    }

    public void close(){
        db1.close();
    }

}
