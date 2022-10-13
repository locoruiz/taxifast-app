package com.vitalsoftware.taxifast;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


/**
 * Created by titin on 8/22/16.
 */
public class TaxistaActivity extends AppCompatActivity {
    private Taxista taxista;
    private ImageView foto;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taxista);

        Toolbar actionBarToolbar = (Toolbar)findViewById(R.id.mi_toolbar);
        setSupportActionBar(actionBarToolbar);

        Window window = this.getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        if (Build.VERSION.SDK_INT >= 21){
            window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimaryDark));
        }



        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        taxista = (Taxista)getIntent().getSerializableExtra("taxista");

        ((TextView)findViewById(R.id.txtNombre)).setText(taxista.nombre);
        ((TextView)findViewById(R.id.txtTelefono)).setText("Telefono:"+taxista.telefono);
        ((TextView)findViewById(R.id.txtLicencia)).setText("Licencia:"+taxista.licencia);


        ((Button)findViewById(R.id.btnLlamar)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:"+taxista.telefono));
                startActivity(callIntent);
            }
        });
        foto = (ImageView)findViewById(R.id.foto);
        //taxista.imageView = foto;
        //taxista.cargarFoto();
        if (taxista.foto == null){
            new DescargarFoto(foto, (ProgressBar)findViewById(R.id.pbCargando)).execute(App.getContext().getResources().getString(R.string.url)+"/"+taxista.urlFoto);
        }else{
            foto.setImageBitmap(taxista.foto);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_llamada, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {

            finish(); // close this activity and return to preview activity (if there is any)
        }else if(item.getItemId() == R.id.btnLlamar){

            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:"+taxista.telefono));
            startActivity(callIntent);
        }
        return super.onOptionsItemSelected(item);
    }
}
