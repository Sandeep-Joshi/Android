package edu.stevens.cs522.chat.oneway.server.entity;

/**
 * Created by Sandeep Joshi on 2/23/2015.
 */
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import edu.stevens.cs522.chat.oneway.server.contracts.MessageContract;
import edu.stevens.cs522.chat.oneway.server.contracts.PeerContract;

public class Message implements Parcelable{
    public Long id;
    public Long peer_fk;
    public String message;
    public String sender;

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel data, int flags){
        data.writeLong(id);
        data.writeString(message);
        data.writeString(sender);
        data.writeLong(peer_fk);
    }

    public Message(Long Id, String Message, String Sender, Long peer_fk){
        this.id          = Id;
        this.message     = Message;
        this.sender      = Sender;
        this.peer_fk     = peer_fk;

    }

    public Message(String message, String sender, Long peer_fk) {
        this.message = message;
        this.sender  = sender;
        this.peer_fk = peer_fk;
    }

    public Message(Parcel parcel) {
        // TODO Auto-generated constructor stub
        message = parcel.readString();
        sender  = parcel.readString();
        peer_fk = parcel.readLong();
    }

    public Message(Cursor cursor){
        this.id          = MessageContract.getId(cursor);
        this.message     = MessageContract.getMessageText(cursor);
        this.sender      = MessageContract.getSender(cursor);
        this.peer_fk     = MessageContract.getPeerFk(cursor);
    }

    public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() {
        public Message createFromParcel(Parcel source) {
            return new Message(source);
        }
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    public Long UriInsert(ContentResolver cr){
        String id = null;

        ContentValues values = new ContentValues();
        values.put(MessageContract.MESSAGE_TEXT, message);
        values.put(MessageContract.SENDER, sender);
        values.put(MessageContract.PEER_FK, peer_fk);
        Uri uri = cr.insert(MessageContract.CONTENT_URI, values);
        id = uri.getLastPathSegment();
        return Long.parseLong(id);
    }

}
