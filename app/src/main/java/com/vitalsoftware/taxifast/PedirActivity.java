package com.vitalsoftware.taxifast;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by titin on 7/28/16.
 */
public class PedirActivity extends AppCompatActivity implements HttpPost.HttpPostInterface {

    public static final double latInfTarija = -22.04176211756073;
    public static final double longInfTarija = -65.09280625730753;

    public static final double latSupTarija = -21.275128389766692;
    public static final double longSupTarija = -63.89789052307606;


    AutoCompleteTextView origen;
    EditText referencia;
    AutoCompleteTextView destino;
    double latOrigen;
    double longOrigen;
    Address aDestino;
    Geocoder mGeocoder;

    ScrollView scrollView;
    LinearLayout llEsperar;

    boolean esperando;

    List<NomDir> busq;

    int id;
    HttpPost post;
    TextView lblInfo;

    String sess_id;
    boolean activo;
    boolean repetido;
    int idPedido;

    @Override
    protected void onStart() {
        super.onStart();
        activo = true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedir);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Window window = this.getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        if (Build.VERSION.SDK_INT >= 21){
            window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimaryDark));
        }

        SharedPreferences preferences = getSharedPreferences(getString(R.string.preferencias), MODE_PRIVATE);

        id = preferences.getInt("id", 0);
        sess_id = preferences.getString("sess_id", "");

        Intent i = getIntent();

        repetido = i.getBooleanExtra("repetido", false);

        latOrigen =  i.getExtras().getDouble("latOrigen");
        longOrigen =  i.getExtras().getDouble("longOrigen");
        String dirO = i.getStringExtra("dirO");

        origen = (AutoCompleteTextView)findViewById(R.id.origen);
        origen.setText(dirO);


        mGeocoder = new Geocoder(this, Locale.getDefault());

        destino = (AutoCompleteTextView)findViewById(R.id.destino);

        destino.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!TextUtils.isEmpty(destino.getText().toString())){
                    if (busq.size() < 30){
                        NomDir task = new NomDir(destino.getText().toString());
                        busq.add(task);
                        task.execute();
                    }
                }else
                    aDestino = null;
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!TextUtils.isEmpty(destino.getText().toString())){
                    if (busq.size() < 30){
                        NomDir task = new NomDir(destino.getText().toString());
                        busq.add(task);
                        task.execute();
                    }
                }else
                    aDestino = null;
            }
        });

        destino.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AddressAdapter ad = (AddressAdapter)destino.getAdapter();

                destino.setText(ad.direccion(i));
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(destino.getWindowToken(), 0);

                aDestino = ad.getItem(i);
            }
        });


        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        esperando = false;

        busq = new ArrayList<>();

        referencia = (EditText)findViewById(R.id.referencia);

        scrollView = (ScrollView)findViewById(R.id.login_form);
        llEsperar = (LinearLayout)findViewById(R.id.llEspera);

        lblInfo = (TextView)findViewById(R.id.lblInfo);

        Button btnPedir = (Button)findViewById(R.id.btnPedir);
        btnPedir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (origen.getText().toString().trim().equals(""))
                {
                    Toast.makeText(PedirActivity.this, "Debe especificar una direccion!", Toast.LENGTH_SHORT).show();
                    return;
                }
                lblInfo.setText("Buscando el taxi mas cercano");
                showProgress(true);
                esperando = true;
                HashMap<String, String> datos = new HashMap<String, String>();
                datos.put("id", id+"");
                datos.put("sess_id", sess_id);
                datos.put("latOrigen", latOrigen+"");
                datos.put("longOrigen", longOrigen+"");
                datos.put("direccionOrigen", origen.getText().toString());
                datos.put("referencia", referencia.getText().toString());
                datos.put("direccionDestino", destino.getText().toString());
                datos.put("estado", "HA");
                idPedido = 0;
                if(repetido)
                    datos.put("repetido", "1");
                if (aDestino != null){
                    datos.put("latDestino", aDestino.getLatitude()+"");
                    datos.put("longDestino", aDestino.getLongitude()+"");
                }else{
                    datos.put("latDestino", "0");
                    datos.put("longDestino", "0");
                }
                try{
                    post = new HttpPost(getString(R.string.url)+"/pedirTaxi.php", HttpPost.getPostDataString(datos), PedirActivity.this, 40000);
                    post.execute();
                }catch (UnsupportedEncodingException e){
                    Log.i("rosco", "Error de encoding:"+e.getMessage());
                }
            }
        });
        Button btnCancelar = (Button)findViewById(R.id.btnCancelar);
        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelarPedido();
            }
        });
        if(repetido) {
            referencia.setText(i.getStringExtra("referencia"));
            Log.i("rosco", "Estamos en repetido");
        }else
            Log.i("rosco", "No es repetido");
    }

    @Override
    protected void onStop() {
        super.onStop();
        activo = false;
    }

    void cancelarPedido(){
        showProgress(false);
        esperando = false;
        post.cancel(true);
        post = null;

        HashMap<String, String> datos = new HashMap<String, String>();
        datos.put("id", id+"");
        datos.put("sess_id", sess_id);
        datos.put("id_carrera", idPedido+"");

        try{
            new HttpPost(getString(R.string.url)+"/cancelar.php", HttpPost.getPostDataString(datos), PedirActivity.this, 40000).execute();
        }catch (UnsupportedEncodingException e) {
            Log.i("rosco", e.getMessage());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            if (esperando){
                cancelarPedido();
            }
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }

    public class NomDir extends AsyncTask<Void, Void, Boolean> {
        private String result;
        private LatLng location;
        private boolean reverse;
        private String query;
        private List<Address> addresses;

        public NomDir(LatLng ll){
            location = ll;
            reverse = true;
        }
        public NomDir(String nom){
            query = nom;
            reverse = false;
            addresses = new ArrayList<Address>();
        }

        public NomDir(LatLng l, String str, boolean rev){
            location = l;
            reverse = rev;
            query = str;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                if (reverse) {
                    // NO HAY REVERSE
                }else{
                    List<Address> lista = mGeocoder.getFromLocationName(query, 6, latInfTarija, longInfTarija, latSupTarija, longSupTarija);
                    for (Address address : lista){
                        String ciudad = address.getAdminArea();
                        String pais = address.getCountryName();
                        String numero = address.getSubThoroughfare();
                        String calle = address.getThoroughfare();
                        if (calle != null && ciudad != null && pais != null){
                            addresses.add(address);
                        }
                    }
                }
            }catch (IOException e){
                Log.i("rosco", "Error de geocoder:"+e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (reverse){
                // no hay reverse en esta parte
                //if (result != null && result.length() > 0)txtDesde.setText(result);
                //reqs.remove(this);
                //if (reqs.size() == 0) editando = false;
            }else{
                if (addresses.size() > 0){
                    AddressAdapter ad = new AddressAdapter(addresses, PedirActivity.this);
                    destino.setAdapter(ad);
                    destino.showDropDown();
                }else{
                    //Log.i("rosco", "no hay direcciones!");
                }
                busq.remove(this);
            }
        }
    }

    void showProgress(Boolean si){
        if (si){
            scrollView.setVisibility(View.GONE);
            llEsperar.setVisibility(View.VISIBLE);
        }else{
            scrollView.setVisibility(View.VISIBLE);
            llEsperar.setVisibility(View.GONE);
        }
    }

    @Override
    public void cancelo(String error) {
        Log.i("rosco", error);
        showProgress(false);
        if (activo && esperando){
            AlertDialog alertDialog = new AlertDialog.Builder(PedirActivity.this).create();
            alertDialog.setTitle("Error");
            alertDialog.setMessage(error);
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
    }

    @Override
    public void termino(JSONObject obj) {
        // success 1: Ya tiene taxi
        // success 2: No hay taxis, seguir esperando
        // success 3: Session expirada
        // success 0: Hubo algun error
        try{
            if(obj.has("mensaje")) Log.i("rosco", obj.getString("mensaje"));
            if (obj.getInt("success") ==  1){
                if (!esperando)
                    return;
                Intent i = new Intent();
                Taxista taxista = new Taxista(obj.getInt("id_taxista"), obj.getString("nombre"),
                                                obj.getString("licencia"), obj.getString("foto"), obj.getDouble("latTaxi"), obj.getDouble("longTaxi"), obj.getString("telefono"));

                i.putExtra("taxista", taxista);
                i.putExtra("id_pedido", obj.getInt("id_pedido"));
                setResult(1, i);
                finish();
            }else if(obj.getInt("success") == 2){

                lblInfo.setText("No hay taxis libres, buscando de nuevo...");
                HashMap<String, String> datos = new HashMap<String, String>();
                datos.put("id", id+"");
                datos.put("id_pedido", ""+obj.getInt("id_pedido"));
                datos.put("sess_id", sess_id);
                Log.i("rosco", "no hay taxis:id = "+id+" id_pedido = "+obj.getInt("id_pedido"));
                idPedido = obj.getInt("id_pedido");
                try{
                    post = new HttpPost(getString(R.string.url)+"/pedirTaxi.php", HttpPost.getPostDataString(datos), PedirActivity.this, 40000);
                    post.execute();
                }catch (UnsupportedEncodingException e){
                    Log.i("rosco", "Error de encoding:"+e.getMessage());
                }
            }else if(obj.getInt("success") == 0){
                showProgress(false);
                if (!activo)
                    return;
                AlertDialog alertDialog = new AlertDialog.Builder(PedirActivity.this).create();
                alertDialog.setTitle("Error");
                alertDialog.setMessage(obj.getString("mensaje"));
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }else if(obj.getInt("success") == 3){
                Log.i("rosco", obj.getString("mensaje"));
                AlertDialog alertDialog = new AlertDialog.Builder(PedirActivity.this).create();
                alertDialog.setTitle("Error");
                alertDialog.setMessage(obj.getString("mensaje"));
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                showProgress(false);
                                setResult(3, null);
                                finish();
                            }
                        });
                alertDialog.show();
            }
        }catch (JSONException e){
            Log.i("rosco", "error de json:"+e.getMessage());
        }
    }
}
