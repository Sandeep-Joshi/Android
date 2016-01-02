package edu.stevens.cs522.myapplication.Service;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Sandeep on 5/3/2015.
 */
public class address implements Parcelable {

    private Double lat;
    private Double lng;
    private Context context;
    private TextView locationAdd;

    public address(Double lat, Double lng, Context context){
        this.lat = lat;
        this.lng = lng;
        this.context = context;
    }

    public void setContext(Context context){
        this.context = context;
    }

    public String getLng(){
        return lng.toString();
    }
    public String getLat(){
        return lat.toString();
    }
    public void getLocation(TextView txt) {
        Location location = new Location("Address");
        location.setLatitude(lat);
        location.setLongitude(lng);
        locationAdd = txt;
        try {
            new GetAddressTask().execute(location);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public address(Parcel source) {
        // TODO Auto-generated constructor stub
        lat = source.readDouble();
        lng = source.readDouble();
        //context = source.re
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(lat);
        dest.writeDouble(lng);
    }
    public static final Parcelable.Creator<address> CREATOR = new Parcelable.Creator<address>() {
        public address createFromParcel(Parcel source) {
            return new address(source);
        }
        public address[] newArray(int size) {
            return new address[size];
        }
    };

    private class GetAddressTask extends AsyncTask<Location, Void, String> {
        protected String doInBackground(Location... Params) {

            Geocoder geocoder = new Geocoder(context.getApplicationContext(), Locale.getDefault());
            Location loc = Params[0];
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String addressText;

            if(addresses.size()!=0) {
                Address address = addresses.get(0);
                addressText = address.getAddressLine(0) + " "
                        + address.getLocality() + " " + address.getCountryName();
                return addressText;

            }
            return "Address not found";
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                locationAdd.setText(result);
            } catch (Exception e) {
                e.printStackTrace();

            }

        }
    }
}
