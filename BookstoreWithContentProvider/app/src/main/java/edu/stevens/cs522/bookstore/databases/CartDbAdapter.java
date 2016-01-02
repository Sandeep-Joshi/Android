package edu.stevens.cs522.bookstore.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import edu.stevens.cs522.bookstore.R;
import edu.stevens.cs522.bookstore.contracts.AuthorContract;
import edu.stevens.cs522.bookstore.contracts.BookContract;
import edu.stevens.cs522.bookstore.entities.Author;
import edu.stevens.cs522.bookstore.entities.Book;
import edu.stevens.cs522.bookstore.providers.BookProvider;

/**
 * Created by Sandeep Joshi on 2/13/2015.
 */
public class CartDbAdapter {

    public static String _DATABASE_NAME      = "bookStore.db";
    public static String _BOOK_DB            = "Books";
    public static String _AUTHOR_DB          = "Authors";
    public static int    _VERSION            = 1;  //to update the db
    public static String _INDEX              = "AuthorBookIndex";


    //Db operations using literal

    //To create Book table
    public static String _CREATE_BOOK = "CREATE TABLE " + _BOOK_DB + " ("
            + BookContract._id    + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + BookContract._title + " TEXT NOT NULL, "
            + BookContract._isbn  + " TEXT, "
            + BookContract._price + " TEXT NOT NULL);";

    //To create Author table
    public static String _CREATE_AUTHOR =  "CREATE TABLE " + _AUTHOR_DB + " ("  //IF NOT EXISTS
            + AuthorContract._id          + " INTEGER PRIMARY KEY AUTOINCREMENT, "  //IDENTITY[(1,1)],
            + AuthorContract._book_fk     + " INTEGER NOT NULL, "
            + AuthorContract._first_name  + " TEXT NOT NULL, "
            + AuthorContract._middle_name + " TEXT, "
            + AuthorContract._last_name   + " TEXT, "
            + "FOREIGN KEY ("+ AuthorContract._book_fk+") " +
            "REFERENCES "+_BOOK_DB+"("+BookContract._id+") ON DELETE CASCADE);";

    //To create secondary index in Author _book__fk column
    public static String _CREATE_INDEX = "CREATE INDEX "
            + _INDEX + " ON "
            + _AUTHOR_DB + "("+AuthorContract._book_fk+");";

    //Join query
    public static String _JOIN = "SELECT " + _BOOK_DB + "." + BookContract._id + ", " +
             _BOOK_DB + "." + BookContract._title + ", " +
             _BOOK_DB + "." + BookContract._isbn  + ", " +
             _BOOK_DB + "." + BookContract._price + ", " +
            _AUTHOR_DB + "." + AuthorContract._first_name + ", " +
            _AUTHOR_DB + "." + AuthorContract._middle_name + ", " +
            _AUTHOR_DB + "." + AuthorContract._last_name + ", " +
            " GROUP_CONCAT((" + _AUTHOR_DB + "." + AuthorContract._first_name+"||"+"' '"+"||"+
                                _AUTHOR_DB + "." + AuthorContract._middle_name+"||"+"' '"+"||"+
                                _AUTHOR_DB + "." + AuthorContract._last_name+"),'|')" + " AS " +
            AuthorContract._authors +
            " FROM " + _BOOK_DB + " LEFT OUTER JOIN " + _AUTHOR_DB + " ON " +
            _BOOK_DB + "." + BookContract._id  + " = " + _AUTHOR_DB + "." +  AuthorContract._book_fk
            + " GROUP BY " + AuthorContract._book_fk;
           // + _BOOK_DB + "." + BookContract._title + "=?";
       //   + " GROUP BY " + _BOOK_DB + "." + BookContract._id  + "," + _BOOK_DB + "." + BookContract._title;

    public static String _GET_ID = "SELECT last_insert_rowid() FROM " + _BOOK_DB;

    public static final char SEPARATOR_CHAR = '|';

    private static final Pattern SEPARATOR =
            Pattern.compile(Character.toString(SEPARATOR_CHAR),Pattern.LITERAL);

    public static String[] readStringArray(String in) {
        return SEPARATOR.split(in);
    }


    //Turn on foreign key constraint in each DB connection
    public static String _onForeignKey = "PRAGMA FOREIGN_KEYS = ON";

    //To drop tables for upgrade
    public static String _DROP = "DROP TABLE IF EXISTS ";

