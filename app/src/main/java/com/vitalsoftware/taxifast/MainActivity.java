package com.vitalsoftware.taxifast;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SubMenu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Permission;

import com.google.android.gms.drive.query.internal.LogicalFilter;
import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import android.os.Handler;

import javax.net.ssl.HttpsURLConnection;

import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.MODE_APPEND;
import static android.content.Context.MODE_MULTI_PROCESS;
import static android.content.Context.MODE_PRIVATE;
import static android.support.v7.appcompat.R.styleable.Toolbar;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
                    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener,
                    HttpPost.HttpPostInterface{

    public static final int DistanciaMinima = 80;

    private GoogleMap mMap;
    private LatLng myPosition;
    private Geocoder mGeocoder;
    LocationManager mLocationManager;
    List<NomDir> reqs;
    List<NomDir> busq;

    public static final double latInfTarija = -22.04176211756073;
    public static final double longInfTarija = -65.09280625730753;

    public static final double latSupTarija = -21.275128389766692;
    public static final double longSupTarija = -63.89789052307606;
    public static final ArrayList<String> coords_tarija = new ArrayList<String>(Arrays.asList(
            "-22.078021, -65.000527",
            "-22.215394, -64.561074",
            "-22.372953, -64.544594",
            "-22.560773, -64.407265",
            "-22.643182, -64.440224",
            "-22.719207, -64.404519",
            "-22.740740, -64.348214",
            "-22.867335, -64.313882",
            "-22.779997, -64.267190",
            "-22.563310, -64.242470",
            "-22.403428, -64.116128",
            "-22.009285, -63.940346",
            "-21.999099, -63.709634",
            "-22.053840, -63.682168",
            "-21.999735, -63.660195",
            "-22.004887, -62.809936",
            "-22.166340, -62.786427",
            "-22.252117, -62.655057",
            "-21.056863, -62.260947",
            "-20.999169, -62.266745",
            "-21.003839, -63.977586",
            "-20.947787, -65.178176",
            "-22.080991, -65.215695"
    ));

    public  static  final LatLng plaza = new LatLng(-21.533875205624717, -64.73419189453125);

    public final static double AVERAGE_RADIUS_OF_EARTH = 6371;

    MenuItem titNombre;

    AutoCompleteTextView txtDesde;

    private boolean editando;
    private boolean taxiActivo;
    private LatLng ultimaLocacion;

    ImageView ivUsu;

    ImageButton btnPedir;
    Button btnVerTaxista, btnCancelar, btnInformar;
    Taxista taxista;
    Marker markerTaxi;
    Marker markerUsu;

    HttpPost actualizarTaxi;

    boolean esTaxi;
    boolean trabajando;
    boolean enCamino;
    boolean quiereSalir;

    int itemT;
    MenuItem itemTrabajar;

    private LocationRequest locationRequest;
    private FusedLocationProviderApi fusedLocationProviderApi;
    private GoogleApiClient googleApiClient;

    private boolean cargoElMapa;

    private HttpPost longPoll;
    private RelativeLayout rlPedido;
    private TextView tvDireccion;
    private ProgressBar progressBar;

    private LatLng direccionOrigen;

    private String dirOrigen, refOrigen;

    int idTaxista, idPedido, id;
    String sess_id, estado;
    private final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 459;
    boolean tienePermiso, enTaxi;
    Handler timerHandler = new Handler();
    int tiempoPasado, tiempoEspera=20;

    ArrayList<Pedido> pedidos;
    int mostrandoPedido;
    String referencia, dirPed;
    double latPed, longPed;
    boolean PedidoRepetido;
    boolean appVisible;

    Runnable timerRunnable = new Runnable(){
        @Override
        public void run() {
            tiempoPasado++;
            if (tiempoPasado >= tiempoEspera){
                timerHandler.removeCallbacks(timerRunnable);
                rlPedido.setVisibility(View.GONE);
                // continuar con mas pedidos
                mostrandoPedido++;

                if (mostrandoPedido < pedidos.size()) {
                    mostrarNuevoPedido();
                    return;
                }
                HashMap<String, String> datos = new HashMap<String, String>();
                datos.put("id", id+"");
                datos.put("id_taxista", ""+idTaxista);
                datos.put("sess_id", sess_id);
                datos.put("latTaxi", myPosition.latitude+"");
                datos.put("longTaxi", myPosition.longitude+"");

                try{
                    longPoll = new HttpPost(getString(R.string.url)+"/pedidos.php", HttpPost.getPostDataString(datos), MainActivity.this, 40000);
                    longPoll.execute();
                }catch (Exception e){
                    Log.i("rosco", "error en el pedido:"+e.getMessage());
                }

                return;
            }

            if(tiempoPasado % 5 == 0 || tiempoPasado == 1){
                if (appVisible)
                    sounds.play(soundID, 1, 1, 0, 0, 1);
            }

            timerHandler.postDelayed(timerRunnable, 1000);
        }
    };


    SoundPool sounds;
    int soundID;

    protected void createSoundPool() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            createNewSoundPool();
        } else {
            createOldSoundPool();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void createNewSoundPool(){
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        sounds = new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .build();
    }

    @SuppressWarnings("deprecation")
    protected void createOldSoundPool(){
        sounds = new SoundPool(5, AudioManager.STREAM_MUSIC,0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tienePermiso = false;
        enTaxi = false;
        quiereSalir = false;
        cargoElMapa = false;

        createSoundPool();
        soundID = sounds.load(MainActivity.this, R.raw.ticktock, 1);

        enCamino = false;

        reqs = new ArrayList<NomDir>();
        busq = new ArrayList<NomDir>();
        pedidos = new ArrayList<Pedido>();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //View headerLayout = navigationView.getHeaderView(0);

        SharedPreferences preferences = getSharedPreferences(getString(R.string.preferencias), MODE_PRIVATE);

        id = preferences.getInt("id", 0);
        sess_id = preferences.getString("sess_id", "");
        ivUsu = (ImageView)findViewById(R.id.imageView);
        rlPedido = (RelativeLayout)findViewById(R.id.rlPedido);
        tvDireccion = (TextView)rlPedido.findViewById(R.id.titDireccion);
        progressBar = (ProgressBar) rlPedido.findViewById(R.id.progressBar);

        Menu menuT = navigationView.getMenu();
        MenuItem itemi = menuT.getItem(0);
        SubMenu subMenuT = itemi.getSubMenu();
        titNombre = subMenuT.getItem(0);

        titNombre.setTitle(preferences.getString("nombre", "opa") + " " + preferences.getString("apellidos", "oporoncio"));


        txtDesde = (AutoCompleteTextView) findViewById(R.id.txtBusqueda);

        txtDesde.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!editando && !TextUtils.isEmpty(txtDesde.getText().toString())){
                    if (busq.size() < 30){
                        NomDir task = new NomDir(txtDesde.getText().toString());
                        busq.add(task);
                        task.execute();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        txtDesde.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AddressAdapter ad = (AddressAdapter)txtDesde.getAdapter();

                txtDesde.setText(ad.direccion(i));
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(txtDesde.getWindowToken(), 0);

                Address address = ad.getItem(i);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(address.getLatitude(), address.getLongitude()), 16));
            }
        });
        trabajando = false;
        taxiActivo = preferences.getBoolean("activo", false);
        if (preferences.getInt("esTaxi", 0) == 1){
            Menu menu = navigationView.getMenu();
            MenuItem item = menu.getItem(1);
            SubMenu subMenu = item.getSubMenu();
            itemTrabajar = subMenu.add("Trabajar como taxista").setIcon(R.drawable.contactanos);
            itemT = itemTrabajar.getItemId();
            esTaxi = true;
            idTaxista = preferences.getInt("idTaxista", 0);
        }else{
            esTaxi = false;
            itemT = 0;
        }

        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED){
            tienePermiso = true;
            if(esTaxi)
                getLocation();
        }else{
            tienePermiso = false;

            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

                // MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        fusedLocationProviderApi = LocationServices.FusedLocationApi;

        if (googleApiClient != null) {
            googleApiClient.connect();
        }


        // Mapa

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Location location = getLastKnownLocation();
        if (location != null) {
            // Getting latitude of the current location
            double latitude = location.getLatitude();

            // Getting longitude of the current location
            double longitude = location.getLongitude();

            myPosition = new LatLng(latitude, longitude);
        }else{
            Log.i("rosco", "Esta nula la locacion");
            myPosition = new LatLng(-21.533875205624717, -64.73419189453125);
        }

        mGeocoder = new Geocoder(this, Locale.getDefault());


        btnPedir = (ImageButton)findViewById(R.id.btnPedir);
        btnPedir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if (ultimaLocacion != null){
                ultimaLocacion = mMap.getCameraPosition().target;
                    if(!EnTarija(new LatLng(ultimaLocacion.latitude, ultimaLocacion.longitude))){
                        Toast.makeText(MainActivity.this, "Por ahora TaxiFast solo funciona en Tarija, Bolivia", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    double eps = 0.00001; // distancia en metros!
                    if (Math.abs(mMap.getCameraPosition().target.latitude - latPed) <= eps &&
                            Math.abs(mMap.getCameraPosition().target.longitude - longPed) <= eps){
                        PedidoRepetido = true;
                        txtDesde.setText(dirPed);
                    }else{
                        PedidoRepetido = false;
                        referencia = "";
                    }
                    Intent i = new Intent(MainActivity.this, PedirActivity.class);
                    i.putExtra("latOrigen", ultimaLocacion.latitude);
                    i.putExtra("longOrigen", ultimaLocacion.longitude);
                    i.putExtra("dirO", txtDesde.getText().toString());
                    i.putExtra("repetido", PedidoRepetido);
                    i.putExtra("referencia", referencia);
                    startActivityForResult(i, 2);
                //}else
                //    Toast.makeText(MainActivity.this, "Error en la conexion, inténtelo más tarte", Toast.LENGTH_SHORT).show();
            }
        });

        btnInformar = (Button)findViewById(R.id.btnInformar);
        btnInformar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(esTaxi && enCamino ){
                    if(btnInformar.getText().toString().equals("¡Ya llegué!")) {
                        // ya llego el taxi a su destino!
                        int distacia = calcularDistancia(myPosition, direccionOrigen);
                        if (distacia > DistanciaMinima) { // si es mas de 70 metros
                            Toast.makeText(MainActivity.this, "Está muy lejos del punto!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Informando al usuario", Toast.LENGTH_SHORT).show();
                            HashMap<String, String> mapa = new HashMap<String, String>();

                            mapa.put("id_carrera", "" + idPedido);
                            mapa.put("sess_id", sess_id);
                            mapa.put("sys_func", "yaLlegue");
                            try {
                                new HttpPost(getString(R.string.url) + "/notificaciones.php", HttpPost.getPostDataString(mapa), MainActivity.this).execute();
                            } catch (Exception e) {
                                Log.i("rosco", e.getMessage());
                            }
                        }
                    }else{
                        HashMap<String, String> mapa = new HashMap<String, String>();

                        mapa.put("id_carrera", "" + idPedido);
                        mapa.put("sess_id", sess_id);
                        mapa.put("sys_func", "yaLlegamos");
                        try {
                            new HttpPost(getString(R.string.url) + "/notificaciones.php", HttpPost.getPostDataString(mapa), MainActivity.this).execute();
                        } catch (Exception e) {
                            Log.i("rosco", e.getMessage());
                        }
                        estado = "HA";
                        enCamino = false;
                        btnInformar.setText("¡Ya llegué!");
                        btnInformar.setVisibility(View.GONE);
                    }
                }
            }
        });

        taxista = null;

        btnVerTaxista = (Button)findViewById(R.id.btnVerTaxista);
        btnVerTaxista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irAtaxista();
            }
        });
        btnCancelar = (Button)findViewById(R.id.btnCancelar);
        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NotificationManager notificationManager = (NotificationManager)MainActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancelAll();
                if (btnCancelar.getText().toString().equals("Ya llegamos")){
                    btnCancelar.setText("Cancelar");
                    int padding = btnCancelar.getPaddingBottom();
                    btnCancelar.setBackgroundResource(R.drawable.bg_boton);
                    btnCancelar.setPadding(padding, padding, padding, padding);
                    enCamino = false;
                    markerTaxi.remove();
                    markerUsu.remove();
                    ivUsu.setVisibility(View.VISIBLE);
                    actualizarBotones(false);
                    actualizarTaxi.cancel(true);
                    actualizarTaxi = null;
                    estado = "TE";
                    HashMap<String, String> datos = new HashMap<String, String>();
                    datos.put("id", id+"");
                    datos.put("sess_id", sess_id);
                    datos.put("id_carrera", idPedido+"");
                    datos.put("estado", estado);
                    PedidoRepetido = false;
                    referencia = "";
                    latPed = 0;
                    longPed = 0;
                    try{
                        new HttpPost(getString(R.string.url)+"/cancelar.php", HttpPost.getPostDataString(datos), MainActivity.this, 40000).execute();
                    }catch (UnsupportedEncodingException e) {
                        Log.i("rosco", e.getMessage());
                    }

                    return;
                }
                if (btnCancelar.getText().toString().equals("Ya abordé")){
                    btnCancelar.setText("Ya llegamos");

                    int padding = btnCancelar.getPaddingBottom();
                    btnCancelar.setBackgroundResource(R.drawable.bg_boton_2);
                    btnCancelar.setPadding(padding, padding, padding, padding);
                    markerUsu.remove();

                    //enTaxi = true;
                    //estado = "AB";

                    HashMap<String, String> datos = new HashMap<String, String>();
                    datos.put("id", id+"");
                    datos.put("sess_id", sess_id);
                    datos.put("id_carrera", idPedido+"");
                    datos.put("estado", "AB");
                    try{
                        new HttpPost(getString(R.string.url)+"/cancelar.php", HttpPost.getPostDataString(datos), MainActivity.this, 40000).execute();
                    }catch (UnsupportedEncodingException e) {
                        Log.i("rosco", e.getMessage());
                    }

                    return;
                }

                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Cancelar Pedido");
                alertDialog.setMessage("¿Seguro que quiere cancelar el pedido?\nSi cancela muchas veces se lo dejara de tomar en cuenta en Taxi Fast");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Si",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                HashMap<String, String> datos = new HashMap<String, String>();
                                datos.put("id", id+"");
                                datos.put("sess_id", sess_id);
                                datos.put("id_carrera", idPedido+"");
                                try{
                                    new HttpPost(getString(R.string.url)+"/cancelar.php", HttpPost.getPostDataString(datos), MainActivity.this, 40000).execute();
                                }catch (UnsupportedEncodingException e) {
                                    Log.i("rosco", e.getMessage());
                                }
                                estado = "";
                                enCamino = false;
                                markerTaxi.remove();
                                markerUsu.remove();
                                ivUsu.setVisibility(View.VISIBLE);
                                actualizarBotones(false);
                                actualizarTaxi.cancel(true);
                                actualizarTaxi = null;
                            }
                        });
                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                alertDialog.show();
            }
        });


        rlPedido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Aceptar el pedido!
                enCamino = true;
                if (longPoll != null) {
                    longPoll.cancel(true);
                    longPoll = null;
                }

                markerUsu = mMap.addMarker(new MarkerOptions()
                        .position(direccionOrigen)
                        .title("Pedido")
                        .snippet(dirOrigen)
                        .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("tipin", ivUsu.getWidth(), ivUsu.getHeight()))));

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(mMap.getCameraPosition().target);
                builder.include(markerUsu.getPosition());

                LatLngBounds bounds = builder.build();

                int padding = 120;
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                ivUsu.setVisibility(View.GONE);
                rlPedido.setVisibility(View.GONE);
                sounds.pause(soundID);
                sounds.stop(soundID);
                timerHandler.removeCallbacks(timerRunnable);
                mMap.animateCamera(cu);

                HashMap<String, String> mapa = new HashMap<String, String>();

                mapa.put("id_taxista", ""+idTaxista);
                mapa.put("id_pedido", ""+idPedido);

                mapa.put("latTaxi", ""+myPosition.latitude);
                mapa.put("longTaxi", ""+myPosition.longitude);
                btnPedir.setVisibility(View.GONE);

                int distancia = calcularDistancia(myPosition, direccionOrigen);
                if(distancia <= DistanciaMinima){
                    btnInformar.setVisibility(View.VISIBLE);
                }

                mapa.put("sess_id", sess_id);
                try{
                    new HttpPost(getString(R.string.url)+"/aceptarPedido.php", HttpPost.getPostDataString(mapa), MainActivity.this).execute();
                }catch (Exception e){
                    Log.i("rosco", "error al aceptar:"+e.getMessage());
                }
            }
        });

        NavigationView navview = (NavigationView)findViewById(R.id.nav_view);
        navview.getHeaderView(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (trabajando)
                    return;
                Intent i = new Intent(MainActivity.this, AcercaActivity.class);
                startActivity(i);
            }
        });


        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable( getApplicationContext() );
        if(status == ConnectionResult.SUCCESS) {
            //alarm to go and install Google Play Services
        }else if(status == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED){
            txtDesde.setVisibility(View.GONE);
            btnPedir.setVisibility(View.GONE);
            taxiActivo = false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the task you need to do.
                    tienePermiso = true;
                    if(esTaxi)
                        getLocation();
                } else {
                    Toast.makeText(MainActivity.this, "Debe permitir acceso a ubicación para continuar", Toast.LENGTH_SHORT);
                    tienePermiso = false;
                    // permission denied, boo! Disable the functionality that depends on this permission.
                }
                return;
            }
        }
    }

    public static int calculateDistance(double userLat, double userLng,
                                 double venueLat, double venueLng) {

        double latDistance = Math.toRadians(userLat - venueLat);
        double lngDistance = Math.toRadians(userLng - venueLng);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(venueLat))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = (AVERAGE_RADIUS_OF_EARTH * c) * 1000;

        return (int) (Math.round(distance));
    }

    public static  int calcularDistancia(LatLng dirUno, LatLng dirDos){
        return calculateDistance(dirUno.latitude, dirUno.longitude, dirDos.latitude, dirDos.longitude);
    }

    boolean EnTarija(LatLng locacion){

        if (Runner.dentro_del_poligono(locacion.latitude, locacion.longitude, coords_tarija))
            return true;
        return false;
    }

    void irAtaxista(){
        if (taxista == null){
            Log.i("rosco", "El taxista esta nulo! no deberia pasar esto");
            return;
        }
        Intent i = new Intent(MainActivity.this, TaxistaActivity.class);
        i.putExtra("taxista", taxista);
        startActivity(i);
    }
    void mostrarPedido(){
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle(dirOrigen);
        alertDialog.setMessage(refOrigen);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
    private Location getLastKnownLocation() {
        mLocationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }

        return bestLocation;
    }

    private void getLocation(){
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(1.0f);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("rosco","conection suspended");
    }

    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.i("rosco", "connection failed");
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                googleApiClient.connect();
            }
        } else {
            // Show dialog using GoogleApiAvailability.getErrorDialog()
            showErrorDialog(result.getErrorCode());
            mResolvingError = true;
        }
    }

    @Override
    public void onConnected(Bundle arg0) {
        if(tienePermiso && fusedLocationProviderApi != null && locationRequest != null)
            fusedLocationProviderApi.requestLocationUpdates(googleApiClient,  locationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        myPosition = new LatLng(location.getLatitude(), location.getLongitude());
        if(trabajando && cargoElMapa && markerTaxi != null){
            markerTaxi.setPosition(myPosition);
            if(enCamino){

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(markerTaxi.getPosition());
                builder.include(markerUsu.getPosition());

                LatLngBounds bounds = builder.build();

                int padding = 200;
                CameraUpdate cu;
                if (!estado.equals("AB"))
                    cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                else
                    cu = CameraUpdateFactory.newLatLng(myPosition);

                mMap.animateCamera(cu);

                HashMap<String, String> mapa = new HashMap<String, String>();

                mapa.put("id_taxista", ""+idTaxista);
                mapa.put("id_pedido", ""+idPedido);

                mapa.put("latTaxi", ""+myPosition.latitude);
                mapa.put("longTaxi", ""+myPosition.longitude);

                mapa.put("sess_id", sess_id);

                int distancia = calcularDistancia(myPosition, direccionOrigen);
                if(distancia <= DistanciaMinima){
                    btnInformar.setVisibility(View.VISIBLE);
                }

                try{
                    new HttpPost(getString(R.string.url)+"/aceptarPedido.php", HttpPost.getPostDataString(mapa), this).execute();
                    Log.i("rosco", "enviando nueva locacion");
                }catch (Exception e){
                    Log.i("rosco", "error al enviar locacion:"+e.getMessage());
                }
            }else
                mMap.animateCamera(CameraUpdateFactory.newLatLng(myPosition));
        }
        //Toast.makeText(App.getContext(), "location :"+location.getLatitude()+" , "+location.getLongitude(), Toast.LENGTH_SHORT).show();
    }


    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(
                    this.getActivity(), errorCode, REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((MainActivity) getActivity()).onDialogDismissed();
        }
    }

    private  LatLng miPosicion(){
        Location l = getLastKnownLocation();
        if(l == null) return null;
        return new LatLng(l.getLatitude(), l.getLongitude());
    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(enCamino){
                Toast.makeText(MainActivity.this, "Hay una carrera en curso, no puede salir", Toast.LENGTH_SHORT).show();
                return;
            }
            if(!quiereSalir){
                quiereSalir  = true;
                Toast.makeText(MainActivity.this, "Presione atrás de nuevo si quiere salir", Toast.LENGTH_SHORT).show();
            }else{
                logOut(false);
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        SharedPreferences preferences = getSharedPreferences(getString(R.string.preferencias), MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("estado", estado);
        editor.putString("sess_id", sess_id);
        editor.putInt("esTaxi", esTaxi ? 1 : 0);
        editor.putBoolean("trabajando", trabajando);
        editor.putInt("id_pedido", idPedido);
        editor.putInt("id", id);
        if (taxista != null){
            editor.putInt("id_taxista", taxista.id);
            editor.putString("nombre_taxista", taxista.nombre);
            editor.putString("ci_taxista", taxista.licencia);
            editor.putString("telc_taxista", taxista.telefono);
            editor.putFloat("latTaxi", (float)taxista.latitud);
            editor.putFloat("longTaxi", (float)taxista.longitud);

            editor.putFloat("latOrigen", (float)markerUsu.getPosition().latitude);
            editor.putFloat("longOrigen", (float)markerUsu.getPosition().longitude);
            editor.putString("btntxt", btnCancelar.getText().toString());
        }
        editor.apply();
        Log.i("rosco", "guardando estado:"+estado);
        super.onSaveInstanceState(savedInstanceState);
    }


    void logOut(boolean yaSalio){
        SharedPreferences preferences = getSharedPreferences(getString(R.string.preferencias), MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("login", false);
        editor.putBoolean("facebook", false);
        editor.putInt("esTaxi", 0);
        editor.apply();

        Profile perfil = Profile.getCurrentProfile();
        if (perfil != null){
            LoginManager.getInstance().logOut();
        }
        if(!yaSalio){
            //TODO: Hacer el logout de la pagina
        }
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int idi = item.getItemId();

        if(esTaxi && idi == itemT){
            if (enCamino)
                return true;
            if (trabajando){
                actualizarBotones(false);
                btnInformar.setVisibility(View.GONE);
                itemTrabajar.setTitle("Trabajar como Taxista");
                ivUsu.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.tipin));
                ivUsu.setVisibility(View.VISIBLE);
                if (markerTaxi != null)markerTaxi.remove();
                if (markerUsu != null)markerUsu.remove();
                mMap.getUiSettings().setAllGesturesEnabled(true);
                if(longPoll != null) {
                    longPoll.cancel(true);
                    longPoll = null;
                }
            }else{
                if(!taxiActivo){
                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                    alertDialog.setTitle("Licencia Expiró");
                    alertDialog.setMessage("Su licencia expiró, si quiere seguir trabajando debe comunicarse con Taxi Fast");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                    return true;
                }
                if(myPosition == null ||tienePermiso == false){
                    Toast.makeText(MainActivity.this, "Debe activar su locación para poder trabajar", Toast.LENGTH_LONG).show();
                    return true;
                }
                actualizarBotones(true);
                btnCancelar.setVisibility(View.GONE);
                btnVerTaxista.setVisibility(View.GONE);
                itemTrabajar.setTitle("Pedir Taxi");
                markerTaxi = mMap.addMarker(new MarkerOptions()
                        .position(myPosition)
                        .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("pin", ivUsu.getWidth(), ivUsu.getHeight())))
                );
                //ivUsu.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.pin));
                ivUsu.setVisibility(View.GONE);
                //mMap.getUiSettings().setAllGesturesEnabled(false);
                mMap.animateCamera(CameraUpdateFactory.newLatLng(myPosition));

                HashMap<String, String> datos = new HashMap<String, String>();
                datos.put("id", id+"");
                datos.put("id_taxista", ""+idTaxista);
                datos.put("sess_id", sess_id);
                datos.put("latTaxi", myPosition.latitude+"");
                datos.put("longTaxi", myPosition.longitude+"");

                try{
                    longPoll = new HttpPost(getString(R.string.url)+"/pedidos.php", HttpPost.getPostDataString(datos), MainActivity.this, 40000);
                    longPoll.execute();
                }catch (Exception e){
                    Log.i("rosco", "error en el pedido:"+e.getMessage());
                }
            }

            trabajando = !trabajando;
        }else if(idi == R.id.usuario){
            if (trabajando)
                return false;
            Intent i = new Intent(MainActivity.this, RegisterActivity.class);
            startActivityForResult(i, 1);
        }else if (idi == R.id.nav_contacto) {
            if (trabajando)
                return true;
            Intent i = new Intent(MainActivity.this, ContactoActivity.class);
            startActivity(i);
        } else if (idi == R.id.nav_historial) {
            if (trabajando)
                return false;
            Intent i = new Intent(MainActivity.this, HistorialActivity.class);
            i.putExtra("id", id);
            i.putExtra("sess_id", sess_id);
            startActivityForResult(i, 10);
        } else if (idi == R.id.nav_share) {
            if (trabajando)
                return false;
            String message = "Tuve un buen viaje con TaxiFast. www.roscosoft.com/taxifast";
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT, message);

            startActivity(Intent.createChooser(share, "Compartir TaxiFast"));
        } else if (idi == R.id.nav_salir) {
            if(enCamino){
                Toast.makeText(MainActivity.this, "Hay una carrera en curso, no puede salir", Toast.LENGTH_SHORT).show();
                return true;
            }
            logOut(false);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        appVisible = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        appVisible = true;
        if (cargoElMapa && !trabajando && !enCamino) {
            if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && tienePermiso)
                mMap.setMyLocationEnabled(true);
            else
                mMap.setMyLocationEnabled(false);
        }
    }
    void recuperarEstado(){
        if(estado == null || estado.equals("")){
            SharedPreferences preferences = getSharedPreferences(getString(R.string.preferencias), MODE_PRIVATE);
            enCamino = preferences.getBoolean("enCamino", false);
            id = preferences.getInt("id", 0);
            //trabajando = preferences.getBoolean("trabajando", false);
            sess_id = preferences.getString("sess_id", "");
            idPedido = preferences.getInt("id_pedido", idPedido);
            estado = preferences.getString("estado", "HA");
            if (estado.equals("ES") || estado.equals("TA")){
                Log.i("rosco", "Esperando un taxi!");
                if (actualizarTaxi == null){
                    actualizarBotones(true);
                    Log.i("rosco", "volviendo a actualizar al taxista");
                    double latOrigen = preferences.getFloat("latOrigen", (float)myPosition.latitude);
                    double longOrigen = preferences.getFloat("longOrigen", (float)myPosition.longitude);
                    if (taxista == null){
                        taxista = new Taxista();
                        taxista.id = preferences.getInt("id_taxista", 0);
                        taxista.nombre = preferences.getString("nombre_taxista", "Taxista");
                        taxista.licencia = preferences.getString("ci_taxista", "0000");
                        taxista.telefono = preferences.getString("telc_taxista", "0000");
                        taxista.latitud = preferences.getFloat("latTaxi", (float)plaza.latitude);
                        taxista.longitud = preferences.getFloat("longTaxi", (float)plaza.longitude);
                        btnCancelar.setText(preferences.getString("btntxt", "Cancelar"));
                    }
                    int ancho = ivUsu.getWidth();
                    if (ancho <= 0)
                        ancho = 80;

                    markerTaxi = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(taxista.latitud, taxista.longitud))
                            .title("Taxista")
                            .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("pin", ancho, ancho)))
                            .snippet(taxista.nombre));


                    markerUsu = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latOrigen, longOrigen))
                            .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("tipin", ancho, ancho))));

                    ivUsu.setVisibility(View.GONE);

                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(markerTaxi.getPosition());
                    builder.include(markerUsu.getPosition());

                    LatLngBounds bounds = builder.build();

                    int padding = 200;
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

                    if (mMap == null)
                    {
                        Log.i("rosco", "el mapa estaba nulo");
                        return;
                    }
                    mMap.animateCamera(cu);

                    HashMap<String, String> datos = new HashMap<String, String>();
                    datos.put("id", id+"");
                    datos.put("sess_id", sess_id);
                    datos.put("id_pedido", ""+idPedido);
                    datos.put("taxista", "0");
                    datos.put("latTaxi", "0.0");
                    datos.put("longTaxista", "0.0");
                    datos.put("estado", estado);
                    try{
                        actualizarTaxi = new HttpPost(getString(R.string.url)+"/pedirTaxi.php", HttpPost.getPostDataString(datos), MainActivity.this, 40000);
                        actualizarTaxi.execute();
                    }catch (UnsupportedEncodingException e){
                        Log.i("rosco", "Error de encoding:"+e.getMessage());
                    }
                }else if (actualizarTaxi.getStatus() == AsyncTask.Status.FINISHED){
                    actualizarTaxi.execute();
                    Log.i("rosco", "volviendo a actualizar al taxista");
                }
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle extras = intent.getExtras();
        if (extras != null && extras.containsKey("accion")){
            int accion = extras.getInt("accion");
            switch (accion){
                case 1: // taxista recibido
                    if (enCamino)// ya tenia taxi
                        return;
                    taxista = (Taxista) intent.getSerializableExtra("taxista");

                    int ancho = ivUsu.getWidth();
                    if (ancho <= 0)
                        ancho = 80;

                    markerTaxi = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(taxista.latitud, taxista.longitud))
                            .title("Taxista")
                            .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("pin", ancho, ancho)))
                            .snippet(taxista.nombre));


                    markerUsu = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(intent.getDoubleExtra("latOrigen", 0.0), intent.getDoubleExtra("longOrigen", 0.0)))
                            .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("tipin", ancho, ancho))));

                    ivUsu.setVisibility(View.GONE);

                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(markerTaxi.getPosition());
                    builder.include(markerUsu.getPosition());

                    LatLngBounds bounds = builder.build();

                    int padding = 200;
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

                    if (mMap == null)
                    {
                        Log.i("rosco", "el mapa estaba nulo");
                        return;
                    }

                    mMap.animateCamera(cu);

                    enCamino = true;
                    actualizarBotones(true);

                    HashMap<String, String> datos = new HashMap<String, String>();
                    datos.put("id", id+"");
                    datos.put("sess_id", sess_id);
                    datos.put("id_pedido", ""+intent.getIntExtra("id_pedido", 0));
                    idPedido = intent.getIntExtra("id_pedido", 0);
                    datos.put("taxista", "ya hay taxista a ver");
                    datos.put("latTaxi", ""+taxista.latitud);
                    datos.put("longTaxista", ""+taxista.longitud);
                    datos.put("estado", estado);
                    try{
                        actualizarTaxi = new HttpPost(getString(R.string.url)+"/pedirTaxi.php", HttpPost.getPostDataString(datos), MainActivity.this, 40000);
                        actualizarTaxi.execute();
                    }catch (UnsupportedEncodingException e){
                        Log.i("rosco", "Error de encoding:"+e.getMessage());
                    }
                    break;
                case 2: // taxi afuera
                    if(estado.equals("AB")) // ya abordo
                        return;
                    actualizarBotones(true);
                    btnCancelar.setText("Ya abordé");
                    if (taxista == null){
                        // hay que actualizar tooodo

                    }

                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 3){
            // session expirada
            logOut(true);
            finish();
        }

        quiereSalir = false;
        if (requestCode == 1){ // modificar usuario
            SharedPreferences preferences = getSharedPreferences(getString(R.string.preferencias), MODE_PRIVATE);

            titNombre.setTitle(preferences.getString("nombre", "opa") + " " + preferences.getString("apellidos", "oporoncio"));
        }else if(requestCode == 2){ // hacer pedido
            if (resultCode == 1){
                taxista = (Taxista) data.getSerializableExtra("taxista");

                markerTaxi = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(taxista.latitud, taxista.longitud))
                                    .title("Taxista")
                                    .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("pin", ivUsu.getWidth(), ivUsu.getHeight())))
                                    .snippet(taxista.nombre));


                markerUsu = mMap.addMarker(new MarkerOptions()
                        .position(mMap.getCameraPosition().target)
                        .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("tipin", ivUsu.getWidth(), ivUsu.getHeight()))));

                ivUsu.setVisibility(View.GONE);

                estado = "ES";

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(markerTaxi.getPosition());
                builder.include(markerUsu.getPosition());

                LatLngBounds bounds = builder.build();

                int padding = 100;
                Update cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

                mMap.animateCamera(cu);

                enCamino = true;
                actualizarBotones(true);

                HashMap<String, String> datos = new HashMap<String, String>();
                datos.put("id", id+"");
                datos.put("sess_id", sess_id);
                datos.put("id_pedido", ""+data.getIntExtra("id_pedido", 0));
                idPedido = data.getIntExtra("id_pedido", 0);
                datos.put("taxista", "ya hay taxista a ver");
                datos.put("latTaxi", ""+taxista.latitud);
                datos.put("longTaxista", ""+taxista.longitud);
                datos.put("estado", estado);
                try{
                    actualizarTaxi = new HttpPost(getString(R.string.url)+"/pedirTaxi.php", HttpPost.getPostDataString(datos), MainActivity.this, 40000);
                    actualizarTaxi.execute();
                }catch (UnsupportedEncodingException e){
                    Log.i("rosco", "Error de encoding:"+e.getMessage());
                }

            }else
                Log.i("rosco", "se fue atras");
        }else if(requestCode == 10){ // historial
            if(resultCode == 1){ // eligio un pedido
                if (trabajando)
                    return;
                Pedido pedido = (Pedido) data.getSerializableExtra("pedido");
                txtDesde.setText(pedido.direccion);
                referencia = pedido.referencia;
                PedidoRepetido = true;
                latPed = pedido.latitud;
                longPed = pedido.longitud;
                dirPed = pedido.direccion;
                mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(pedido.latitud, pedido.longitud)));
            }
        }
    }


    public Bitmap resizeMapIcons(String iconName,int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    public void actualizarBotones(boolean cancelar){
        if (cancelar){
            mMap.setMyLocationEnabled(false);
            txtDesde.setVisibility(View.GONE);
            btnCancelar.setVisibility(View.VISIBLE);
            btnVerTaxista.setVisibility(View.VISIBLE);
            btnPedir.setVisibility(View.GONE);
        }else{
            mMap.setMyLocationEnabled(true);
            txtDesde.setVisibility(View.VISIBLE);
            btnCancelar.setVisibility(View.GONE);
            btnVerTaxista.setVisibility(View.GONE);
            btnPedir.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        cargoElMapa = true;
        boolean hayGPS = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if( !hayGPS ) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Debe habilitar el GPS");  // GPS not found
            builder.setMessage("Para que funcione mejor TaxiFast debe habilitar el GPS. ¿Quiere Habilitarlo?"); // Want to enable?
            builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    MainActivity.this.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    tienePermiso = false;
                }
            });
            builder.create().show();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && hayGPS) {
            mMap.setMyLocationEnabled(true);
            tienePermiso = true;
        } else {
            Toast.makeText(MainActivity.this, "Debe activar la locacion!", Toast.LENGTH_SHORT).show();
            tienePermiso = false;
        }

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                ultimaLocacion = cameraPosition.target;
                if (enCamino || trabajando)
                    return;
                if (reqs.size() < 5){
                    NomDir nomDir = new NomDir(cameraPosition.target);
                    reqs.add(nomDir);
                    nomDir.execute();
                    editando = true;

                }
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if(marker.getId().equals(markerTaxi.getId()))
                    irAtaxista();
                else if(marker.getId().equals(markerUsu.getId()))
                    mostrarPedido();
            }
        });

        // Add a marker in sanja and move the camera
        //mMap.addMarker(new MarkerOptions().position(plaza).title("Marker en la plaza"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 16));

        ivUsu.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                onNewIntent(getIntent());
            }
        });

        recuperarEstado();
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
        public List<Address> getStringFromLocation(double lat, double lng)
                throws IOException, JSONException {

            String address = String
                    .format(Locale.ENGLISH, "http://maps.googleapis.com/maps/api/geocode/json?latlng=%1$f,%2$f&sensor=true&language="
                            + Locale.getDefault().getCountry(), lat, lng);
            StringBuilder stringBuilder = new StringBuilder();
            URL urlc = null;
            HttpURLConnection con = null;
            try{
                urlc = new URL(address);
            }catch (MalformedURLException e){
                Log.i("rosco", e.getMessage());
            }
            try {
                con = (HttpURLConnection)urlc.openConnection();
                con.setRequestMethod("GET");
                con.setDoInput(true);
                con.setDoOutput(true);

                /*
                OutputStream os = con.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(postDataString);

                writer.flush();
                writer.close();
                os.close();
                */
                int responseCode = con.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK){
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    while ((line = br.readLine()) != null){
                        stringBuilder.append(line);
                    }
                }else{
                    Log.i("rosco", "Error:"+responseCode);
                    return null;
                }
            } catch (Exception e) {
                Log.i("rosco", "Hubo un error al hacer el request:"+e.getMessage());
                return null;
            }finally {
                con.disconnect();
            }
            //Log.i("rosco", stringBuilder.toString());
            JSONObject jsonObject = new JSONObject(stringBuilder.toString());

            ArrayList<Address> retList = new ArrayList<Address>();

            if ("OK".equalsIgnoreCase(jsonObject.getString("status"))) {
                JSONArray results = jsonObject.getJSONArray("results");
                for (int i = 0; i < results.length(); i++) {
                    JSONObject result = results.getJSONObject(i);
                    Address addr = new Address(Locale.getDefault());

                    JSONArray components = result.getJSONArray("address_components");
                    String streetNumber = "";
                    String route = "";
                    for (int a = 0; a < components.length(); a++) {
                        JSONObject component = components.getJSONObject(a);
                        JSONArray types = component.getJSONArray("types");
                        for (int j = 0; j < types.length(); j++) {
                            String type = types.getString(j);
                            if (type.equals("locality")) {
                                addr.setLocality(component.getString("long_name"));
                            }else if(type.equals("administrative_area_level_2")) {
                                addr.setAdminArea(component.getString("long_name"));
                            }else if (type.equals("street_number")) {
                                streetNumber = component.getString("long_name");
                                addr.setSubThoroughfare(streetNumber);
                            } else if (type.equals("route")) {
                                route = component.getString("long_name");
                                addr.setThoroughfare(route);
                            }
                        }
                    }
                    addr.setAddressLine(0, route + " " + streetNumber);

                    addr.setLatitude(result.getJSONObject("geometry").getJSONObject("location").getDouble("lat"));
                    addr.setLongitude(result.getJSONObject("geometry").getJSONObject("location").getDouble("lng"));
                    retList.add(addr);
                }
            }

            return retList;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                if (reverse) {
                    List<Address> lista = mGeocoder.getFromLocation(location.latitude, location.longitude, 1);
                    if (lista != null && lista.size() > 0) {
                        Address address = lista.get(0);
                        String ciudad = address.getAdminArea();
                        String pais = address.getCountryName();
                        String numero = address.getSubThoroughfare();
                        String calle = address.getThoroughfare();
                        if(ciudad == null)
                            ciudad = address.getSubAdminArea();
                        if(ciudad == null)
                            ciudad = "Tarija"; // Cambiar esto para otras ciudades
                        if (calle == null)
                            calle = "Desconocida";

                        if (calle.startsWith("Avenida") || calle.startsWith("Puente"))
                            result = "";
                        else
                            result = "Calle ";

                        if (numero != null)
                            result += calle + " #" + address.getSubThoroughfare() + ", " + ciudad; // de gana poner el pais
                        else
                            result += calle + ", " + ciudad;
                        //ultimaLocacion = address;
                        //ultimaLocacion.setLatitude(location.latitude);
                        //ultimaLocacion.setLongitude(location.longitude);
                    }else{
                        // Vamos a usar otra tecnologia
                        try {
                            lista = getStringFromLocation(location.latitude, location.longitude);
                            if (lista != null && lista.size() > 0) {
                                Address address = lista.get(0);
                                String ciudad = address.getAdminArea();
                                String pais = address.getCountryName();
                                String numero = address.getSubThoroughfare();
                                String calle = address.getThoroughfare();
                                if(ciudad == null)
                                    ciudad = address.getSubAdminArea();
                                if(ciudad == null)
                                    ciudad = "Tarija"; // Cambiar esto para otras ciudades
                                if (calle == null)
                                    calle = "Desconocida";

                                if (calle.startsWith("Avenida") || calle.startsWith("Puente"))
                                    result = "";
                                else
                                    result = "Calle ";

                                if (numero != null)
                                    result += calle + " #" + address.getSubThoroughfare() + ", " + ciudad; // de gana poner el pais
                                else
                                    result += calle + ", " + ciudad;
                            }else
                                Log.i("rosco", "No se puede cargar el nombre de la ubicación");
                        }catch (JSONException e){
                            Log.i("rosco", "Error al buscar locacion:"+e.getMessage());
                        }
                    }
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
                if (result != null && result.length() > 0){
                    txtDesde.setText(result);
                }
                reqs.remove(this);
                if (reqs.size() == 0) editando = false;
            }else{
                if (addresses.size() > 0){
                    AddressAdapter ad = new AddressAdapter(addresses, MainActivity.this);
                    txtDesde.setAdapter(ad);
                    txtDesde.showDropDown();
                }else{
                    //Log.i("rosco", "no hay direcciones!");
                }
                busq.remove(this);
            }
        }
    }

    void mostrarNuevoPedido(){
        if(pedidos.size() == 0){
                buscarPedidos();
            return;
        }
        Pedido pedido = pedidos.get(mostrandoPedido);

        rlPedido.setVisibility(View.VISIBLE);
        tvDireccion.setText("Pedido en:\n"+pedido.direccion);
        longPoll = null;

        ObjectAnimator animation = ObjectAnimator.ofInt (progressBar, "progress", 0, 500); // see this max value coming back here, we animale towards that value
        animation.setDuration (tiempoEspera*1000); //in milliseconds
        animation.setInterpolator (new DecelerateInterpolator());
        tiempoPasado = 0;
        timerHandler.post(timerRunnable);
        animation.start ();

        direccionOrigen = new LatLng(pedido.latitud, pedido.longitud);
        dirOrigen = pedido.direccion;
        refOrigen = pedido.referencia;
        idPedido = pedido.id;
    }
    void buscarPedidos(){
        Log.i("rosco", "buscando pedidos...");
        HashMap<String, String> datos = new HashMap<String, String>();
        datos.put("id", id+"");
        datos.put("sess_id", sess_id);
        datos.put("id_taxista", ""+idTaxista);
        datos.put("latTaxi", myPosition.latitude+"");
        datos.put("longTaxi", myPosition.longitude+"");
        datos.put("pedido_viejo", idPedido+"");

        try{
            longPoll = new HttpPost(getString(R.string.url)+"/pedidos.php", HttpPost.getPostDataString(datos), MainActivity.this, 40000);
            longPoll.execute();
        }catch (Exception e){
            Log.i("rosco", "Error al hacer request:"+e.getMessage());
        }
    }

    @Override
    public void termino(JSONObject obj) {
        // es el taxista esperando pedidos
        // success 1: Hay pedidos nuevos
        // success 2: No hay pedidos, seguir esperando
        // success 3: SE inicio session en otro lugar
        // success 4: El usuario cancelo el pedido
        // success 0: Hubo algun error
        try{
            if (obj.has("mensaje"))
                Log.i("rosco", obj.getString("mensaje"));

            if (obj.getInt("success") == 4){
                Toast.makeText(MainActivity.this, obj.getString("mensaje"), Toast.LENGTH_LONG).show();
                enCamino = false;
                estado = "HA";
                markerUsu.remove();
                btnInformar.setText("¡Ya llegué!");
                btnInformar.setVisibility(View.GONE);
                HashMap<String, String> datos = new HashMap<String, String>();
                datos.put("id", id+"");
                datos.put("id_taxista", ""+idTaxista);
                datos.put("sess_id", sess_id);
                datos.put("latTaxi", myPosition.latitude+"");
                datos.put("longTaxi", myPosition.longitude+"");

                try{
                    longPoll = new HttpPost(getString(R.string.url)+"/pedidos.php", HttpPost.getPostDataString(datos), MainActivity.this, 40000);
                    longPoll.execute();
                }catch (Exception e){
                    Log.i("rosco", "Error al hacer request:"+e.getMessage());
                }
            }
            if(obj.getInt("success") == 3){
                Toast.makeText(MainActivity.this, "Hizo login desde otro dispositivo", Toast.LENGTH_SHORT).show();
                logOut(true);
                finish();
            }
        }catch (JSONException e){
            Log.i("rosco", "Error de JSON:"+e.getMessage());
        }

        if (trabajando){
            try{
                // Estas son las opciones del taxista
                if(obj.getInt("success") == 1){

                    // request 1 = informar al usuario de llegada
                    if (obj.has("request") && obj.getInt("request") == 1){
                        Log.i("rosco", obj.getString("mensaje"));
                        return;
                    }
                    // request 2 = El usuario esta a bordo
                    if(obj.has("request") && obj.getInt("request") == 2){
                        estado = obj.getString("estado");
                        if(estado.equals("AB")) {
                            btnInformar.setText("Ya llegamos");
                            markerUsu.remove();
                        }
                        return;
                    }
                    if (enCamino)
                        return; // trabajando no debe buscar pedidos

                    mostrandoPedido = 0;
                    pedidos.clear();
                    JSONArray array = obj.getJSONArray("pedidos");
                    for (int i = 0; i < array.length(); i++){
                        JSONObject obj1 = array.getJSONObject(i);
                        //Log.i("rosco", "tiempo: "+obj1.getInt("tiempo"));
                        Pedido pedido = new Pedido();
                        pedido.direccion = obj1.getString("direccionOrigen");
                        pedido.latitud = obj1.getDouble("latOrigen");
                        pedido.longitud = obj1.getDouble("longOrigen");
                        pedido.referencia = obj1.getString("referencia");
                        pedido.id = obj1.getInt("idPedido");
                        if(idPedido == pedido.id){
                            continue;
                        }
                        pedidos.add(pedido);
                    }
                    if (pedidos.size() > 0 && !appVisible) {
                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 2 /* Request code */, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                        Uri defaultSoundUri = null;
                        defaultSoundUri = Uri.parse("android.resource://com.vitalsoftware.taxifast/" + R.raw.taxifast);

                        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.notificon2)
                                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                                .setContentTitle("Nuevos Pedidos")
                                .setContentText("Hay nuevos pedidos cerca de usted!")
                                .setAutoCancel(true)
                                .setSound(defaultSoundUri)
                                .setPriority(2)
                                .setContentIntent(pendingIntent);

                        Notification notification = notificationBuilder.build();

                        NotificationManager notificationManager =
                                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                        notificationManager.notify(0 /* ID of notification */, notification);
                    }
                    mostrarNuevoPedido();

                }else if(obj.getInt("success") == 2){
                    if (enCamino)
                        return; // trabajando, no buscar mas pedidos
                    // no hay pedidos
                    buscarPedidos();
                    Log.i("rosco", "No hay pedidos, preguntandpo de nuevo...");
                }else if (obj.getInt("success") == 0){
                    Log.i("rosco", "error:"+obj.getString("mensaje"));
                }
            }catch (JSONException e){
                Log.i("rosco", "error de json:"+e.getMessage()+":"+obj.toString());
            }
        }else{
            // es el usuario esperando actualizar la posicion del taxista
           try{
               if(obj.getInt("success") == 1){
                   if(estado.equals("TE")) // carrera terminada
                       return;
                   if (obj.has("request") && obj.getInt("request") == 2){
                       // carrera actualizada
                       estado = "AB";
                       return;
                   }
                   if (obj.has("request") && obj.getInt("request") == 1){
                       // carrera terminada
                       return;
                   }
                   markerTaxi.setPosition(new LatLng(obj.getDouble("latTaxi"), obj.getDouble("longTaxi")));
                   taxista.latitud = obj.getDouble("latTaxi");
                   taxista.longitud = obj.getDouble("longTaxi");

                   if(estado.equals("AB"))
                       mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(taxista.latitud, taxista.longitud), mMap.getCameraPosition().zoom));

                   HashMap<String, String> datos = new HashMap<String, String>();
                   datos.put("id", id+"");
                   datos.put("id_pedido", ""+idPedido);

                   if(obj.has("id_taxista") && obj.getInt("id_taxista") != taxista.id){
                       Toast.makeText(MainActivity.this, "Un taxista mas cercano esta en camino", Toast.LENGTH_SHORT).show();
                       taxista = new Taxista(obj.getInt("id_taxista"), obj.getString("nombre"),
                               obj.getString("licencia"), obj.getString("foto"), obj.getDouble("latTaxi"), obj.getDouble("longTaxi"), obj.getString("telefono"));
                   }

                   datos.put("taxista", ""+taxista.id);
                   datos.put("latTaxi", ""+taxista.latitud);
                   datos.put("longTaxi", ""+taxista.longitud);
                   datos.put("sess_id", sess_id);

                   if (obj.has("estado") && obj.getString("estado").equals("TA")){ // Taxi esta afuera
                       if(estado.equals("AB") || btnCancelar.getText().toString().equals("Ya llegamos"))
                       {
                           // no hacer nada
                       }else {
                           btnCancelar.setText("Ya abordé");
                           estado = "TA";
                       }
                   }
                   datos.put("estado", estado);
                   Log.i("rosco", "actualizando el taxi, estado = "+estado);
                   try{
                       actualizarTaxi = new HttpPost(getString(R.string.url)+"/pedirTaxi.php", HttpPost.getPostDataString(datos), MainActivity.this, 40000);
                       actualizarTaxi.execute();
                   }catch (UnsupportedEncodingException e){
                       Log.i("rosco", "Error de encoding:"+e.getMessage());
                   }
               }else if(obj.getInt("success") == 0){
                   Log.i("rosco", "Hubo un error: "+obj.getString("mensaje"));
               }
           }catch (JSONException e){
               Log.i("rosco", "json exception:"+e.getMessage());
           }
        }
    }

    @Override
    public void cancelo(String error) {
        if (trabajando && !enCamino){
            Handler handler = new Handler();
            handler.postDelayed(new Runnable(){
                @Override
                public void run(){
                    if (longPoll == null || longPoll.getStatus() == AsyncTask.Status.FINISHED){
                        HashMap<String, String> datos = new HashMap<String, String>();
                        datos.put("id", id+"");
                        datos.put("id_taxista", ""+idTaxista);
                        datos.put("sess_id", sess_id);
                        datos.put("latTaxi", myPosition.latitude+"");
                        datos.put("longTaxi", myPosition.longitude+"");

                        try{
                            longPoll = new HttpPost(getString(R.string.url)+"/pedidos.php", HttpPost.getPostDataString(datos), MainActivity.this, 40000);
                            longPoll.execute();
                        }catch (Exception e){
                            Log.i("rosco", "error en el pedido:"+e.getMessage());
                        }
                    }
                }
            }, 5000);
        }else if(!trabajando && enCamino){
            Handler handler = new Handler();
            handler.postDelayed(new Runnable(){
                @Override
                public void run(){
                    if (actualizarTaxi == null || actualizarTaxi.getStatus() == AsyncTask.Status.FINISHED){
                        HashMap<String, String> datos = new HashMap<String, String>();
                        datos.put("id", id+"");
                        datos.put("sess_id", sess_id);
                        datos.put("id_pedido", ""+idPedido);
                        datos.put("taxista", "0");
                        datos.put("latTaxi", "0.0");
                        datos.put("longTaxista", "0.0");
                        datos.put("estado", estado);
                        try{
                            actualizarTaxi = new HttpPost(getString(R.string.url)+"/pedirTaxi.php", HttpPost.getPostDataString(datos), MainActivity.this, 40000);
                            actualizarTaxi.execute();
                        }catch (UnsupportedEncodingException e){
                            Log.i("rosco", "Error de encoding:"+e.getMessage());
                        }
                    }
                }
            }, 5000);
        }
        Log.i("rosco", "Cancelo: "+error);
    }
}
