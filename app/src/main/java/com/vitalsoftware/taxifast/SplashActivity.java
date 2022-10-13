package com.vitalsoftware.taxifast;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.LoginManager;

/**
 * Created by titin on 10/5/16.
 */

public class SplashActivity extends Activity implements HttpPost.HttpPostInterface
{
    private static final long DELAY = 3000;
    private boolean scheduled = false;
    private Timer splashTimer;
    private String token;
    boolean loggeado;
    boolean tiempo;
    boolean tiempoLog;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        Window window = this.getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        if (Build.VERSION.SDK_INT >= 21){
            window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimaryDark));
        }

        AppEventsLogger.activateApp(this);
        tiempo = false;
        tiempoLog = false;

        SharedPreferences preferences = getSharedPreferences(getString(R.string.preferencias), MODE_PRIVATE);
        token = preferences.getString("token", "");
        if(token.trim().length() == 0)
            token = FirebaseInstanceId.getInstance().getToken();

        if (token == null){
            //Toast.makeText(SplashActivity.this, "Hay problemas de conexion..", Toast.LENGTH_SHORT).show();
            //while (token == null){
            //  token = FirebaseInstanceId.getInstance().getToken();
            //}
            token = "";
        }else{
            // hay token
        }

        loggeado = preferences.getBoolean("login", false);
        if (loggeado){
            // ya estaba logueado!! hacer la sesion de nuevo
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("token", token);
            map.put("id", preferences.getInt("id", 0)+"");

            try{
                new HttpPost(getString(R.string.url)+"/loginu.php", HttpPost.getPostDataString(map), SplashActivity.this).execute();
            }catch (Exception e){
                Log.i("rosco", "Error antes del request:"+e.getMessage());
            }
        }else
            tiempoLog = true;


        splashTimer = new Timer();
        splashTimer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                tiempo = true;
                if (tiempoLog){
                    SplashActivity.this.finish();
                    if (loggeado){
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    }else {
                        if (Profile.getCurrentProfile() != null){
                            // Ya estaba loggueado con facebook
                            LoginManager.getInstance().logOut();
                        }
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    }
                    finish();
                }
            }
        }, DELAY);
        scheduled = true;
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (scheduled)
            splashTimer.cancel();
        splashTimer.purge();
    }


    // HttpPostInterface
    public void termino(JSONObject obj){
        try{
            // hizo login desde facebook o ya estaba loggeado
            if (obj.getInt("success") == 1){
                SharedPreferences preferences = getSharedPreferences(getString(R.string.preferencias), MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("sess_id", obj.getString("sess_id"));
                int activo = obj.getInt("activo");
                editor.putBoolean("activo", activo == 1);
                editor.apply();
                if (tiempo){
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                }
            }

        }catch (JSONException e){
            tiempoLog = true;
            Log.i("rosco", "error de json:"+e.getMessage());
        }
        if (tiempo){
            if (Profile.getCurrentProfile() != null){
                // Ya estaba loggueado con facebook
                LoginManager.getInstance().logOut();
            }
            Intent i = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        }else
            tiempoLog = true;
    }
    public void cancelo(String mensaje) {
        if (tiempo){
            if (Profile.getCurrentProfile() != null){
                // Ya estaba loggueado con facebook
                LoginManager.getInstance().logOut();
            }
            Intent i = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        }
        tiempoLog = true;
    }
}