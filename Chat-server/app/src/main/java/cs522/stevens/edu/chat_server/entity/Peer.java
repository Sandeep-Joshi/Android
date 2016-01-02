package cs522.stevens.edu.chat_server.entity;

/**
 * Created by Sandeep Joshi on 2/23/2015.
 */

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.net.InetAddress;
import java.net.UnknownHostException;

import edu.stevens.cs522.chat.oneway.server.contracts.PeerContract;

public class Peer implements Parcelable {

    public Long id;
    public String name;
    public InetAddress address;
    public int port;

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel data, int flags){
        data.writeLong(id);
        data.writeString(name);
        data.writeByteArray(address.getAddress());
        data.writeInt(port);
    }

    public Peer(Parcel parcel) {
        id      = parcel.readLong();
        name    = parcel.readString();
        byte[] add = parcel.createByteArray();
        try {
            address = InetAddress.getByAddress(add);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        port    = parcel.readInt();
    }

    public Peer(String name,InetAddress addr, int port ){
        this.name = name;
        this.address = addr;
        this.port=port;
    }

    public Peer(Cursor cursor){
        this.name = PeerContract.getName(cursor);
        try {
            this.address = InetAddress.getByName(PeerContract.getAddress(cursor));
        }catch (UnknownHostException e){
        }
        this.port= Integer.parseInt(PeerContract.getPort(cursor));
        this.id  = Long.parseLong(PeerContract.getId(cursor));
    }

    public static final Parcelable.Creator<Peer> CREATOR = new Parcelable.Creator<Peer>() {
        public Peer createFromParcel(Parcel parcel) {
            return new Peer(parcel);
        }
        public Peer[] newArray(int size) {
            return new Peer[size];
        }
    };

    public Long UriInsert(ContentResolver cr){
        String id = null;
        //Check if the peer exists already
        Cursor c = cr.query(PeerContract.CONTENT_URI, null,
                            PeerContract.NAME + "=?", new String[]{name}, null);

         ContentValues values = new ContentValues();
        if (c.getCount() > 0) {
            values.put(PeerContract.ADDRESS, address.toString());
            values.put(PeerContract.PORT, port);
            cr.update(PeerContract.CONTENT_URI,
                    values, PeerContract.NAME + "=?", new String[]{name});
            if(c.moveToFirst()) {
                id = c.getString(c.getColumnIndex(PeerContract.ID));
            }
        }
        else {
            values.put(PeerContract.ADDRESS, address.toString());
            values.put(PeerContract.PORT, port);
            values.put(PeerContract.NAME, name);
            Uri uri = cr.insert(PeerContract.CONTENT_URI, values);
            id = uri.getLastPathSegment();
        }

        return Long.parseLong(id);
    }

}
