package edu.stevens.cs522.bookstore;

/**
 * Created by Sandeep Joshi on 3/6/2015.
 */

//Used for factory creation for async callbacks
public interface IContinue<T> {
    public void Kontinue(T value);
}


