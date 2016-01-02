package edu.stevens.cs522.myapplication.cloud;

import android.util.JsonReader;
import android.util.JsonToken;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import edu.stevens.cs522.myapplication.cloud.Request.Register;
import edu.stevens.cs522.myapplication.cloud.Request.Unregister;
import edu.stevens.cs522.myapplication.cloud.Request.PostMessage;

/**
 * Created by Sandeep on 3/25/2015.
 */
public class RestMethod {

    public Response perform(Register request) throws Exception {

        Response response = new Response();
        URL Url = null;
        try {
            Url = new URL(request.getRequestUri().toString());
        } catch (MalformedURLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        HttpURLConnection httpUrlConnection = null;
        try {
            httpUrlConnection = (HttpURLConnection) Url.openConnection();
            httpUrlConnection.setReadTimeout(1500);
            httpUrlConnection.setConnectTimeout(2000);
            httpUrlConnection.setRequestMethod("POST");
            addHeader(request, httpUrlConnection);
            int responseCode = httpUrlConnection.getResponseCode();
            response.setResponseCode(responseCode);
            if (responseCode == HttpURLConnection.HTTP_CREATED) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                                            httpUrlConnection.getInputStream()));
                StringBuilder str = new StringBuilder();
                String line = null;
                while ((line = reader.readLine())!=null) {
                    str.append(line + "");
                }
                StringReader srd = new StringReader(str.toString());
                JsonReader rd = new JsonReader(srd);

                rd.beginObject();
                while (rd.peek()!=JsonToken.END_OBJECT) {
                    String label = rd.nextName();
                    if (label.equals(Response._id)) {
                        response.setId(rd.nextString());
                    }
                }
                rd.close();
            }

        } catch (Exception e) {
            throw e;

        } finally {
            httpUrlConnection.disconnect();
        }
        return response;
    }

    public StreamingResponse perform(PostMessage request) throws Exception {

        URL Url = null;
        HttpURLConnection httpUrlConnection = null;
        StreamingResponse streaming = null;
        try {
            // Creating the URL and connecting to the server
            Url = new URL(request.getRequestUri().toString());
            httpUrlConnection = (HttpURLConnection) Url.openConnection();
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            httpUrlConnection.setReadTimeout(1500);
            httpUrlConnection.setConnectTimeout(2000);
            httpUrlConnection.setRequestMethod("POST");

            addHeader(request, httpUrlConnection);
            httpUrlConnection.setDoOutput(true);
            httpUrlConnection.setDoInput(true);
            httpUrlConnection.setRequestProperty("Content-Type","application/json");
            httpUrlConnection.setRequestProperty("Accept", "application/json");
            streaming = new StreamingResponse();
            streaming.connection = httpUrlConnection;

        } catch (Exception e) {
            throw e;

        }
        return streaming;

    }

    public Response perform(Unregister request) throws Exception {

        Response response = new Response();
        URL Url = null;
        try {
            Url = new URL(request.getRequestUri().toString());
        } catch (MalformedURLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        HttpURLConnection httpUrlConnection = null;
        try {

            httpUrlConnection = (HttpURLConnection) Url.openConnection();

            httpUrlConnection.setReadTimeout(1500);
            httpUrlConnection.setConnectTimeout(2000);
            httpUrlConnection.setRequestMethod("DELETE");

            int responseCode = httpUrlConnection.getResponseCode();
            response.setResponseCode(responseCode);


        } catch (Exception e) {
            throw e;

        } finally {
            httpUrlConnection.disconnect();
        }

        return response;
    }

    public void addHeader(Request req, HttpURLConnection http){
        Map<String, String> header = req.getRequestHeaders();
        Iterator it = header.entrySet().iterator();

        while(it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();
            http.setRequestProperty((String)pair.getKey(),(String)pair.getValue());
        }
    }
}
