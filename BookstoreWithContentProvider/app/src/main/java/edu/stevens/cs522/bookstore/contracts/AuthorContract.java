package edu.stevens.cs522.bookstore.contracts;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import edu.stevens.cs522.bookstore.databases.CartDbAdapter;

/**
 * Created by Sandeep Joshi on 2/13/2015.
 */
public class AuthorContract {

    public final static String _id          =  "_id";
    public final static String _first_name  = "_first_name";
    public final static String _middle_name = "_middle_name";
    public final static String _last_name   = "_last_name";
    public final static String _book_fk     = "_book_fk";
    public final static String _authors     = "_authors";

    public static final String auth = "edu.stevens.cs522.bookstore";
    public static final String path = "/"+ CartDbAdapter._AUTHOR_DB;
    public static Uri CONTENT_URI = ContentUri(auth,path);

    public static Uri ContentUri(String authority, String path) {
        return new Uri.Builder().scheme("content").authority(authority).path(path).build();
    }

    public static Uri ContentUri(String id) {
        return withExtendedPath(CONTENT_URI,id);
    }
    public static Uri withExtendedPath(Uri uri, String path){
        Uri.Builder builder = uri.buildUpon();
        for(String p : path.split("\\ ",-1))
            builder.appendPath(p);
        return builder.build();
    }

    public Long getId(Uri uri){
        return Long.parseLong(uri.getLastPathSegment());
    }

    public static final String CONTENT_PATH = CONTENT_PATH(CONTENT_URI);
    public static final String CONTENT_PATH_ITEM = CONTENT_PATH(ContentUri("#"));
    public static String CONTENT_PATH(Uri uri){
        return uri.getPath().substring(1);
    }

    public static int getId(Cursor cursor) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(_id));
    }

    public static void putId(ContentValues values, int id) {
        values.put(_id, id);
    }

    public static int getFk(Cursor cursor) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(_book_fk));
    }

    public static void putFk(ContentValues values, int fk) {
        values.put(_book_fk, fk);
    }

    public static String getFirstName(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(_first_name));
    }

    public static void putFirstName(ContentValues values, String FirstName) {
        values.put(_first_name, FirstName);
    }
    public static String getMiddleName(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(_middle_name));
    }

    public static void putMiddleName(ContentValues values, String MiddleName) {
        values.put(_middle_name, MiddleName);
    }
    public static String getLastName(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(_last_name));
    }

    public static void putLastName(ContentValues values, String LastName) {
        values.put(_last_name, LastName);
    }
    public static String getAllauthors(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(_authors));
    }
}

