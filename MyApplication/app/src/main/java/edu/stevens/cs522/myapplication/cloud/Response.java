package edu.stevens.cs522.myapplication.cloud;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.JsonReader;
import android.util.JsonToken;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Created by Sandeep on 3/29/2015.
 */
public class Response implements Parcelable {

    private String id;
    private int responseCode;
    public String responseMessage = "";
    // HTTP status code.
    public int httpResponseCode = 0;
    // HTTP status line message.
    public String httpResponseMessage = "";

    public static String _id = "id";
    public static enum ResponseType {
        ERROR,
        REGISTER,
    }
    private final static String TAG = Response.class.getCanonicalName();

    public Response(Parcel source) {
        // TODO Auto-generated constructor stub
        id = source.readString();
        httpResponseCode = source.readInt();
        httpResponseMessage = source.readString();
    }

    public Response(){};

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(responseMessage);
        out.writeInt(httpResponseCode);
        out.writeString(httpResponseMessage);
    }

    // Parse the json response entity.
    protected void parseResponse(JsonReader rd) throws IOException {
        // The default is not to expect any json data in the response.
        rd.beginObject();
        while(rd.peek()!=JsonToken.END_OBJECT){
            String label = rd.nextName();
            if("id".equals(label)){
                //
            }
        }
    }

    protected static String parseString(JsonReader rd) throws IOException {
        if (rd.peek() == JsonToken.NULL) {
            rd.nextNull();
            return null;
        } else {
            return rd.nextString();
        }
    }

    protected static void matchName(String name, JsonReader rd) throws IOException {
        String label = rd.nextName();
        if (!name.equals(label)) {
            throw new IOException("Error in response entity: expected "+name+", encountered "+label);
        }
    }

    public Response(HttpURLConnection connection) throws IOException {
        // Use connection.getHeaderField() to get app-specific response headers
        //connection.getHeaderField();  //check for app specified header
        httpResponseCode = connection.getResponseCode();
        httpResponseMessage = connection.getResponseMessage();

    }

    public Response(String responseMessage, int httpResponseCode, String httpResponseMessage) {
        this.responseMessage = responseMessage;
        this.httpResponseCode = httpResponseCode;
        this.httpResponseMessage = httpResponseMessage;
    }

    public void setId(String id) { this.id = id;}

    public String getId() { return id;}

    public static Response createResponse(Parcel in) {
        ResponseType requestType = ResponseType.valueOf(in.readString());
        switch (requestType) {
            case ERROR:
                return new ErrorResponse(in);
            case REGISTER:
                return new RegisterResponse(in);
        }
        throw new IllegalArgumentException("Unknown request type: "+requestType.name());
    }

    public int getResponseCode() { return responseCode;}

    public void setResponseCode(int responseCode) { this.responseCode = responseCode;}

    public static final Parcelable.Creator<Response> CREATOR = new Parcelable.Creator<Response>() {
        public Response createFromParcel(Parcel source) {
            return new Response(source);
        }
        public Response[] newArray(int size) {
            return new Response[size];
        }
    };

    public static class ErrorResponse extends Response implements Parcelable {

        public Status status;

        public enum Status {
            NETWORK_UNAVAILABLE,
            SERVER_ERROR,
            SYSTEM_ERROR,
            APPLICATION_ERROR
        }

        public ErrorResponse(int responseCode, Status status, String message) {
            this(responseCode, status, message, "");
            this.status = status;
        }

        public ErrorResponse(int responseCode, Status status, String message, String httpMessage) {
            super(message, responseCode, httpMessage);
            this.status = status;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeString(ResponseType.ERROR.name());
            super.writeToParcel(out, flags);
            out.writeString(status.name());
        }

        public ErrorResponse(Parcel in) {
            super(in);
            status = Status.valueOf(in.readString());
        }

        public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
            public ErrorResponse createFromParcel(Parcel in) {
                in.readString(); // read the tag
                return new ErrorResponse(in);
            }

            public ErrorResponse[] newArray(int size) {
                return new ErrorResponse[size];
            }
        };
    }

    public static class RegisterResponse extends Response implements Parcelable {

        public long id;

        public RegisterResponse(HttpURLConnection connection, JsonReader rd) throws IOException {
            super(connection);
            parseResponse(rd);
        }

        @Override
        protected void parseResponse(JsonReader rd) throws IOException {
            rd.beginObject();
            matchName("id", rd);
            this.id = rd.nextInt();
            rd.endObject();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeString(ResponseType.REGISTER.name());
            super.writeToParcel(out, flags);
            out.writeLong(id);
        }

        public RegisterResponse(Parcel in) {
            super(in);
            id = in.readInt();
        }

        public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
            public RegisterResponse createFromParcel(Parcel in) {
                in.readString();   // read the tag
                return new RegisterResponse(in);
            }

            public RegisterResponse[] newArray(int size) {
                return new RegisterResponse[size];
            }
        };
    }

}
