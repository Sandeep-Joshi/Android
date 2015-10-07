package edu.stevens.cs522.myapplication.entity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import edu.stevens.cs522.myapplication.contracts.ChatRoomContract;

/**
 * Created by Sandeep on 4/26/2015.
 */
public class ChatRoom implements Parcelable{
    public long id;
    public String name;

    public ChatRoom(long id ,String name) {
        this.name = name;
        this.id = id;
    }

    public ChatRoom(String name) {
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public ChatRoom(Parcel source) {
        // TODO Auto-generated constructor stub
        name = source.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
    }

    public static final Parcelable.Creator<ChatRoom> CREATOR = new Parcelable.Creator<ChatRoom>() {
        public ChatRoom createFromParcel(Parcel source) {
            return new ChatRoom(source);
        }
        public ChatRoom[] newArray(int size) {
            return new ChatRoom[size];
        }
    };

    public Long get(Context context){
        Long id = null;
        Cursor c = context.getContentResolver().query(ChatRoomContract.CONTENT_URI,
                new String[] { ChatRoomContract.ID, ChatRoomContract.NAME },
                ChatRoomContract.NAME + "=?", new String[] { name }, null);
        if (c.moveToFirst()) {
            id = ChatRoomContract.getId(c);
        }
        return id;
    }

    public Long add(Context context) {
        ContentValues values = new ContentValues();
        Long id = this.get(context);
        //see if this room exists
        if (id==null){
            values.put(ChatRoomContract.NAME,this.name);
            Uri uri = context.getContentResolver().insert(ChatRoomContract.CONTENT_URI,values);
            id = Long.parseLong(uri.getLastPathSegment());
        }
        return id;
    }
}
