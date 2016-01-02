package edu.stevens.cs522.bookstore.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import edu.stevens.cs522.bookstore.R;
import edu.stevens.cs522.bookstore.entities.Book;

/**
 * Created by Sandeep Joshi on 2/4/2015.
 */
public class DisplayActivity extends Activity{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info);
        Bundle b = this.getIntent().getExtras();
        Book book= b.getParcelable(constants.book);
       // Book book = new Book(getIntent());
        if (book!=null){
            TextView title  =  ((TextView)findViewById(R.id.info_title));
            title.setText(book.title);
            TextView author = ((TextView)findViewById(R.id.info_author));
            author.setText(book.getAuth());
            TextView isbn  =  ((TextView)findViewById(R.id.info_isbn));
            isbn.setText(book.isbn);
            TextView price  =  ((TextView)findViewById(R.id.info_price));
            price.setText(book.price);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.general, menu);
        return true;
    }

}
