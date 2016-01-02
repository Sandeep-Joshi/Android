package edu.stevens.cs522.bookstore;

import android.database.Cursor;

/**
 * Created by Sandeep Joshi on 3/6/2015.
 */
public interface IEntityCreator<T> {
    public T create(Cursor cursor);
}
