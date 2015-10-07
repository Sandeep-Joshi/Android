package cs522.stevens.edu.chatapp2;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Sandeep on 3/15/2015.
 */
public class Peer implements Parcelable {
    public long id;
    public String name;
    public String address;
    public int port;

    public Peer(String name, String address,int port) {
        this.name = name;
        this.address = address;
        this.port = port;
    }

    public Peer(Parcel source) {
        // TODO Auto-generated constructor stub
        name = source.readString();
        address = source.readString();
        port = source.readInt();
    }
    public Peer(Cursor cursor) {
        this.name = PeerContract.getName(cursor);
        this.address = PeerContract.getAddress(cursor);
        this.port = Integer.parseInt(PeerContract.getPort(cursor));
        this.id = Integer.parseInt(PeerContract.getId(cursor));
    }

    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        // TODO Auto-generated method stub
        dest.writeString(name);
        dest.writeString(address);
        dest.writeInt(port);
    }
    public static final Parcelable.Creator<Peer> CREATOR = new Parcelable.Creator<Peer>() {
        public Peer createFromParcel(Parcel source) {
            return new Peer(source);
        }
        public Peer[] newArray(int size) {
            return new Peer[size];
        }
    };

}
