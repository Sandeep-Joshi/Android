package edu.stevens.cs522.bookstore.contracts;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import java.util.StringTokenizer;

import edu.stevens.cs522.bookstore.databases.CartDbAdapter;
import edu.stevens.cs522.bookstore.providers.BookProvider;

/**
 * Created by Sandeep Joshi on 2/13/2015.
 */
public class BookContract {

    public static final String _id    = "_id";
    public static final String _title = "_title";
    public static final String _isbn  = "_isbn";
    public static final String _price = "_price";

    public static final String auth = "edu.stevens.cs522.bookstore";
    public static final String path = "/"+ CartDbAdapter._BOOK_DB;
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

    public static Long getId(Uri uri){
        return Long.parseLong(uri.getLastPathSegment());
    }

    public static final String CONTENT_PATH = CONTENT_PATH(CONTENT_URI);
    public static final String CONTENT_PATH_ITEM = CONTENT_PATH(ContentUri("#"));
    public static String CONTENT_PATH(Uri uri){
        return uri.getPath().substring(1);
    }

    public static String contentType(String content){
        return "vnd.android.cursor/vnd." + auth + "." + content + "s";
    }

    public static String contentItemType(String content){
        return "vnd.android.cursor.item/vnd." + auth + "." + content;
    }

    public static int getId(Cursor cursor){
        return cursor.getInt(cursor.getColumnIndexOrThrow(_id));
    }

    public static void putId(ContentValues values, int id){
        values.put(_id, id);
    }

    public static String getTitle(Cursor cursor){
        return cursor.getString(cursor.getColumnIndexOrThrow(_title));
    }

    public static void putTitle(ContentValues values, String title){
        values.put(_title, title);
    }

    public static String getIsbn(Cursor cursor){
        return cursor.getString(cursor.getColumnIndexOrThrow(_isbn));
    }

    public static void putIsbn(ContentValues values, String isbn){
        values.put(_isbn, isbn);
    }

    public static String getPrice(Cursor cursor){
        return cursor.getString(cursor.getColumnIndexOrThrow(_price));
    }

    public static void putPrice(ContentValues values, String price){
        values.put(_price, price);
    }

}
