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

import java.net.InetAddress;
import java.net.UnknownHostException;

import edu.stevens.cs522.myapplication.contracts.PeerContract;

public class Peer implements Parcelable {

    public Long id;
    public String name;
    public InetAddress address;
    public int port;
    public String regId;
    public String clientId;
    public double longitude;
    public double latitude;


    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel data, int flags){
        data.writeLong(id);
        data.writeString(clientId);
        data.writeString(name);
        data.writeString(regId);
        data.writeByteArray(address.getAddress());
        data.writeInt(port);
        data.writeDouble(longitude);
        data.writeDouble(latitude);
    }

    public Peer(Parcel parcel) {
        id        = parcel.readLong();
        clientId  = parcel.readString();
        name      = parcel.readString();
        regId     = parcel.readString();

        byte[] add = parcel.createByteArray();
        try {
            address = InetAddress.getByAddress(add);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        port    = parcel.readInt();
        longitude = parcel.readDouble();
        latitude  = parcel.readDouble();
    }

    public Peer(long id, String clientId, String name, String regId, InetAddress addr, int port,
    double longitude, double latitude){
        this.name      = name;
        this.clientId  = clientId;
        this.address   = addr;
        this.port      = port;
        this.id        = id;
        this.regId     = regId;
        this.longitude = longitude;
        this.latitude  = latitude;
    }

    public Peer(long id, String name, String regId ){
        this.name = name;
        this.id=id;
        this.regId=regId;
    }

    public Peer(Cursor cursor){
        this.name = PeerContract.getName(cursor);
        this.clientId = PeerContract.getClientId(cursor);
       /*try {
            this.address = InetAddress.getByName(PeerContract.getAddress(cursor));
        }catch (UnknownHostException e){

        }*/
        this.port= Integer.parseInt(PeerContract.getPort(cursor));
        this.id  = Long.parseLong(PeerContract.getId(cursor));
        this.regId  = PeerContract.getRegId(cursor);
        this.id  = Long.parseLong(PeerContract.getId(cursor));
        this.longitude = PeerContract.getLongitude(cursor);
        this.latitude  = PeerContract.getLatitude(cursor);
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
            values.put(PeerContract.CLIENTID, clientId);
            values.put(PeerContract.REGID, regId);
            values.put(PeerContract.ADDRESS, address.toString());
            values.put(PeerContract.PORT, port);
            values.put(PeerContract.LONGITUDE, longitude);
            values.put(PeerContract.LATITUDE, latitude);
            cr.update(PeerContract.CONTENT_URI,
                    values, PeerContract.NAME + "=?", new String[]{name});
            if(c.moveToFirst()) {
                id = c.getString(c.getColumnIndex(PeerContract.ID));
            }
        }
        else {
            values.put(PeerContract.CLIENTID, clientId);
            values.put(PeerContract.REGID, regId);
            values.put(PeerContract.ADDRESS, address.toString());
            values.put(PeerContract.PORT, port);
            values.put(PeerContract.NAME, name);
            values.put(PeerContract.LONGITUDE, longitude);
            values.put(PeerContract.LATITUDE, latitude);
            Uri uri = cr.insert(PeerContract.CONTENT_URI, values);
            id = uri.getLastPathSegment();
        }

        return Long.parseLong(id);
    }

}
