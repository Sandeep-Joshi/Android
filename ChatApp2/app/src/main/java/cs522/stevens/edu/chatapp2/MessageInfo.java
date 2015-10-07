package cs522.stevens.edu.chatapp2;

/**
 * Created by Sandeep on 3/15/2015.
 */
import android.os.Parcel;
import android.os.Parcelable;

public class MessageInfo implements Parcelable{
    public long id;
    public String messageText;
    public String sender;

    public MessageInfo(String message, String sender) {
        this.messageText = message;
        this.sender = sender;
    }

    public MessageInfo(Parcel source) {
        // TODO Auto-generated constructor stub
        messageText = source.readString();
        sender = source.readString();
    }

    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        // TODO Auto-generated method stub
        dest.writeString(messageText);
        dest.writeString(sender);
    }
    public static final Parcelable.Creator<MessageInfo> CREATOR = new Parcelable.Creator<MessageInfo>() {
        public MessageInfo createFromParcel(Parcel source) {
            return new MessageInfo(source);
        }
        public MessageInfo[] newArray(int size) {
            return new MessageInfo[size];
        }
    };

}
