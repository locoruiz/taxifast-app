package com.vitalsoftware.taxifast;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.Image;
import android.util.Log;
import android.widget.ImageView;

import java.io.Serializable;
import java.net.URL;

/**
 * Created by titin on 8/22/16.
 */
public class Taxista implements Serializable, DescargarFoto.DesgargarFotoDelegate {
    public int id;
    public String nombre;
    public String licencia;
    public String telefono;

    public Bitmap foto;
    public String urlFoto;

    public double latitud, longitud;

    public ImageView imageView; // donde mostrar la foto

    public Taxista(){

    }
    public Taxista(int idx, String nombrex, String licenciax, String urlFotox, double lat, double longi, String tel){
        id = idx;
        nombre = nombrex;
        licencia = licenciax;
        latitud = lat;
        longitud = longi;
        telefono = tel;
        urlFoto = urlFotox;
    }

    public void cargarFoto(){
        if (foto == null)
            new DescargarFoto(this).execute(App.getContext().getResources().getString(R.string.url)+"/"+urlFoto);
        else{
            imageView.setImageBitmap(foto);
            Log.i("rosco", "ya estaba cargada la foto...");
        }
    }

    @Override
    public void termino(Bitmap bmp){
        foto = bmp;
        if (imageView != null)
            imageView.setImageBitmap(foto);
        else
            Log.i("rosco", "imageView esta nulo!!!");

        Log.i("rosco", "se cargo la foto");
    }

    @Override
    public void cancelo(String error) {
        Log.i("rosco", "No pudo cargar la foto:"+error);
    }
}
