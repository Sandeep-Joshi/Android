package edu.stevens.cs522.myapplication.entity;

/**
 * Created by Sandeep Joshi on 2/23/2015.
 */

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import edu.stevens.cs522.myapplication.contracts.MessageContract;

public class iMessage implements Parcelable {
    public Long id;
    public Long peer_fk;
    public String message;
    public String sender;
    public Long timeStamp;
    public Long seq;
    public Long chatRoom;
    public double latitude;
    public double longitude;

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel data, int flags){
        data.writeLong(id);
        data.writeString(message);
        data.writeString(sender);
        data.writeLong(seq);
        data.writeLong(timeStamp);
        data.writeLong(chatRoom);
        data.writeLong(peer_fk);
        data.writeDouble(latitude);
        data.writeDouble(longitude);
    }

    public iMessage(Long Id, String Message, String Sender, Long chatRoom, Long timeStamp, Long seq,
                    Long peer_fk, Double latitude, Double longitude){
        this.id          = Id;
        this.message     = Message;
        this.sender      = Sender;
        this.peer_fk     = peer_fk;
        this.timeStamp   = timeStamp;
        this.chatRoom    = chatRoom;
        this.seq         = seq;
        this.latitude    = latitude;
        this.longitude   = longitude;
    }

    public iMessage(String Message, String Sender, Long chatRoom, Long timeStamp, Long seq,
                    Long peer_fk, Double latitude, Double longitude){
        this.message     = Message;
        this.sender      = Sender;
        this.peer_fk     = peer_fk;
        this.timeStamp   = timeStamp;
        this.chatRoom    = chatRoom;
        this.seq         = seq;
        this.latitude    = latitude;
        this.longitude   = longitude;
    }

    public iMessage(Parcel parcel) {
        // TODO Auto-generated constructor stub
        message   = parcel.readString();
        sender    = parcel.readString();
        chatRoom  = parcel.readLong();
        seq       = parcel.readLong();
        timeStamp = parcel.readLong();
        peer_fk   = parcel.readLong();
        latitude  = parcel.readDouble();
        longitude = parcel.readDouble();
    }

    public iMessage(Cursor cursor){
        this.id          = MessageContract.getId(cursor);
        this.message     = MessageContract.getMessageText(cursor);
        this.sender      = MessageContract.getSender(cursor);
        this.chatRoom    = MessageContract.getChatroom(cursor);
        this.seq         = MessageContract.getSeq(cursor);
        this.timeStamp   = Long.parseLong(MessageContract.getTimestamp(cursor));
        this.peer_fk     = MessageContract.getPeerFk(cursor);
        this.latitude    = MessageContract.getLatitude(cursor);
        this.longitude   = MessageContract.getLongitude(cursor);
    }

    public static final Creator<iMessage> CREATOR = new Creator<iMessage>() {
        public iMessage createFromParcel(Parcel source) {
            return new iMessage(source);
        }
        public iMessage[] newArray(int size) {
            return new iMessage[size];
        }
    };

    public Long UriInsert(ContentResolver cr){
        String id = null;

        ContentValues values = new ContentValues();
        values.put(MessageContract.MESSAGE_TEXT, message);
        values.put(MessageContract.SENDER, sender);
        values.put(MessageContract.CHATROOM, chatRoom);
        values.put(MessageContract.SEQ, seq);
        values.put(MessageContract.PEER_FK, peer_fk);
        values.put(MessageContract.LATITUDE, latitude);
        values.put(MessageContract.LONGITUDE, longitude);
        Uri uri = cr.insert(MessageContract.CONTENT_URI, values);
        id = uri.getLastPathSegment();
        return Long.parseLong(id);
    }

}
