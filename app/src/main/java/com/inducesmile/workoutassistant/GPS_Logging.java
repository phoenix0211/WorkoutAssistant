package com.inducesmile.workoutassistant;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

import java.io.FileOutputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class GPS_Logging extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    TextView totalDistanceTravelled;
    Long tsLong;
    String ts="";
    String FileName="data"+ts+".csv";

    String start="Start Logging Data";
    String stop="Stop Logging Data";
    Button dataLogger;

    boolean firstTimeInLocationManager=true;
    Double oldLatitude=0.0;
    Double oldLongitude=0.0;
    float totalDistance=0.0f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps__logging);
        dataLogger=findViewById(R.id.start_stop_button);
        totalDistanceTravelled=findViewById(R.id.totalDistance);
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
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
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

        }
        else{
            Location.distanceBetween(oldLatitude, oldLongitude,
                    lat, lonng, result);
            String as=String.valueOf(result[0]);
            totalDistance=result[0];
            totalDistanceTravelled.setText(as);
            oldLongitude=lonng;
            oldLatitude=lat;

        }

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



    @Override
    public void onMapReady(GoogleMap googleMap) {

    }
}
