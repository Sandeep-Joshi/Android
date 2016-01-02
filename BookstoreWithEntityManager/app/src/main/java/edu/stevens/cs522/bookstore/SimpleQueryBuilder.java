package edu.stevens.cs522.bookstore;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sandeep Joshi on 3/6/2015.
 */
//Factory for async queries
public class SimpleQueryBuilder<T> implements IContinue<Cursor> {
    private IEntityCreator<T> helper;
    private ISimpleQueryListener<T> listener;

    private SimpleQueryBuilder(IEntityCreator<T> helper, ISimpleQueryListener<T> listener){
        this.helper   = helper;
        this.listener = listener;
    }

    @Override
    public void Kontinue(Cursor value) {
        List<T> instances = new ArrayList<T>();
        if (value.moveToFirst()){
            do{
                T instance = helper.create(value);  //create object lists
                instances.add(instance);
            }while(value.moveToNext());
        }

        value.close();
        listener.handleResults(instances);
    }

    public static <T> void executeQuery(Activity context, Uri uri, IEntityCreator<T> helper,
                                        ISimpleQueryListener<T> listener){
        SimpleQueryBuilder<T> qb = new SimpleQueryBuilder<T>(helper,listener);

        AsyncContentResolver resolver = new AsyncContentResolver(context.getContentResolver());
        resolver.queryAsync(uri, null, null, null, null, qb);
    }

  /*  public static <T> void executeQuery(Activity context, Uri uri, IEntityCreator<T> helper,
                                        ISimpleQueryListener<T> listener){
        SimpleQueryBuilder<T> qb = new SimpleQueryBuilder<T>(helper,listener);

        AsyncContentResolver resolver = new AsyncContentResolver(context.getContentResolver());
        resolver.queryAsync(uri, null, null, null, null, qb);
    }*/
}
