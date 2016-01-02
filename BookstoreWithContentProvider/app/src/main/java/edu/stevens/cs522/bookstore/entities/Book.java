package edu.stevens.cs522.bookstore.entities;

import android.content.ContentValues;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import edu.stevens.cs522.bookstore.R;
import edu.stevens.cs522.bookstore.activities.BookStoreActivity;
import edu.stevens.cs522.bookstore.activities.constants;
import edu.stevens.cs522.bookstore.contracts.AuthorContract;
import edu.stevens.cs522.bookstore.contracts.BookContract;
import edu.stevens.cs522.bookstore.databases.CartDbAdapter;

public class Book implements Parcelable {  //implemented Parcelable interface
	
	// TODO Modify this to implement the Parcelable interface.

	public int id;
	
	public String title;

    public ArrayList<Author> authors = new ArrayList<Author>();
	
	//public Author[] authors;

    public String[] allAuthors;  //concatenated authors
	
	public String isbn;
	
	public String price;

    public static final String Id      = "id";
    public static final String Authors = "authors";
    public static final String Title   = "title";
    public static final String Isbn    = "isbn";
    public static final String Price   = "price";

    //
    public int describeContents() {
        return 0;
    }

    public static AtomicInteger newID = new AtomicInteger();

    private static final int getNewId(){
        return newID.incrementAndGet();
    }

    private static final String getPrice(){
        Random random = new Random();
        String price = Integer.toString(random.nextInt(101) + 100);
        return "$ "+price;
    }

    public void writeToParcel(Parcel data, int flags){
      //  data.writeInt(id);
        data.writeTypedList(authors);
        data.writeString(title);
        data.writeString(isbn);
        data.writeString(price);
    }

    private void readFromParcel(Parcel in){
        in.readTypedList(authors,Author.CREATOR);
        this.title   = in.readString();
        this.isbn    = in.readString();
        this.price   = in.readString();

    }


    public String[] parseAuthors(String authors){
        if(authors!=null)
            return authors.split(",");//constants.separator);
        else
            return null;
    }

    public Book(int id, String title, ArrayList<Author> author, String isbn, String price) {
        this.title   = title;
        this.authors = author;
        this.isbn    = isbn;
        this.price   = price;
    }

	public Book(int id, String title, ArrayList<Author> author, String isbn) {
        this.authors = author;
        this.title   = title;
		this.isbn    = isbn;
		this.price   = getPrice();
	}

    public void bundle(Intent result){
        result.putParcelableArrayListExtra(Authors,this.authors);
        result.putExtra(Title,this.title);
        result.putExtra(Isbn,this.isbn);
        result.putExtra(Price,this.price);
    }

    public Book(Parcel in) {
        in.readTypedList(authors,Author.CREATOR);
        this.title   = in.readString();
        this.isbn    = in.readString();
        this.price   = in.readString();
    }

    public Book(Intent in){
        this.authors = in.getParcelableArrayListExtra(Authors);
        this.title   = in.getStringExtra(Title);
        this.price   = in.getStringExtra(Price);
        this.isbn    = in.getStringExtra(Isbn);
    }

    public Book(Cursor cursor){
        this.title = BookContract.getTitle(cursor);
        this.price = BookContract.getPrice(cursor);
        this.isbn  = BookContract.getIsbn(cursor);

        //Initialize author array
        ArrayList<Author> array = new ArrayList<Author>();
        allAuthors = parseAuthors(AuthorContract.getAllauthors(cursor));

        for (int i=0;i<allAuthors.length;i++){
            Author author = new Author(AuthorContract.getFirstName(cursor),AuthorContract.getMiddleName(cursor),AuthorContract.getLastName(cursor));
            array.add(author);
        }

         this.authors = array;
    }

    public void writeToProvider(ContentValues values){
        BookContract.putTitle(values, title);
        BookContract.putIsbn(values, isbn);
        BookContract.putPrice(values, price);
    }

    public static final Parcelable.Creator<Book> CREATOR = new Parcelable.Creator<Book>() {
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    public String getAuth() {
        StringBuilder authList = new StringBuilder();

        for (int i = 0; i< authors.size();i++){
            authList.append(authors.get(i).getName()+", ");
        }
        return authList.toString();
    }

    // TODO redefine toString() to display book title and price (why?).
    //It is needed so the Array adapter may call it to parse the object to displayable string
    @Override
    public String toString() {
        return this.title+ constants.separator + this.price;
    }

}