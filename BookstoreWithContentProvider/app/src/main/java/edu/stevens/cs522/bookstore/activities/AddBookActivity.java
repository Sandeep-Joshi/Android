package edu.stevens.cs522.bookstore.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import edu.stevens.cs522.bookstore.R;
import edu.stevens.cs522.bookstore.entities.Author;
import edu.stevens.cs522.bookstore.entities.Book;
import android.view.View.OnClickListener;

import java.util.ArrayList;

public class AddBookActivity extends Activity implements OnClickListener{
	
	// Use this as the key to return the book details as a Parcelable extra in the result intent.
	public static final String BOOK_RESULT_KEY = "book_result";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_book);

        //Register all on screen buttons
        Button button1 = (Button)findViewById(R.id.add);
        button1.setOnClickListener(this);
        Button button2 = (Button)findViewById(R.id.cancel);
        button2.setOnClickListener(this);
        Button button3 = (Button)findViewById(R.id.search);
        button3.setOnClickListener(this);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// TODO provide SEARCH and CANCEL options
		return true;
	}


    @Override
    public void onClick(View v) {
        Intent result = new Intent();
        final String title  =  ((EditText)findViewById(R.id.search_title)).getText().toString();
        final String author =  ((EditText)findViewById(R.id.search_author)).getText().toString();
        final String isbn   =  ((EditText)findViewById(R.id.search_isbn)).getText().toString();
        Author authors;

        String[] splits = author.split("\\s+");
        if (splits.length==3){
            authors = new Author(splits[0], splits[1], splits[2]);
        }else if (splits.length==2){
            authors = new Author(splits[0], " ", splits[1]);
        }else {
            authors = new Author(splits[0], " ", " ");
        }
        ArrayList<Author> authArray = new ArrayList<Author>();
        authArray.add(authors);

        switch (v.getId()){

            case android.R.id.home:
                finish();
                break;

            case R.id.add:
                //pass the inputs as the intent extras
                Book book = new Book(0,title, authArray, isbn);
                //book.bundle(result);
                result.putExtra(constants.book, book);
                setResult(Activity.RESULT_OK, result);
                finish();
                break;

            // SEARCH: return the book details to the BookStore activity
            case R.id.search:
                result.putExtra(constants.book, searchBook(title, authArray, isbn));
                setResult(Activity.RESULT_OK, result);
                finish();
                break;

            // CANCEL: cancel the search request
            case R.id.cancel:
                setResult(Activity.RESULT_CANCELED,result);
                finish();
                break;
        }

    }

    //Menu is made on the screen rather expander below as this was needing unnecessary clicks
	/*@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        Toast.makeText(getApplicationContext(), "on options", Toast.LENGTH_SHORT).show();

        super.onOptionsItemSelected(item);
		// TODO
        Intent result = new Intent();
        final String title  =  ((EditText)findViewById(R.id.search_title)).getText().toString();
        final String author =  ((EditText)findViewById(R.id.search_author)).getText().toString();
        final String isbn   =  ((EditText)findViewById(R.id.search_isbn)).getText().toString();

        String[] splits = author.split("\\s+");
        Author authors = new Author(splits[0],splits[1],splits[2]);
        Author[] authArray = Author.CREATOR.newArray(1);
        authArray[0] = authors;

        switch (item.getItemId()){

            case android.R.id.home:
                finish();
                break;

            case R.id.add:
                //pass the inputs as the intent extras
                Book book = new Book(0,title, authArray, isbn);
                result.putExtra(constants.book, book);
                setResult(Activity.RESULT_OK, result);
                Toast.makeText(getApplicationContext(), R.string.Msg_added, Toast.LENGTH_SHORT).show();
                finish();

                break;

            // SEARCH: return the book details to the BookStore activity
            case R.id.search:
                result.putExtra(constants.book, searchBook(title, authArray, isbn));
                setResult(Activity.RESULT_OK, result);
                finish();
                break;

            // CANCEL: cancel the search request
            case R.id.cancel:
                setResult(Activity.RESULT_CANCELED,result);
                finish();
                break;
        }

		

		return false;
	}*/
	
	public Book searchBook(String title, ArrayList<Author> authArray, String isbn){
		/*
		 * Search for the specified book.
		 */
		// TODO Just build a Book object with the search criteria and return that.
        Book book = new Book(0,title, authArray, isbn);
        return book;
	}

}