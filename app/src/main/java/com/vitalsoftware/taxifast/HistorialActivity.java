package com.vitalsoftware.taxifast;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import static android.support.v7.appcompat.R.styleable.Toolbar;

/**
 * Created by titin on 11/23/16.
 */

public class HistorialActivity extends AppCompatActivity implements HttpPost.HttpPostInterface {

    class MiAdapter extends ArrayAdapter<Pedido>{
        private List<Pedido> lista;
        private Context context;

        public MiAdapter(List<Pedido> pedidos, Context c){
            super(c, 0, pedidos);
            lista = pedidos;
            context = c;
        }

        @Override
        public int getCount() {
            return lista.size();
        }


        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Nullable
        @Override
        public Pedido getItem(int position) {
            return lista.get(position);
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            if (view == null){
                LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view =  mInflater.inflate(R.layout.pedido_item, null);
            }
            Pedido pedido = lista.get(i);
            ((TextView)view.findViewById(R.id.lblDir)).setText(pedido.direccion);
            ((TextView)view.findViewById(R.id.lblRef)).setText(pedido.referencia);

            return view;
        }
    }

    ListView listView;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

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

        HashMap<String, String> mapa = new HashMap<>();
        int id = getIntent().getIntExtra("id", 0);
        mapa.put("id_usuario", ""+id);
        mapa.put("sess_id", getIntent().getStringExtra("sess_id"));
        try {
            new HttpPost(getString(R.string.url)+ "/historial.php", HttpPost.getPostDataString(mapa), HistorialActivity.this).execute();
        }catch (Exception e){
            Log.i("rosco", "no se pudo buscar los pedidos:"+e.getMessage());
            finish();
        }
        listView = (ListView)findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Pedido pedido = (Pedido) adapterView.getItemAtPosition(i);
                Intent intent = new Intent();
                intent.putExtra("pedido", pedido);

                setResult(1, intent);
                finish();
            }
        });
        progressBar = (ProgressBar)findViewById(R.id.login_progress);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            setResult(0);
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void termino(JSONObject obj) {

        try{
            if(obj.has("mensaje"))
                Log.i("rosco", obj.getString("mensaje"));
            if(obj.getInt("success") == 1){
                JSONArray historial = obj.getJSONArray("carreras");
                ArrayList<Pedido> pedidos = new ArrayList<>();
                for (int i = 0; i < historial.length(); i++){
                    Pedido pedido = new Pedido();
                    JSONObject obj1 = historial.getJSONObject(i);
                    pedido.direccion = obj1.getString("direccion");
                    pedido.referencia = obj1.getString("referencia");
                    pedido.latitud = obj1.getDouble("latitud");
                    pedido.longitud = obj1.getDouble("longitud");
                    pedidos.add(pedido);
                }
                progressBar.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
                MiAdapter miAdapter = new MiAdapter(pedidos, HistorialActivity.this);
                listView.setAdapter(miAdapter);
            }else{
                AlertDialog alertDialog = new AlertDialog.Builder(HistorialActivity.this).create();
                alertDialog.setTitle("Error");
                alertDialog.setMessage(obj.getString("mensaje"));
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                setResult(0);
                                finish();
                            }
                        });
                alertDialog.show();
            }
        }catch (JSONException e){
            Log.i("rosco", "Error de JSON:"+e.getMessage());
        }
    }

    @Override
    public void cancelo(String error) {
        AlertDialog alertDialog = new AlertDialog.Builder(HistorialActivity.this).create();
        alertDialog.setTitle("Error");
        alertDialog.setMessage(error);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        setResult(0);
                        finish();
                    }
                });
        alertDialog.show();
    }
}
