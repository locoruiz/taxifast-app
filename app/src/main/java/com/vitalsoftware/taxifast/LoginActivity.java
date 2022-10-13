package com.vitalsoftware.taxifast;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.facebook.ProfileTracker;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;

import static android.Manifest.permission.READ_CONTACTS;
import static android.content.Context.MODE_PRIVATE;
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

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor>, HttpPost.HttpPostInterface {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private LoginButton loginButton;
    private CallbackManager callbackManager;

    boolean loggeado;

    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);
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

        AppEventsLogger.activateApp(this);

        SharedPreferences preferences = getSharedPreferences(getString(R.string.preferencias), MODE_PRIVATE);
        token = preferences.getString("token", "");
        if(token.trim().length() == 0)
            token = FirebaseInstanceId.getInstance().getToken();

        if (token == null){
            Toast.makeText(LoginActivity.this, "Hay problemas de conexion..", Toast.LENGTH_SHORT).show();

            token = "";
        }


        loggeado = preferences.getBoolean("login", false);
        if (loggeado){
            // Ahora se hace esto desde splash screen
            /*
            // ya estaba logueado!! hacer la sesion de nuevo
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("token", token);
            map.put("id", preferences.getInt("id", 0)+"");

            try{
                new HttpPost(getString(R.string.url)+"/loginu.php", HttpPost.getPostDataString(map), LoginActivity.this).execute();
            }catch (Exception e){
                Log.i("rosco", "Error antes del request:"+e.getMessage());
            }
            */
        }
        /*
        if (Profile.getCurrentProfile() != null){
            // Ya estaba loggueado con facebook
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(i);
        }
        */
        callbackManager = CallbackManager.Factory.create();

        loginButton = (LoginButton)findViewById(R.id.login_button);
        //loginButton.setReadPermissions(Arrays.asList("public_profile", "email"));

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            private ProfileTracker mProfileTracker;
            private Profile perfil;

            @Override
            public void onSuccess(LoginResult loginResult) {
                //Log.i("rosco", "User ID:" + loginResult.getAccessToken().getUserId()+"\n Auth Token:"+loginResult.getAccessToken().getToken() );

                if (Profile.getCurrentProfile() == null){
                    mProfileTracker = new ProfileTracker() {
                        @Override
                        protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                            perfil = currentProfile;
                            mProfileTracker.stopTracking();
                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put("uid", perfil.getId());
                            map.put("facebook", "1");
                            map.put("nombre", perfil.getFirstName() + " " + perfil.getMiddleName());
                            map.put("apellidos", perfil.getLastName());
                            map.put("token", token);
                            showProgress(true);
                            try{
                                new HttpPost(getString(R.string.url)+"/loginu.php", HttpPost.getPostDataString(map), LoginActivity.this).execute();
                            }catch (Exception e){
                                Log.i("rosco", "Error antes del request:"+e.getMessage());
                            }

                            SharedPreferences preferences = getSharedPreferences(getString(R.string.preferencias), MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("nombre", perfil.getFirstName() + " " + perfil.getMiddleName());
                            editor.putString("apellidos", perfil.getLastName());
                            editor.putString("telefono", "-----");
                            editor.putString("correo", "-----");
                            editor.putBoolean("login", true);
                            editor.putInt("esTaxi", 0);
                            editor.putBoolean("facebook", true);
                            editor.apply();
                        }
                    };
                }else{
                    perfil = Profile.getCurrentProfile();
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("uid", perfil.getId());
                    map.put("facebook", "1");
                    map.put("nombre", perfil.getFirstName() + " " + perfil.getMiddleName());
                    map.put("apellidos", perfil.getLastName());
                    map.put("token", token);
                    showProgress(true);
                    try{
                        new HttpPost(getString(R.string.url)+"/loginu.php", HttpPost.getPostDataString(map), LoginActivity.this).execute();
                    }catch (Exception e){
                        Log.i("rosco", "Error antes del request:"+e.getMessage());
                    }

                    SharedPreferences preferences = getSharedPreferences(getString(R.string.preferencias), MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("nombre", perfil.getFirstName() + " " + perfil.getMiddleName());
                    editor.putString("apellidos", perfil.getLastName());
                    editor.putString("telefono", "-----");
                    editor.putString("correo", "-----");
                    editor.putBoolean("login", true);
                    editor.putInt("esTaxi", 0);
                    editor.putBoolean("facebook", true);
                    editor.apply();
                }
            }

            @Override
            public void onCancel()
            {
                Log.i("rosco", "Se cancelo el login");
            }

            @Override
            public void onError(FacebookException e) {
                Log.i("rosco", "Hubo un error en el login");
            }
        });

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);


        Button mRegisterButton = (Button)findViewById(R.id.btnCrearCuenta);
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(i);
            }
        });

        TextView txtRec = (TextView)findViewById(R.id.txtLinkPassword);
        txtRec.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LoginActivity.this, RecuperarActivity.class);
                startActivity(i);
            }
        });
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPasswordView.setText("");
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        private StringBuilder response;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }


        private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
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

        @Override
        protected Boolean doInBackground(Void... params) {
            response = new StringBuilder();
            URL url = null;
            HttpURLConnection con = null;
            try{
                url = new URL(getString(R.string.url)+"/loginu.php");
            }catch (MalformedURLException e){
                Log.i("rosco", e.getMessage());
            }
            try {
                con = (HttpURLConnection)url.openConnection();
                con.setReadTimeout(10000);
                con.setConnectTimeout(15000);
                con.setRequestMethod("POST");
                con.setDoInput(true);
                con.setDoOutput(true);

                HashMap<String, String> map = new HashMap<>();
                map.put("email", mEmail);
                map.put("password", mPassword);
                map.put("token", token);
                OutputStream os = con.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(map));

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
                }
            } catch (Exception e) {
                Log.i("rosco", "Hubo un error al hacer el request:"+e.getMessage());
                return false;
            }finally {
                con.disconnect();
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                try{
                    if(response.toString().length() == 0)
                    {
                        Toast.makeText(App.getContext(), "Hubo un problema, intentelo mas tarde", Toast.LENGTH_SHORT);
                        return;
                    }
                    JSONObject obj = new JSONObject(response.toString());
                    int logro = obj.getInt("success");
                    if (logro == 0){
                        // no logro guardar
                        AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
                        alertDialog.setTitle("Error");
                        alertDialog.setMessage(obj.getString("mensaje"));
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                    }else{
                        Log.i("rosco", "hizo login bien a ver");
                        // login ok
                        SharedPreferences preferences = getSharedPreferences(getString(R.string.preferencias), MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("nombre", obj.getString("nombre"));
                        editor.putString("apellidos", obj.getString("apellidos"));
                        editor.putString("telefono", obj.getString("telefono"));
                        editor.putString("correo", mEmail);
                        editor.putString("password", mPassword);
                        editor.putBoolean("login", true);
                        int esTaxi = obj.getInt("esTaxi");
                        int activo = obj.getInt("activo");
                        editor.putBoolean("activo", activo == 1);
                        editor.putInt("esTaxi", esTaxi);
                        editor.putInt("idTaxista", obj.getInt("idTaxista"));
                        editor.putInt("id", obj.getInt("id"));
                        editor.putString("sess_id", obj.getString("sess_id"));
                        editor.apply();

                        Intent i = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(i);
                    }
                }catch (JSONException e){
                    Log.i("rosco", "error de json:"+e.getMessage()+"\n"+response.toString());
                }
            } else {
                // no logro guardar
                AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
                alertDialog.setTitle("Error");
                alertDialog.setMessage("Hubo un error, intentelo mas tarde");
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
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }


    // HttpPostInterface
    public void termino(JSONObject obj){
        try{
            showProgress(false);
            // hizo login desde facebook o ya estaba loggeado
            if (obj.getInt("success") == 1){
                if(loggeado){
                    SharedPreferences preferences = getSharedPreferences(getString(R.string.preferencias), MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("sess_id", obj.getString("sess_id"));
                    editor.apply();

                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(i);
                }else{
                    SharedPreferences preferences = getSharedPreferences(getString(R.string.preferencias), MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("telefono", "-----");
                    editor.putString("correo", "-----");
                    editor.putBoolean("login", true);
                    editor.putInt("esTaxi", 0);
                    editor.putBoolean("facebook", true);
                    editor.putInt("id", obj.getInt("id"));
                    editor.putString("sess_id", obj.getString("sess_id"));
                    editor.apply();

                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(i);
                }
            }else{
                AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
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
        AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
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

