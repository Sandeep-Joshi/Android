package edu.stevens.cs522.bookstore;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.net.Uri;

/**
 * Created by Sandeep Joshi on 3/6/2015.
 */
public class MyContentResolver extends AsyncQueryHandler {
    public MyContentResolver(ContentResolver cr) {
        super(cr);
    }

    //@Override
    public void onInsertComplete(Uri uri){

    }
}
