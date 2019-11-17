package com.inducesmile.workoutassistant;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = GeofenceBroadcastReceiver.class.getSimpleName();

    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
//            String errorMessage = GeofenceErrorMessages.getErrorString(this,
//                    geofencingEvent.getErrorCode());
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(
                    this,
                    geofenceTransition,
                    triggeringGeofences
            );
            Log.i(TAG, "Calling wakeLogger");
            wakeLogger(geofenceTransitionDetails, context);
        } else {
            // Log the error.
        }
    }

    private void wakeLogger(String msg, Context context) {
        Intent notificationIntent = GPS_Logging.makeNotificationIntent(
                context, msg
        );
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(GPS_Logging.class);
        stackBuilder.addNextIntent(notificationIntent);
    }

    private String getGeofenceTransitionDetails(GeofenceBroadcastReceiver geofenceBroadcastReceiver, int geofenceTransition, List<Geofence> triggeringGeofences) {
        ArrayList<String> triggeringGeofencesList = new ArrayList<>();
        for ( Geofence geofence : triggeringGeofences ) {
            triggeringGeofencesList.add( geofence.getRequestId() );
        }

        String status = null;
        if ( geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL ) {
            status = "Remained in geofence ";

        }
        else if ( geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT ) {
            status = "Exiting ";

        }
        else if ( geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            status = "Entering ";
        }
        return status + TextUtils.join( ", ", triggeringGeofencesList);
    }
}
