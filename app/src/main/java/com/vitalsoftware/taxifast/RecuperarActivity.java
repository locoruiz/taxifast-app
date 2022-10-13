package com.vitalsoftware.taxifast;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toolbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by titin on 7/25/16.
 */
public class RecuperarActivity extends AppCompatActivity implements HttpPost.HttpPostInterface {
    Button btn;
    ProgressBar pb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_password);

        android.support.v7.widget.Toolbar actionBarToolbar = (android.support.v7.widget.Toolbar)findViewById(R.id.toolbar);
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

        final AutoCompleteTextView correo = (AutoCompleteTextView)findViewById(R.id.email);
        btn = (Button)findViewById(R.id.btnEnviar);
        pb = (ProgressBar)findViewById(R.id.login_progress);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashMap<String, String> map = new HashMap<String, String>();
                String email = correo.getText().toString();
                // Check for a valid email address.
                boolean cancel = false;
                if (TextUtils.isEmpty(email)) {
                    correo.setError(getString(R.string.error_field_required));
                    cancel = true;
                } else if (!isEmailValid(email)) {
                    correo.setError(getString(R.string.error_invalid_email));
                    cancel = true;
                }

                if (cancel) {
                    // There was an error; don't attempt login and focus the first
                    // form field with an error.
                    correo.requestFocus();
                } else {
                    map.put("correo", email);
                    try{
                        new HttpPost(getString(R.string.url)+"/recuperar.php", HttpPost.getPostDataString(map), RecuperarActivity.this, 70000).execute();
                        showProgress(true);
                    }catch (Exception e){
                        Log.i("rosco", "Error antes del request:"+e.getMessage());
                    }
                }
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void showProgress(boolean show){
        pb.setVisibility(show ? View.VISIBLE : View.GONE);
        btn.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }
    // HttpPostInterface
    public void termino(JSONObject obj){
        try{
            showProgress(false);
            if (obj.getInt("success") == 1){
                AlertDialog alertDialog = new AlertDialog.Builder(RecuperarActivity.this).create();
                alertDialog.setTitle("Correo Enviado");
                alertDialog.setMessage(obj.getString("mensaje"));
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                            }
                        });
                alertDialog.show();
            }else{
                AlertDialog alertDialog = new AlertDialog.Builder(RecuperarActivity.this).create();
                alertDialog.setTitle("Error");
                alertDialog.setMessage(obj.getString("mensaje"));
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        }catch (JSONException e){
            Log.i("rosco", "error de json:"+e.getMessage());
        }
    }
    public void cancelo(String mensaje) {
        showProgress(false);
        AlertDialog alertDialog = new AlertDialog.Builder(RecuperarActivity.this).create();
        alertDialog.setTitle("Error");
        alertDialog.setMessage(mensaje);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
}
