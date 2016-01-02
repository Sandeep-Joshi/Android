package edu.stevens.cs522.bookstore.managers;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import edu.stevens.cs522.bookstore.AsyncContentResolver;
import edu.stevens.cs522.bookstore.IContinue;
import edu.stevens.cs522.bookstore.contracts.BookContract;
import edu.stevens.cs522.bookstore.entities.Book;

/**
 * Created by Sandeep Joshi on 3/6/2015.
 */
public class BookManager  {
    AsyncContentResolver asyncContentResolver = new AsyncContentResolver(context.getContentResolver());

    final static Uri CONTENT_URI = BookContract.CONTENT_URI;

    public void persistAsync(final Book book, IContinue<Uri> callback){
        ContentValues values = new ContentValues();
        book.writeToProvider(values);
        asyncContentResolver.insertAsync(CONTENT_URI,values,new IContinue<Uri>() {
            @Override
            public void Kontinue(Uri value) {
                //book.id = getId(value);
            }
        });
    }

}
