package edu.stevens.cs522.myapplication.cloud;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.ArrayMap;
import android.util.JsonReader;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;

/**
 * Created by Sandeep on 3/25/2015.
 */
public abstract class Request implements Parcelable {
    public long clientID;
    public String registrationID; // sanity check 
    public String url;
    public String client;
    public double latitude;
    public double longitude;

    // App‐specific HTTP request headers.
    public abstract Map<String,String> getRequestHeaders();
    // Chat service URI with parameters e.g. query string parameters. 
    public abstract Uri getRequestUri();
    // JSON body (if not null) for request data not passed in headers.
    public abstract String getRequestEntity() throws IOException;
    // Define your own Response class, including HTTP response code.
    public abstract Response getResponse(HttpURLConnection connection,JsonReader rd /* Null for streaming */);

    public static class Register extends Request{

        public Register(long clientID, String id, String url, String client) {
            super(clientID, id, url, client);
        }

        public Register(Parcel parcel) {
            super(parcel);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(clientID);
            dest.writeString(registrationID);
            dest.writeString(url);
            dest.writeString(client);
            dest.writeDouble(longitude);
            dest.writeDouble(latitude);
        }

        @Override
        public Map<String, String> getRequestHeaders() {
            Map<String, String> header = new ArrayMap<String, String>();
            header.put("X-latitude",Double.toString(latitude));
            header.put("X-longitude",Double.toString(longitude));
            return header;
        }

        @Override
        public Uri getRequestUri() {
            String tempUrl = this.url + "?username=" + this.client + "&regid="
                    + this.registrationID;
            return Uri.parse(tempUrl);        }

        @Override
        public String getRequestEntity() throws IOException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Response getResponse(HttpURLConnection connection, JsonReader rd) {
            // TODO Auto-generated method stub
            return null;
        }

        public static final Parcelable.Creator<Register> CREATOR = new Parcelable.Creator<Register>() {
            public Register createFromParcel(Parcel source) {
                return new Register(source);
            }
            public Register[] newArray(int size) {
                return new Register[size];
            }
        };

    }
    public Request(long clientID, String id, String url, String client){
        this.clientID = clientID;
        this.registrationID = id;
        this.url = url;
        this.client = client;
    }


    public void setLocation(double longitude, double latitude){
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public Request(long clientID, String id, String url, String client, double longitude, double latitude){
        this.clientID = clientID;
        this.registrationID = id;
        this.url = url;
        this.client = client;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public Request(Parcel parcel){
        clientID = parcel.readLong();
        registrationID = parcel.readString();
        url = parcel.readString();
        client = parcel.readString();
        longitude = parcel.readDouble();
        latitude = parcel.readDouble();
    }

    public void writeToParcel(Parcel parcel, int flags){
        parcel.writeLong(clientID);
        parcel.writeString(registrationID);
        parcel.writeString(url);
        parcel.writeString(client);
        parcel.writeDouble(longitude);
        parcel.writeDouble(latitude);
    }

    public static class Unregister extends Request {

        public Unregister(long clientID, String regID, String url, String client) {
            super(clientID, regID, url, client);
        }

        public Unregister(Parcel source) {
            // TODO Auto-generated constructor stub
            super(source);
        }

        public int describeContents() {
            // TODO Auto-generated method stub
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(clientID);
            dest.writeString(registrationID);
            dest.writeString(url);
            dest.writeString(client);
            dest.writeDouble(longitude);
            dest.writeDouble(latitude);

        }

        @Override
        public Map<String, String> getRequestHeaders() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Uri getRequestUri() {
            String tempUrl = this.url + "/" + Long.toString(this.clientID)
                    + "?regid=" + this.registrationID;
            return Uri.parse(tempUrl);
        }

        @Override
        public String getRequestEntity() throws IOException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Response getResponse(HttpURLConnection connection, JsonReader rd) {
            // TODO Auto-generated method stub
            return null;
        }

        public static final Parcelable.Creator<Unregister> CREATOR = new Parcelable.Creator<Unregister>() {
            public Unregister createFromParcel(Parcel source) {
                return new Unregister(source);
            }

            public Unregister[] newArray(int size) {
                return new Unregister[size];
            }
        };
    }

    public static class PostMessage extends Request {
        public String message;
        public String chatRoom;

        public PostMessage(long clientID, String regID, String url, String room, String client, String message) {
            super(clientID, regID, url, client);
            this.message  = message;
            this.chatRoom = room;
        }
        public PostMessage(Parcel source) {
            // TODO Auto-generated constructor stub
            super(source);
            this.message  = source.readString();
            this.chatRoom = source.readString();
        }

        @Override
        public int describeContents() {
                // TODO Auto-generated method stub
           return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(message);
            dest.writeString(chatRoom);
        }

        @Override
        public Map<String, String> getRequestHeaders() {
            // TODO Auto-generated method stub
            Map<String, String> header = new ArrayMap<String, String>();
            header.put("X-latitude",Double.toString(latitude));
            header.put("X-longitude",Double.toString(longitude));
            return header;
        }

        @Override
        public Uri getRequestUri() {
            String tempUrl = this.url + "/" + Long.toString(this.clientID)
                    + "?regid=" + this.registrationID + "&seqnum=0";
            return Uri.parse(tempUrl);
        }

        @Override
        public String getRequestEntity() throws IOException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Response getResponse(HttpURLConnection connection, JsonReader rd) {
            // TODO Auto-generated method stub
            return null;
        }
        public static final Parcelable.Creator<PostMessage> CREATOR = new Parcelable.Creator<PostMessage>() {
            public PostMessage createFromParcel(Parcel source) {
                return new PostMessage(source);
            }
            public PostMessage[] newArray(int size) {
                return new PostMessage[size];
            }
        };

    }

}