    public static enum parameter {id,title,name,isbn};

    public static enum dbMode {read,write};

    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;
    private final Context context;

    //passed from parent as this is non-context class
    // enables access to resource files
    public CartDbAdapter(Context ParentContext){
        context = ParentContext;
        dbHelper = new DatabaseHelper(context,_DATABASE_NAME,null,_VERSION);
    }


    private static class DatabaseHelper extends SQLiteOpenHelper{

        private final Context Pcontext;

        public DatabaseHelper(Context context, String dbname, SQLiteDatabase.CursorFactory obj, int version ){
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
            s = String.format(s,i,i2);
            Log.w("TaskDBAdapter",s);
            sqLiteDatabase.execSQL(_DROP + _DATABASE_NAME + "." + _BOOK_DB);
            sqLiteDatabase.execSQL(_DROP + _DATABASE_NAME + "." + _AUTHOR_DB); //implicit delete
            //indices should be deleted by itself so we need to create it once again
            onCreate(sqLiteDatabase);
        }
    }

    public Cursor fetchAllBooks(){
        String selection = _JOIN + " ;";
        Cursor c = db.rawQuery(selection, null);
        c.moveToFirst();
        return c;
    }

    public Book fetchBook(String arg1, parameter arg2){
        Book book = null;
        String selection;
        String[] selArg     = {arg1};

        switch (arg2){
            case id:
                selection  = _JOIN + " WHERE " + _BOOK_DB + "." + BookContract._id + "=?";
                break;

            case title:
                selection  = _JOIN + " WHERE " + _BOOK_DB + "." + BookContract._title + "=?";
                break;

           case name:
                selection  = _JOIN + " WHERE " + _AUTHOR_DB + "." + AuthorContract._first_name + "=?";
                break;

            case isbn:
                selection    = _JOIN + " WHERE " +  _BOOK_DB + "." + BookContract._isbn + "=?";
                break;

            default:
                selection  = null;
        }
        Cursor cursor = db.rawQuery(selection,selArg);
        if(cursor != null) {
            cursor.moveToFirst();
            book = new Book(cursor);
        }
        return book;
    }

  public void persistBook(Book book) throws SQLException {
        ContentValues values = new ContentValues();

      db.beginTransaction();
      try {
          //id should get updated on its own
          book.writeToProvider(values);
          db.insert(_BOOK_DB, null, values);
          values.clear();
          persistAuthors(book.authors);
          db.setTransactionSuccessful();

      }catch (SQLiteException ex){
        Log.e("Database",context.getResources().getString(R.string.log_insert));
      }

      finally {
          db.endTransaction();
      }

    }

    public void persistAuthors(ArrayList<Author> author) throws SQLException {
        ContentValues values = new ContentValues();
        String firstName;
        //get the foreign key
        Cursor c = db.rawQuery(_GET_ID,null);
        c.moveToFirst();
        int fk = c.getInt(0); //?? do we increment here
        c.close();
        for (int i = 0; i < author.size(); i++) {
            values.clear();
            //id should get updated on its own
            try {
                firstName = author.get(i).firstName;
            }catch (NullPointerException ex){
                continue;
            }
            if (firstName!=null) {
                AuthorContract.putFk(values, fk);
                AuthorContract.putFirstName(values, author.get(i).firstName);
                AuthorContract.putMiddleName(values, author.get(i).middleInitial);
                AuthorContract.putLastName(values, author.get(i).lastName);
                db.insert(_AUTHOR_DB, null, values);
            }
        }
    }

    public boolean delete(Book book){
        return (db.delete(_BOOK_DB, BookContract._title + " = "
                +"'"+ book.title+"'", null)!=0);
    }


    public boolean deleteAll(){
        return ((db.delete(_BOOK_DB, null, null)!=0))&&(db.delete(_AUTHOR_DB, null, null)!=0);    }


    public void close(){
        //db.close();
    }

    public CartDbAdapter open(dbMode mode) throws SQLException{
        switch (mode){
            case read:
                db = dbHelper.getReadableDatabase();
                break;
            case write:
                db = dbHelper.getWritableDatabase();
                break;
            default:
                Log.e("TaskDBAdapter",context.getResources().getString(R.string.log_modeErr));
                return null;
        }
        db.execSQL(_onForeignKey);
        return this;
    }

}
