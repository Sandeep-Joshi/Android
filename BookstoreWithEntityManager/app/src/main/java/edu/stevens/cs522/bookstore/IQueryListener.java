package edu.stevens.cs522.bookstore;

/**
 * Created by Sandeep on 3/13/2015.
 */

//loader managed queries
public interface IQueryListener<T> {
    public void handleResults(TypedCursor<T> results);
    public void closeResults();
}
