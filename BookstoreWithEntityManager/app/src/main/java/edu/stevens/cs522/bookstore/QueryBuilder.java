package edu.stevens.cs522.bookstore;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

/**
 * Created by Sandeep on 3/13/2015.
 */
public class QueryBuilder<T> implements LoaderManager.LoaderCallbacks<Cursor> {

    private int loaderId;
    private IQueryListener<T> listener;
    private IEntityCreator<T> creator;
    private LoaderManager lm;

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        if(i == loaderId){
            return new CursorLoader(context, uri, projection, selection, selectionArgs, null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor t) {
        if(loader.getId()== loaderId){
            listener.handleResults(new TypedCursor<T>(t,creator));
        }else {
            throw new IllegalStateException("Unexpected loader callback");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(loader.getId()== loaderId){
            listener.closeResults();
        }else {
            throw new IllegalStateException("Unexpected loader callback");
        }
    }

    private QueryBuilder(String tag, Context context, Uri uri, int loaderId,
                         IEntityCreator<T> creator,IQueryListener<T> listener){
        this.loaderId = loaderId;
        this.creator  = creator;
        this.listener = listener;
    }

    public static <T> void executeQuery(String tag, Activity context, Uri uri, int loaderId,
                                        IEntityCreator<T> creator, IQueryListener<T> listener){
        QueryBuilder<T> qb = new QueryBuilder<T>(tag, context, uri, loaderId, creator, listener);
        LoaderManager lm = context.getLoaderManager();
        lm.initLoader(loaderId, null, qb);
    }


    public static <T> void executeQuery(String tag, Activity context, Uri uri, int loaderId,
                                        String[] projection, String selection, String[] selectionArgs
                                        IEntityCreator<T> creator1, IQueryListener<T> listener){
        QueryBuilder<T> qb = new QueryBuilder<T>(tag, context, uri, loaderId, creator1, listener);
        LoaderManager lm = context.getLoaderManager();
        lm.initLoader(loaderId, null, qb);
    }

    public static <T> void reexecuteQuery(Activity context, Uri uri, String[] projection,
                                          String selection, String[] selectionArgs
                                          IEntityCreator<T> creator2, IQueryListener<T> listener){
     /*   QueryBuilder<T> qb = new QueryBuilder<T>(tag, context, uri, loaderId, creator, listener);
        LoaderManager lm = context.getLoaderManager();
        lm.restartLoader(loaderId, null, qb);*/
    }


}
