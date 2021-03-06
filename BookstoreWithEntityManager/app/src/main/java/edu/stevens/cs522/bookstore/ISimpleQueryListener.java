package edu.stevens.cs522.bookstore;

import java.util.List;

/**
 * Created by Sandeep Joshi on 3/6/2015.
 */
//Async queries sans loader manager.
public interface ISimpleQueryListener<T> {
    public void handleResults(List<T> results);
}
