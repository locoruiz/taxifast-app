package com.vitalsoftware.taxifast;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by titin on 7/12/16.
 */

public class HttpPost extends AsyncTask<Void, Void, Boolean> {
    Exception error;

    public interface HttpPostInterface {
        void termino(JSONObject obj);
        void cancelo(String error);
    }


    public static String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    private StringBuilder response;
    private String postDataString;
    private String url;
    private HttpPostInterface delegate;
    private int timeout;

    HttpPost(String urlx, String postDataStringx, HttpPostInterface del) {
        url = urlx;
        postDataString = postDataStringx;
        delegate = del;
        timeout = 30000;
    }
    HttpPost(String urlx, String postDataStringx, HttpPostInterface del, int timeoutx) {
        url = urlx;
        postDataString = postDataStringx;
        delegate = del;
        timeout = timeoutx;
    }


    @Override
    protected Boolean doInBackground(Void... params) {
        response = new StringBuilder();
        URL urlc = null;
        HttpURLConnection con = null;
        try{
            urlc = new URL(url);
        }catch (MalformedURLException e){
            Log.i("rosco", e.getMessage());
            error = e;
        }
        try {
            con = (HttpURLConnection)urlc.openConnection();
            con.setReadTimeout(timeout);
            con.setConnectTimeout(timeout);
            con.setRequestMethod("POST");
            con.setDoInput(true);
            con.setDoOutput(true);

            OutputStream os = con.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(postDataString);

            writer.flush();
            writer.close();
            os.close();

            int responseCode = con.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK){
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                while ((line = br.readLine()) != null){
                    response.append(line);
                }
            }else{
                error = new Exception("Error:"+responseCode);
                return false;
            }
        } catch (Exception e) {
            Log.i("rosco", "Hubo un error al hacer el request:"+e.getMessage());
            error = e;
            return false;
        }finally {
            con.disconnect();
        }
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {

        if (success) {
            try{
                if (response.toString().length() == 0){
                    if(delegate != null) delegate.cancelo("No hay respuesta del servidor");
                }else{
                    //Log.i("rosco", response.toString()); // solo en desarrollo
                }
                JSONObject obj = new JSONObject(response.toString());
                if(delegate != null) delegate.termino(obj);
            }catch (JSONException e){
                Log.i("rosco", "Json malformado:"+e.getMessage()+":\n"+response.toString());
                if(delegate != null) delegate.cancelo(response.toString());
            }
        } else {
            if(delegate != null) delegate.cancelo(error.getMessage());
        }
    }

    @Override
    protected void onCancelled() {
        if(delegate != null) {
            if (error == null)
                delegate.cancelo("Se cancelo el request");
            else
                delegate.cancelo(error.getMessage());
        }
    }
}