package com.inducesmile.workoutassistant;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class GPS_Logging extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<Status> {

    private static final String TAG = GPS_Logging.class.getSimpleName();

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    TextView totalDistanceTravelled;
    static TextView totalLapsCompleted;
    Long tsLong;
    String ts="";
    String FileName="data"+ts+".csv";

    String start="Start Logging Data";
    String stop="Stop Logging Data";
    Button dataLogger;

    boolean firstTimeInLocationManager=true;
    static Double oldLatitude=0.0;
    static Double oldLongitude=0.0;
    static float totalDistance=0.0f;
    static float lapDistance = 0.0f;

    private GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps__logging);
        dataLogger=findViewById(R.id.start_stop_button);
        totalDistanceTravelled=findViewById(R.id.totalDistance);
        totalLapsCompleted=findViewById(R.id.laps);
        dataLogger.setText(start);
        dataLogger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if((dataLogger.getText()).toString().equals(start)){
                    buildGoogleApiClient();
                    dataLogger.setText(stop);
                    tsLong = System.currentTimeMillis()/1000;
                    ts = tsLong.toString();
                    FileName="data"+ts+".csv";
                }
                else
                {
                    mGoogleApiClient.disconnect();
                    dataLogger.setText(start);
                    firstTimeInLocationManager=true;



                }

            }
        });
        geofencingClient = LocationServices.getGeofencingClient(this);
    }

    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient=new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest=new LocationRequest();
        mLocationRequest.setInterval(3000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ContextCompat.checkSelfPermission(GPS_Logging.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {



            // Permission is not granted

            if (ActivityCompat.shouldShowRequestPermissionRationale(GPS_Logging.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                new AlertDialog.Builder(this)
                        .setTitle("Required Location Permission")
                        .setMessage("You have to give this permission to acess this feature")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(GPS_Logging.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        1);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(GPS_Logging.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        else{
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this); //this baiscally request to fetch location after the interval specified above.
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private static int laps = 0;
    private static TreeMap<Float, String> distanceMap = new TreeMap<>();

    @Override
    public void onLocationChanged(Location location) {
        String data=",,,";
        mLastLocation=location;
        Double lat=location.getLatitude();
        Double lonng=location.getLongitude();
        Float speed=location.getSpeed();
        float result[]=new float[1];
        if(firstTimeInLocationManager){
            oldLatitude=lat;
            oldLongitude=lonng;
            totalDistance=0.0f;
            lapDistance=0.0f;
            startGeofence(oldLatitude, oldLongitude);
            totalLapsCompleted.setText("0");
        }
        else{
            Location.distanceBetween(oldLatitude, oldLongitude,
                    lat, lonng, result);

            totalDistance=result[0]+totalDistance;
            lapDistance=result[0]+lapDistance;
            String as=String.valueOf(totalDistance);

            totalDistanceTravelled.setText(as);
            oldLongitude=lonng;
            oldLatitude=lat;
            //Toast.makeText(getApplicationContext(),"Insterted Value",Toast.LENGTH_LONG).show();

        }
        distanceMap.put(lapDistance, lat.toString() + ',' + lonng.toString());
        Log.i(TAG, distanceMap.get(lapDistance));
        data=""+lonng+","+lat+","+totalDistance+"\n";
        try {
            FileOutputStream fileOutputStream = openFileOutput(FileName, Context.MODE_APPEND);
            fileOutputStream.write(data.getBytes());
            fileOutputStream.close();

        }catch (Exception e){
            e.printStackTrace();
        }
        firstTimeInLocationManager=false;
    }

    private String REQUEST_ID = "startLocation";
    private float GEOFENCE_RADIUS = 50.0f;
    private static final long GEO_DURATION = 60 * 60 * 1000;
    private static String message;
    private static final String NOTIFICATION_MSG = "NOTIFICATION MSG";

    public static Intent makeNotificationIntent(Context context, String msg) {
        Intent intent = new Intent( context, GPS_Logging.class );
        intent.putExtra( NOTIFICATION_MSG, msg );
        message = msg;
        if(msg.contains("Remained"))
        {
            Log.i(TAG, "calling lapChange");
            lapChange(context);
        }
        else if(msg.contains("Exiting"))
        {
            Toast.makeText(context, "Exiting Detected", Toast.LENGTH_LONG).show();
        }
        else if(msg.contains("Entering")){
            Toast.makeText(context, "Entering the Geofence", Toast.LENGTH_LONG).show();
            Log.i(TAG, "Entering the geofence");
        }
        return intent;
    }

    private static String getMapValue(float key){
        Log.i(TAG, "key: " + key);
        Map.Entry<Float,String> low = distanceMap.floorEntry(key);
        Map.Entry<Float,String> high = distanceMap.ceilingEntry(key);
        String res = null;
        if (low != null && high != null) {
            res = Math.abs(key-low.getKey()) < Math.abs(key-high.getKey())
                    ?   low.getValue()
                    :   high.getValue();
        } else if (low != null || high != null) {
            res = low != null ? low.getValue() : high.getValue();
        }
        Log.i(TAG, "res: " + res);
        return res;
    }

    private static void lapChange(Context context) {
//        distanceMap.put(0.0f, "30.767935,76.788814");
//        distanceMap.put(93.03f, "30.768544,76.789908");
//        distanceMap.put(185.55f, "30.769097,76.789661");
//        distanceMap.put(280.00f, "30.768489,76.788513");
        if(lapDistance > 360 && lapDistance < 500){
            Toast.makeText(context, "Lap completed", Toast.LENGTH_LONG).show();
            float unit = lapDistance/4;
            Log.i(TAG, "unit: " + unit);
            String[] a = getMapValue(0.0f).split(",", 2);
            String[] b = getMapValue(unit).split(",", 2);
            String[] c = getMapValue(2*unit).split(",", 2);
            String[] d = getMapValue(3*unit).split(",", 2);
            float[] diag_1 = new float[1];
            float[] diag_2 = new float[1];
            Location.distanceBetween(Double.valueOf(a[0]), Double.valueOf(a[1]), Double.valueOf(c[0]), Double.valueOf(c[1]), diag_1);
            Location.distanceBetween(Double.valueOf(b[0]), Double.valueOf(b[1]), Double.valueOf(d[0]), Double.valueOf(d[1]), diag_2);
            float area = 0.5f*diag_1[0]*diag_2[0];
            Log.i(TAG, "Area: " + area);
            if(area > 5000f && area < 10000f){
                laps++;
                totalLapsCompleted.setText(String.valueOf(laps));
                Toast.makeText(context, "Valid Lap, Area: " + area, Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(context, "Invalid Lap, Area: " + area, Toast.LENGTH_LONG).show();
            }
        }
        else{
            Toast.makeText(context, "Not a lap", Toast.LENGTH_LONG).show();
        }
        lapDistance = 0.0f;
        distanceMap.clear();
        //distanceMap.put(lapDistance, oldLatitude.toString() + "," + oldLongitude.toString());
    }

    private void startGeofence(double lat, double lng){
        Geofence geofence = createGeofence(lat, lng);
        GeofencingRequest geofencingRequest = createGeofencingRequest(geofence);
        geofencingClient.addGeofences(geofencingRequest, createGeofencePendingIntent())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofences added
                        // ...
                        Toast.makeText(getApplicationContext(), "Lap Started", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to add geofences
                        // ...
                        Toast.makeText(getApplicationContext(), "Failed to add geofence", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private Geofence createGeofence(double lat, double lng){
        return new Geofence.Builder()
                .setRequestId(REQUEST_ID)
                .setCircularRegion(lat, lng, GEOFENCE_RADIUS)
                .setExpirationDuration(GEO_DURATION)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_ENTER)
                .setLoiteringDelay(8000)
                .build();
    }

    private GeofencingRequest createGeofencingRequest(Geofence geofence) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL);
        builder.addGeofence(geofence);
        return builder.build();
    }

    private PendingIntent createGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        geofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    @Override
    public void onResult(@NonNull Status status) {
        if ( status.isSuccess() ) {
            Toast toast = Toast.makeText(this, status.toString(), Toast.LENGTH_LONG);
            toast.show();
        } else {
            // inform about fail
        }
    }
}