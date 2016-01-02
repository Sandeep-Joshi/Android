package edu.stevens.cs522.bookstore.entities;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import edu.stevens.cs522.bookstore.contracts.AuthorContract;
import edu.stevens.cs522.bookstore.contracts.BookContract;

public class Author implements Parcelable {
	
	// TODO Modify this to implement the Parcelable interface.

	// NOTE: middleInitial may be NULL!
	
	public String firstName;

	public String middleInitial;

	public String lastName;

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel data, int flags){
        if (firstName==null)
            data.writeValue(firstName);
        else
            data.writeString(firstName);

        if (middleInitial == null)
            data.writeValue(middleInitial);
        else
            data.writeString(middleInitial);

        if (lastName == null)
            data.writeValue(lastName);
        else
            data.writeString(lastName);

    }

    public void writeToProvider(ContentValues values){
        AuthorContract.putFirstName(values, firstName);
        AuthorContract.putLastName(values, lastName);
        AuthorContract.putMiddleName(values, middleInitial);
    }

    public Author(String firstName, String middleInitial, String lastName) {
        this.firstName     = firstName;
        this.middleInitial = middleInitial;
        this.lastName      = lastName;
    }

    public Author(Parcel in) {
        this.firstName      = in.readString();
        this.middleInitial  = in.readString();//in.readValue(String.class.getClassLoader());
        this.lastName       = in.readString();
    }

    public String getName(){
        return firstName+' '+middleInitial+' '+lastName;
    }

    //Method to convert parcel to array
    public static Author[] convert(Parcelable[] in) {
        Author[] authors  = new Author[in.length];
        System.arraycopy(in,0,authors,0,in.length);
        return authors;
    }

    public static final Creator<Author> CREATOR = new Parcelable.Creator<Author>() {
        public Author createFromParcel(Parcel in) {
            return new Author(in);
        }

        public Author[] newArray(int size) {
            return new Author[size];
        }
    };
}
