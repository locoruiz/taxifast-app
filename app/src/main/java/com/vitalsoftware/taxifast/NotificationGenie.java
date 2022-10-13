package com.vitalsoftware.taxifast;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.util.Map;

/**
 * Created by titin on 9/23/16.
 */
public class NotificationGenie extends FirebaseMessagingService {

    private static final String TAG = "rosco";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.

        Map<String, String> mapa = remoteMessage.getData();

        sendNotification(mapa);
    }

    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.ic_stat_notificon2 : R.drawable.ic_launcher;
    }

    private void sendNotification(Map<String, String> mapa) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        boolean taxiAfuera = false;
        if (mapa != null && mapa.containsKey("accion")){
            int accion = Integer.parseInt(mapa.get("accion"));
            intent.putExtra("accion", accion);
            if (accion == 1){
                // notificacion de que ya tiene un taxi

                Taxista taxista = new Taxista(Integer.parseInt(mapa.get("id_taxista")),
                        mapa.get("nombre"),
                        mapa.get("licencia"), mapa.get("foto"), Double.parseDouble(mapa.get("latTaxi")),
                        Double.parseDouble(mapa.get("longTaxi")),
                        mapa.get("telefono"));

                intent.putExtra("taxista", taxista);
                intent.putExtra("id_pedido", Integer.parseInt(mapa.get("id_pedido")));
                intent.putExtra("latOrigen", Double.parseDouble(mapa.get("latPedido")));
                intent.putExtra("longOrigen", Double.parseDouble( mapa.get("longPedido")));

                // guardar todo en shared preferences



            }else if(accion == 2){
                // Taxi Afuera!
                /*
                Taxista taxista = new Taxista(Integer.parseInt(mapa.get("id_taxista")),
                        mapa.get("nombre"),
                        mapa.get("licencia"), mapa.get("foto"), Double.parseDouble(mapa.get("latTaxi")),
                        Double.parseDouble(mapa.get("longTaxi")),
                        mapa.get("telefono"));

                intent.putExtra("taxista", taxista);
                intent.putExtra("id_pedido", Integer.parseInt(mapa.get("id_pedido")));
                intent.putExtra("latOrigen", Double.parseDouble(mapa.get("latPedido")));
                intent.putExtra("longOrigen", Double.parseDouble( mapa.get("longPedido")));
                */
                taxiAfuera = true;
            }
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 2 /* Request code */, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri = null;
        if (taxiAfuera){
            defaultSoundUri = Uri.parse("android.resource://com.vitalsoftware.taxifast/" + R.raw.sonido);
        }else{
            defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notificon2)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                .setContentTitle(mapa.get("title"))
                .setContentText(mapa.get("body"))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setPriority(2)
                .setContentIntent(pendingIntent);

        Notification notification = notificationBuilder.build();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notification);
    }
}