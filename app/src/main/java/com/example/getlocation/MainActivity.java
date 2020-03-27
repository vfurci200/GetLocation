package com.example.getlocation;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.ACCESS_BACKGROUND_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;


public class MainActivity extends AppCompatActivity {

    private double lat;
    private double lon;
    List<Double> CurrentPos = new ArrayList<Double>();
    List<Geofence> geofenceList = new ArrayList<>();
    private final int REQUEST_RESOLVE_GOOGLE_CLIENT_ERROR=1;
    private GeofencingClient geofencingClient;
    boolean mResolvingError;

    GoogleApiClient mGoogleApiClient;

    GoogleApiClient.ConnectionCallbacks mConnectionCallbacks =
            new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(Bundle bundle) {
                    Toast.makeText(MainActivity.this, "onConnected()", Toast.LENGTH_LONG).show();
                    getLocation();
                }
                @Override
                public void onConnectionSuspended(int i) {}
            };

    GoogleApiClient.OnConnectionFailedListener mOnConnectionFailedListener =
            new GoogleApiClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(ConnectionResult connectionResult) {
                    Toast.makeText(MainActivity.this, connectionResult.toString(), Toast.LENGTH_LONG).show();
                    if (mResolvingError) {
                        return;
                    } else if (connectionResult.hasResolution()) {
                        mResolvingError = true;
                        try {
                            connectionResult.startResolutionForResult(MainActivity.this,
                                    REQUEST_RESOLVE_GOOGLE_CLIENT_ERROR);
                        } catch (IntentSender.SendIntentException e) {
                            mGoogleApiClient.connect();
                        }
                    } else {
                        showGoogleAPIErrorDialog(connectionResult.getErrorCode());
                    }
                }
            };

    private void showGoogleAPIErrorDialog(int errorCode) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        Dialog errorDialog = googleApiAvailability.getErrorDialog(
                this, errorCode, REQUEST_RESOLVE_GOOGLE_CLIENT_ERROR);
        errorDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_RESOLVE_GOOGLE_CLIENT_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK
                    && !mGoogleApiClient.isConnecting()
                    && !mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
            }
        }
    }

    protected void setupGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(mConnectionCallbacks)
                .addOnConnectionFailedListener(mOnConnectionFailedListener)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            setupGoogleApiClient();
        } else {
            ActivityCompat.requestPermissions(this, new String[] {ACCESS_FINE_LOCATION},1);
        }
        if (ActivityCompat.checkSelfPermission(this, ACCESS_BACKGROUND_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            GetAddGeofences();
        } else {
            ActivityCompat.requestPermissions(this, new String[] {ACCESS_BACKGROUND_LOCATION},1);
        }


    } // onCreate end

    private void GetAddGeofences(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            geofencingClient = LocationServices.getGeofencingClient(this);
            geofencingClient.addGeofences(createGeofencingRequest(), createGeofencePendingIntent())
                    .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(MainActivity.this, "onSuccess()", Toast.LENGTH_SHORT).show();
                            final TextView textView2 = findViewById(R.id.textView2);
                            textView2.setText("HI");
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this,
                                    "onFailure(): " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION},1);
        }}


    private List getLocation() throws SecurityException {

        LocationServices.getFusedLocationProviderClient(this).getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        lat = location.getLatitude();
                        lon = location.getLongitude();
                        CurrentPos.add(lat);
                        CurrentPos.add(lon);
                        addCurrentLocToGeoList(geofenceList, (ArrayList) CurrentPos);
                        final TextView textView = findViewById(R.id.textView);
                        if (location != null) {
                            textView.setText(DateFormat.getTimeInstance()
                                    .format(location.getTime()) + "\n"
                                    + "Latitude=" + location.getLatitude() + "\n"
                                    + "Longitude=" + location.getLongitude());
                        } else {
                            Toast.makeText(MainActivity.this, "Location null", Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                });
    return CurrentPos;

    }

    private final int MINIMUM_RECOMENDED_RADIUS=100;

    private PendingIntent createGeofencePendingIntent() {
        /* if (mCreateGeofencePendingIntent != null) {
           return mCreateGeofencePendingIntent;
        } */
        Intent intent = new Intent(this, GeofenceIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }

    private List createGeofenceList() {

        //Location location = new Location(LocationManager.GPS_PROVIDER);
        geofenceList.add(new Geofence.Builder()

                .setRequestId("Google HQ")
                .setCircularRegion(
                        37.421998,  //Latitude
                        -122.084, //Longitude
                        MINIMUM_RECOMENDED_RADIUS)
                .setLoiteringDelay(10*1000)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL)
                .build());
        geofenceList.add(new Geofence.Builder()
                .setRequestId("Dublin")
                .setCircularRegion(
                        53.3433833,  //Latitude
                        -6.2690049, //Longitude
                        MINIMUM_RECOMENDED_RADIUS)
                .setLoiteringDelay(10*1000)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL)
                .build());
        return geofenceList;
    }

    private List addCurrentLocToGeoList(List geofenceList, ArrayList<Double> CurrentPos) {
        geofenceList.add(new Geofence.Builder()
                .setRequestId("Current Location")
                .setCircularRegion(CurrentPos.get(0), CurrentPos.get(1),
                        MINIMUM_RECOMENDED_RADIUS)
                .setLoiteringDelay(10*1000)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL)
                .build());
        return geofenceList;

    }

   private GeofencingRequest createGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL);
        //builder.addGeofences(createGeofenceList());
        builder.addGeofences(createGeofenceList());
        return builder.build();
    }




    public void onClick(View v) {


        switch(v.getId()){
            case R.id.btngeo1:


                break;

            case R.id.btngeo2:


                break;
}
    }



}