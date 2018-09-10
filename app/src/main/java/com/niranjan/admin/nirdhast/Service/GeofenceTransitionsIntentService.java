package com.niranjan.admin.nirdhast.Service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.google.gson.Gson;
import com.niranjan.admin.nirdhast.R;
import com.niranjan.admin.nirdhast.Utils.util;
import com.niranjan.admin.nirdhast.model.Caregiver;
import com.niranjan.admin.nirdhast.model.UserGeofence;

import java.util.List;


public class GeofenceTransitionsIntentService extends IntentService {

    String TAG = GeofenceTransitionsIntentService.class.getSimpleName();
    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionsIntentService");
    }

    Caregiver caregiver1,caregiver2;

    @Override
    protected void onHandleIntent(Intent intent) {
        Intent broadcastIntent = new Intent(util.ACTION.GEOFENCE_BROADCAST_ACTION);

        SharedPreferences sharedPref =
                getSharedPreferences(util.KEYS.SHARED_PREFS_ID, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String caregiverString1 = sharedPref.getString(util.KEYS.CAREGIVER1, null);
        caregiver1 = gson.fromJson(caregiverString1, Caregiver.class);

        String caregiverString2 = sharedPref.getString(util.KEYS.CAREGIVER2, null);
        caregiver2 = gson.fromJson(caregiverString2, Caregiver.class);

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        //check whether geofence has error
        if (geofencingEvent.hasError()) {
            // geofence has error
            String error = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.getErrorCode());
            broadcastIntent.putExtra(util.EXTRA.GEOFENCE_SUCCESS, true);
            broadcastIntent.putExtra(util.EXTRA.GEOFENCE_MESSAGE, error);
            sendBroadcast(broadcastIntent);

            return;
        }

        // Get the geofence transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            String reqId = triggeringGeofences.get(0).getRequestId();

            //create an intent to call Intentservice to send SMS
            Intent smsIntent = new Intent(getApplicationContext(),SendMessageIntentService.class);

            smsIntent.putExtra(util.EXTRA.SMS_CAREGIVER1,caregiver1);
            smsIntent.putExtra(util.EXTRA.SMS_CAREGIVER2,caregiver2);

            if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

                //send broadcast to LocationService
                broadcastIntent.putExtra(util.EXTRA.GEOFENCE_SUCCESS, true);
                broadcastIntent.putExtra(util.EXTRA.GEOFENCE_MESSAGE,
                        getString(R.string.geofence_enter_message));
                sendBroadcast(broadcastIntent);

                smsIntent.putExtra(util.EXTRA.SMS_MESSAGE,getString(R.string.geofence_enter_message));
                //Start service to send sms to caregivers
                startService(smsIntent);

            } else if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                //send broadcast to LocationService
                broadcastIntent.putExtra(util.EXTRA.GEOFENCE_SUCCESS, true);
                broadcastIntent.putExtra(util.EXTRA.GEOFENCE_MESSAGE,
                        getString(R.string.geofence_exit_message));
                sendBroadcast(broadcastIntent);

                smsIntent.putExtra(util.EXTRA.SMS_MESSAGE,getString(R.string.geofence_exit_message));
                //Start service to send sms to caregivers
                startService(smsIntent);

            }
        } else {
            //send broadcast to LocationService
                broadcastIntent.putExtra(util.EXTRA.GEOFENCE_SUCCESS, false);
                broadcastIntent.putExtra(util.EXTRA.GEOFENCE_MESSAGE,
                        geofencingEvent.getErrorCode());
                sendBroadcast(broadcastIntent);
        }
    }
}
