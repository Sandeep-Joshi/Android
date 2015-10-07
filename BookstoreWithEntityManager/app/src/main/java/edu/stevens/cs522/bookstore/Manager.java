package edu.stevens.cs522.bookstore;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

/**
 * Created by Sandeep on 3/13/2015.
 */
public abstract class Manager<T> {

    private final Context context;

    private final IEntityCreator<T> creator;

    private final int loaderId;

    private final String tag;

    private ContentResolver syncResolver;

    private AsyncContentResolver asyncResolver;

    protected ContentResolver getSyncResolver(){
        if(syncResolver==null){
            syncResolver = context.getContentResolver();
        }
        return syncResolver;
    }

    protected AsyncContentResolver getAsyncResolver(){
        if(asyncResolver==null)
            asyncResolver = new AsyncContentResolver(context.getContentResolver());
        return asyncResolver;
    }

    protected void executeSimpleQuery(Uri uri, ISimpleQueryListener<T> listener){
        SimpleQueryBuilder.executeQuery((Activity)context, uri, creator, listener);
    }

    protected void executeSimpleQuery(Uri uri, String[] projection, String selection,
                                      String[] selectionArgs, ISimpleQueryListener<T> listener){
        SimpleQueryBuilder.executeQuery((Activity)context,uri,projection,selection,
                                         selectionArgs,creator,listener);
    }

    protected void executeQuery(Uri uri, IQueryListener<T> listener){
        QueryBuilder.executeQuery(tag,(Activity)context, uri, loaderId, creator, listener);
    }

    protected void executeQuery(Uri uri, String[] projection, String selection,
                                String[] selectionArgs, IQueryListener<T> listener){
        QueryBuilder.executeQuery(tag,(Activity)context,uri,loaderId,projection,selection,
                                    selectionArgs,creator,listener);
    }

    protected void reexecuteQuery(Uri uri, String[] projection, String selection,
    String[] selectionArgs, IQueryListener<T> listener){
        QueryBuilder.reexecuteQuery((Activity)context,uri,projection,selection,selectionArgs,
                                    creator,listener);
    }

    protected Manager(Context context, IEntityCreator<T> creator, int loaderId){
        this.context = context;
        this.creator = creator;
        this.loaderId = loaderId;
        this.tag = this.getClass().getCanonicalName();
    }
}
