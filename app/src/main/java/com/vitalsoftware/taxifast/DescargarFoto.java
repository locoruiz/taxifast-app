package com.vitalsoftware.taxifast;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.InputStream;

/**
 * Created by titin on 8/22/16.
 */

public  class DescargarFoto extends AsyncTask<String, Void, Bitmap> {
    ImageView imageView;
    ProgressBar progressBar;
    public interface DesgargarFotoDelegate{
        void termino(Bitmap bmp);
        void cancelo(String error);
    }

    public DescargarFoto() {

    }
    public DescargarFoto(ImageView iv, ProgressBar pb){
        imageView = iv;
        progressBar = pb;
    }
    public DescargarFoto(ImageView iv){
        imageView = iv;
    }

    private DesgargarFotoDelegate delegate;

    public DescargarFoto(DesgargarFotoDelegate del){
        delegate = del;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        BitmapFactory.Options options;
        try {
            options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in, null, options);
        } catch (Exception e) {
            Log.i("rosco", "Error en la foto:"+e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        if(imageView != null) {
            imageView.setImageBitmap(result);
            if(progressBar != null)
                progressBar.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            Log.i("rosco", "ya deberia haber cargado la foto!!");
        }
        if(delegate != null) delegate.termino(result);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        if(delegate != null) delegate.cancelo("Error en la conexion");
    }
}
