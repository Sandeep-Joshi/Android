package edu.stevens.cs522.bookstore.providers;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import edu.stevens.cs522.bookstore.R;
import edu.stevens.cs522.bookstore.contracts.AuthorContract;
import edu.stevens.cs522.bookstore.contracts.BookContract;
import edu.stevens.cs522.bookstore.databases.CartDbAdapter;
import edu.stevens.cs522.bookstore.entities.Author;
import edu.stevens.cs522.bookstore.entities.Book;

/**
 * Created by Sandeep Joshi on 3/5/2015.
 */
public class BookProvider extends ContentProvider {


    public static String _DATABASE_NAME = "bookStore.db";
    public static String _BOOK_DB = "Books";
    public static String _AUTHOR_DB = "Authors";
    public static int _VERSION = 1;  //to update the db
    public static String _INDEX = "AuthorBookIndex";

    //To create Book table
    public static String _CREATE_BOOK = "CREATE TABLE " + _BOOK_DB + " ("
            + BookContract._id + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + BookContract._title + " TEXT NOT NULL, "
            + BookContract._isbn + " TEXT, "
            + BookContract._price + " TEXT NOT NULL);";

    //To create Author table
    public static String _CREATE_AUTHOR = "CREATE TABLE " + _AUTHOR_DB + " ("  //IF NOT EXISTS
            + AuthorContract._id + " INTEGER PRIMARY KEY AUTOINCREMENT, "  //IDENTITY[(1,1)],
            + AuthorContract._book_fk + " INTEGER NOT NULL, "
            + AuthorContract._first_name + " TEXT NOT NULL, "
            + AuthorContract._middle_name + " TEXT, "
            + AuthorContract._last_name + " TEXT, "
            + "FOREIGN KEY (" + AuthorContract._book_fk + ") " +
            "REFERENCES " + _BOOK_DB + "(" + BookContract._id + ") ON DELETE CASCADE);";

    //To create secondary index in Author _book__fk column
    public static String _CREATE_INDEX = "CREATE INDEX "
            + _INDEX + " ON "
            + _AUTHOR_DB + "(" + AuthorContract._book_fk + ");";

    //Join query
    public static String _JOIN = "SELECT " + _BOOK_DB + "." + BookContract._id + ", " +
            _BOOK_DB + "." + BookContract._title + ", " +
            _BOOK_DB + "." + BookContract._isbn + ", " +
            _BOOK_DB + "." + BookContract._price + ", " +
            _AUTHOR_DB + "." + AuthorContract._first_name + ", " +
            _AUTHOR_DB + "." + AuthorContract._middle_name + ", " +
            _AUTHOR_DB + "." + AuthorContract._last_name + ", " +
            " GROUP_CONCAT((" + _AUTHOR_DB + "." + AuthorContract._first_name + "||" + "' '" + "||" +
            _AUTHOR_DB + "." + AuthorContract._middle_name + "||" + "' '" + "||" +
            _AUTHOR_DB + "." + AuthorContract._last_name + "),'|')" + " AS " +
            AuthorContract._authors +
            " FROM " + _BOOK_DB + " LEFT OUTER JOIN " + _AUTHOR_DB + " ON " +
            _BOOK_DB + "." + BookContract._id + " = " + _AUTHOR_DB + "." + AuthorContract._book_fk
            + " GROUP BY " + AuthorContract._book_fk;
    // + _BOOK_DB + "." + BookContract._title + "=?";
    //   + " GROUP BY " + _BOOK_DB + "." + BookContract._id  + "," + _BOOK_DB + "." + BookContract._title;

    public static String _GET_ID = "SELECT last_insert_rowid() FROM " + _BOOK_DB;

    public static final char SEPARATOR_CHAR = '|';


    private SQLiteDatabase db2;
    private DatabaseHelper dbHelper;

    private static final Pattern SEPARATOR =
            Pattern.compile(Character.toString(SEPARATOR_CHAR), Pattern.LITERAL);

    public static String[] readStringArray(String in) {
        return SEPARATOR.split(in);
    }


    //Turn on foreign key constraint in each DB connection
    public static String _onForeignKey = "PRAGMA FOREIGN_KEYS = ON";

    //To drop tables for upgrade
    public static String _DROP = "DROP TABLE IF EXISTS ";

    public static enum parameter {id, title, name, isbn};

    public static enum dbMode {read, write};


    // Create the constants used to differentiate  
    // between the different URI  requests. 
    private static final int ALL_ROWS = 1;
    private static final int SINGLE_ROW = 2;
    private static final int ALL_ROWS_AUTH = 3;
    private static final int SINGLE_ROW_AUTH = 4;

