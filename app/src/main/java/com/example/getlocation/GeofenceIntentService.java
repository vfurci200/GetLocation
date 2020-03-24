package com.example.getlocation;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

public class GeofenceIntentService extends IntentService {
    private static Context context;
    private static GeofenceIntentService instance;
    public GeofenceIntentService() {
        super("GeofenceIntentService");
    }
    @Override
    protected void onHandleIntent(Intent intent) {

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Toast.makeText(getApplicationContext(), "Geofence error code= "
                    + geofencingEvent.getErrorCode(), Toast.LENGTH_SHORT).show();
            Log.i("info", "Geofence Error.");
            return;
        }


        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            // Get the geofence that were triggered
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            Toast.makeText(getApplicationContext(), "GEOFENCE_TRANSITION_DWELL",
                    Toast.LENGTH_SHORT).show();
            Log.i("info", "Geofence Entered.");

        }
    }

        public String getGeofenceTransitionDetails(int geoFenceTransition, List<Geofence> triggeringGeofences)
        {

            // get the ID of each geofence triggered
            ArrayList<String> triggeringGeofencesList = new ArrayList<>();
            for (Geofence geofence : triggeringGeofences) {
                triggeringGeofencesList.add(geofence.getRequestId());
            }
            String status = "Dwelling" ;
            return status + TextUtils.join( ", ", triggeringGeofencesList);
        }

       public static GeofenceIntentService getInstance() {
        return instance;
    }


    }




