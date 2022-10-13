package com.vitalsoftware.taxifast;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.util.JsonReader;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarException;

import javax.net.ssl.HttpsURLConnection;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class RegisterActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText mNombreView;
    private EditText mApellidoView;
    private EditText mTelefonoView;
    private EditText mPasswordRView;
    private EditText mPasswordViejoView;
    private Button btnCambiarP;
    private View mProgressView;
    private View mLoginFormView;

    boolean editando;
    boolean editandoPas;
    boolean editandoF;
    String uid;
    int id;
    String sess_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

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


        // Set up the login form.
        mNombreView = (EditText)findViewById(R.id.nombre);
        mApellidoView = (EditText)findViewById(R.id.apellidos);
        mTelefonoView = (EditText)findViewById(R.id.telefono);
        mPasswordRView = (EditText)findViewById(R.id.passwordR);
        mPasswordViejoView = (EditText)findViewById(R.id.passwordViejo);

        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordRView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        id = 0;
        btnCambiarP = (Button)findViewById(R.id.btnPasswords);
        editandoPas = false;

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        editandoF = false;
        uid = "";

        SharedPreferences preferences = getSharedPreferences(getString(R.string.preferencias), MODE_PRIVATE);
        if (preferences.getBoolean("login", false)){
            id = preferences.getInt("id", 0);
            editando = true;
            setTitle("Editar Cuenta");
            if (preferences.getBoolean("facebook", false)){
                mEmailView.setVisibility(View.GONE);
                editandoF = true;
                uid = preferences.getString("uid", "nada a ver");
            }else{
                mEmailView.setText(preferences.getString("correo", "no hay correo"));
                btnCambiarP.setVisibility(View.VISIBLE);
            }
            sess_id = preferences.getString("sess_id", "0");
            mPasswordView.setVisibility(View.GONE);
            mPasswordRView.setVisibility(View.GONE);
            mNombreView.setText(preferences.getString("nombre", "no hay nombre"));
            mApellidoView.setText(preferences.getString("apellidos", "no tiene apellidos!"));
            mTelefonoView.setText(preferences.getString("telefono", "no hay telefono"));
            btnCambiarP.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    mPasswordViejoView.setVisibility(View.VISIBLE);
                    mPasswordRView.setVisibility(View.VISIBLE);
                    mPasswordView.setVisibility(View.VISIBLE);
                    btnCambiarP.setVisibility(View.GONE);
                    editandoPas = true;
                }
            });
        }else{
            editando = false;
            sess_id = "0";
        }



        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }



    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
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
        mNombreView.setError(null);
        mApellidoView.setError(null);
        mTelefonoView.setError(null);
        mPasswordRView.setError(null);
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String nombre = mNombreView.getText().toString();
        String apellido = mApellidoView.getText().toString();
        String telefono = mTelefonoView.getText().toString();

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String passwordR = mPasswordRView.getText().toString();
        String passwordViejo = mPasswordViejoView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(nombre)){
            mNombreView.setError(getString(R.string.error_field_required));
            focusView = mNombreView;
            cancel = true;
        }
        if (TextUtils.isEmpty(apellido)){
            mApellidoView.setError(getString(R.string.error_field_required));
            if(focusView == null)focusView = mApellidoView;
            cancel = true;
        }
        if (!editando || (editando && editandoPas)){
            if (TextUtils.isEmpty(passwordViejo) && editando){
                mPasswordViejoView.setError(getString(R.string.error_field_required));
                if(focusView == null)focusView = mPasswordViejoView;
                cancel = true;
            }
            if (TextUtils.isEmpty(password)){
                mPasswordView.setError(getString(R.string.error_field_required));
                if(focusView == null)focusView = mPasswordView;
                cancel = true;
            }
            if (TextUtils.isEmpty(passwordR)){
                mPasswordRView.setError(getString(R.string.error_field_required));
                if(focusView == null)focusView = mPasswordRView;
                cancel = true;
            }

            // Check for a valid password, if the user entered one.
            if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
                mPasswordView.setError(getString(R.string.error_invalid_password));
                if(focusView == null)focusView = mPasswordView;
                cancel = true;
            }
            if (!password.equals(passwordR)) {
                mPasswordRView.setError("Las contraseñas no coinciden");
                if(focusView == null)focusView = mPasswordView;
                cancel = true;
            }
        }

        if (!editandoF){
            // Check for a valid email address.
            if (TextUtils.isEmpty(email)) {
                mEmailView.setError(getString(R.string.error_field_required));
                if(focusView == null)focusView = mEmailView;
                cancel = true;
            } else if (!isEmailValid(email)) {
                mEmailView.setError(getString(R.string.error_invalid_email));
                if(focusView == null)focusView = mEmailView;
                cancel = true;
            }
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(nombre, apellido, telefono, email, password, passwordViejo, editando, editandoF, uid, sess_id);
            mAuthTask.execute((Void) null);
            Log.i("rosco", "registrando....");
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
                new ArrayAdapter<>(RegisterActivity.this,
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

        private final String mNombre;
        private final String mApellidos;
        private final String mTelefono;
        private final String mEmail;
        private final String mPassword;
        private StringBuilder response;
        private boolean editando;
        private String passViejo;
        private boolean editandoF;
        private String uid;
        private String sesi;
        UserLoginTask(String nombre, String apellidos, String telefono, String email, String password, String passwordViejo, boolean edt, boolean editF, String id, String sesix) {
            mNombre = nombre;
            mApellidos = apellidos;
            mTelefono = telefono;
            mEmail = email;
            mPassword = password;
            editando = edt;
            passViejo = passwordViejo;
            editandoF = editF;
            uid = id;
            sesi = sesix;
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
                url = new URL(getString(R.string.url)+"/register.php");
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
                map.put("nombre", mNombre);
                map.put("apellidos", mApellidos);
                map.put("telefono", mTelefono);
                map.put("email", mEmail);
                map.put("id", id+"");
                map.put("sess_id", sesi);

                Log.i("rosco", "session id = "+sesi);

                if(editandoF){
                    map.put("editandoF", editandoF+"");
                }

                map.put("password", mPassword);
                if (editando)
                    map.put("editando", editando+"");
                map.put("passwordViejo", passViejo);

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
                    JSONObject obj = new JSONObject(response.toString());
                    int logro = obj.getInt("success");
                    if (logro == 0){
                        // no logro guardar
                        AlertDialog alertDialog = new AlertDialog.Builder(RegisterActivity.this).create();
                        alertDialog.setTitle("Error");
                        alertDialog.setMessage(obj.getString("mensaje"));
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();

                    }else if(logro == 1){
                        // registro ok
                        SharedPreferences preferences = getSharedPreferences(getString(R.string.preferencias), MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("nombre", mNombre);
                        editor.putString("apellidos", mApellidos);
                        editor.putString("telefono", mTelefono);
                        if(!editandoF) editor.putString("correo", mEmail);
                        editor.putBoolean("login", true);
                        if (!editando) editor.putInt("id", obj.getInt("id"));
                        editor.apply();
                        if (!editando){
                            editor.putString("sess_id", obj.getString("sess_id"));
                            Intent i = new Intent(RegisterActivity.this, MainActivity.class);
                            startActivity(i);
                        }
                        finish();
                    }else if(logro == 2){
                        // ya estaba registrado ese correo
                        mEmailView.setError("Ya esta registrado este correo");
                        mEmailView.requestFocus();
                    }else if(logro == 3){
                        // session expirada!
                        Log.i("rosco", obj.getString("mensaje"));
                        AlertDialog alertDialog = new AlertDialog.Builder(RegisterActivity.this).create();
                        alertDialog.setTitle("Error");
                        alertDialog.setMessage(obj.getString("mensaje"));
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        setResult(3, null);
                                        dialog.dismiss();
                                        finish();
                                    }
                                });
                        alertDialog.show();
                    }
                }catch (JSONException e){
                    Log.i("rosco", "error de json:"+e.getMessage()+"\n"+response.toString());
                    Log.i("rosco", "json = "+e.getMessage()+"\n"+response.toString());
                }
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