    private final static UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(BookContract.auth, BookContract.CONTENT_PATH, ALL_ROWS);
        uriMatcher.addURI(BookContract.auth, BookContract.CONTENT_PATH_ITEM, SINGLE_ROW);
        uriMatcher.addURI(AuthorContract.auth, AuthorContract.CONTENT_PATH, ALL_ROWS_AUTH);
        uriMatcher.addURI(AuthorContract.auth, AuthorContract.CONTENT_PATH_ITEM, SINGLE_ROW_AUTH);
    }

    public boolean onCreate() {
        DatabaseHelper dbhelper = new DatabaseHelper(getContext(), _DATABASE_NAME, null, _VERSION);
        db2 = dbhelper.getWritableDatabase();
        return (db2 != null) ? true : false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selArg, String sort) {
        SQLiteQueryBuilder db = new SQLiteQueryBuilder();
        String sel;
        Cursor c;

        switch (uriMatcher.match(uri)) {
            case ALL_ROWS:
                sel = _JOIN + " ;";
                c = db2.rawQuery(sel, null);
                break;
            case SINGLE_ROW:
                sel = _JOIN + " WHERE " + _BOOK_DB + "." + BookContract._id + "=?";
                String[] selArgs = {Long.toString(BookContract.getId(uri))};
                c = db2.rawQuery(sel, selArgs);
                break;

 /*         case ALL_ROWS_AUTH:
                break;

            case SINGLE_ROW_AUTH:
                break;*/
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);

        }

        if (c != null) {
            //c.moveToFirst();
            c.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case ALL_ROWS:
                return BookContract.contentType("Book");
            case SINGLE_ROW:
                return BookContract.contentItemType("Book");
            case ALL_ROWS_AUTH:
                return BookContract.contentType("Author");
            case SINGLE_ROW_AUTH:
                return BookContract.contentItemType("Author");
            default:
                throw new IllegalArgumentException("Unsupported type: " + uri);
        }
    }

     @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri instanceUri = null;
        long row;
        switch (uriMatcher.match(uri)) {
            case ALL_ROWS:
                row = db2.insert(_BOOK_DB, null, values);
                if (row > 0) {
                    /*instanceUri = BookContract.ContentUri(Long.toString(row));
                    ContentResolver cr = getContext().getContentResolver();
                    cr.notifyChange(instanceUri, null);*/
                    instanceUri = ContentUris.withAppendedId(BookContract.CONTENT_URI, row);
                }
                break;

            case ALL_ROWS_AUTH:
                row = db2.insert(_AUTHOR_DB, null, values);
                if (row > 0) {
                    /*instanceUri = AuthorContract.ContentUri(Long.toString(row));
                    ContentResolver cr = getContext().getContentResolver();
                    cr.notifyChange(instanceUri, null);*/
                    instanceUri = ContentUris.withAppendedId(AuthorContract.CONTENT_URI, row);

                }
                break;

            default:
                Log.e("insert",uri.toString());

                instanceUri = null;
                break;

        }
         getContext().getContentResolver().notifyChange(instanceUri, null);
         return instanceUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)) {
            case ALL_ROWS:
                db2.delete(_AUTHOR_DB, null, null);
                count = db2.delete(_BOOK_DB, null, null);
                break;
            case SINGLE_ROW:
                String id = Long.toString(BookContract.getId(uri));//uri.getPathSegments().get(1);
                count = db2.delete(_BOOK_DB, BookContract._id + " = " + id
                        + (!TextUtils.isEmpty(selection)?" AND (" + selection
                        + ')' : ""), selectionArgs);
                db2.delete(_AUTHOR_DB, AuthorContract._book_fk + " = " + id
                        + (!TextUtils.isEmpty(selection)?" AND (" + selection
                        + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case ALL_ROWS:
                count = db2.update(_BOOK_DB, values, selection, selectionArgs);
                break;
            case SINGLE_ROW:
                count = db2.update(_BOOK_DB, values, BookContract._id + " = " + Long.toString(BookContract.getId(uri))
                                + (!TextUtils.isEmpty(selection)?" AND (" + selection + ')':""),

                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        private final Context Pcontext;

        public DatabaseHelper(Context context, String dbname, SQLiteDatabase.CursorFactory obj, int version) {
            super(context, dbname, obj, version);
            Pcontext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(_CREATE_BOOK);
            sqLiteDatabase.execSQL(_CREATE_AUTHOR);
            sqLiteDatabase.execSQL(_CREATE_INDEX);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
            //logging
            String s = Pcontext.getResources().getString(R.string.log_upg);
            s = String.format(s, i, i2);
            Log.w("TaskDBAdapter", s);
            sqLiteDatabase.execSQL(_DROP + _DATABASE_NAME + "." + _BOOK_DB);
            sqLiteDatabase.execSQL(_DROP + _DATABASE_NAME + "." + _AUTHOR_DB); //implicit delete
            //indices should be deleted by itself so we need to create it once again
            onCreate(sqLiteDatabase);
        }
    }
}