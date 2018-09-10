package com.niranjan.admin.nirdhast.Service;


import android.app.Notification;
import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;

import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.niranjan.admin.nirdhast.MainActivity;
import com.niranjan.admin.nirdhast.R;
import com.niranjan.admin.nirdhast.Utils.util;
import com.niranjan.admin.nirdhast.model.UserGeofence;
import com.niranjan.admin.nirdhast.model.UserLocation;

import java.util.ArrayList;


public  class LocationService extends Service {
    private static final String LOG_TAG = "LocationService";
    private static final String TAG = LocationService.class.getSimpleName();

    public static boolean IS_SERVICE_RUNNING = false;

    LocationRequest mLocationRequest;
    FusedLocationProviderClient mFusedLocationClient;
    LocationSettingsRequest.Builder mBuilder;
    private LocationCallback mLocationCallback;

    boolean mRequestingLocationUpdates = false;

    protected Location mLastLocation;

    private GeofencingClient mGeofencingClient;

    float mGeofenceRadius = 10;
    boolean addGeofence = false;
    UserGeofence userGeofence;

    LatLng homeLocation;

    ArrayList<Geofence> mGeofenceList;
    PendingIntent mGeofencePendingIntent;

    private BroadcastReceiver mGeofenceBroadcastReceiver;
    Boolean isRegRecvr = true;

    FirebaseDatabase database;
    DatabaseReference myRef;
    private FirebaseAuth mAuth;

    private static final String CHANNEL_ID = "channel_01";

    private NotificationManager mNotificationManager;

    public LocationService() {
    }

    @Override
    public void onCreate() {
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        myRef.keepSynced(true);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        if (intent.getAction().equals(util.ACTION.STARTFOREGROUND_ACTION)) {
            //starts the service

            //Checks if geofence is to be added.
            //GEOFENCE_ADD = true -> add geofence
            // GEOFENCE_ADD = true -> do not add Geofence
            if(intent.hasExtra(util.EXTRA.GEOFENCE_ADD)){

                addGeofence = intent.getBooleanExtra(util.EXTRA.GEOFENCE_ADD,false);

                userGeofence = intent.getParcelableExtra(util.EXTRA.GEOFENCE);

                homeLocation = new LatLng(userGeofence.getLatitude(),userGeofence.getLongitude());
                mGeofenceRadius = userGeofence.getRadius();

            }
            Log.i(LOG_TAG, "Received Start Foreground Intent ");
            showNotification(null);

            IS_SERVICE_RUNNING = true;
            initialize();
            getLastLocation();
            if(addGeofence) {
                startGeofence();
            }
            Toast.makeText(this, getString(R.string.tracking_started),
                    Toast.LENGTH_SHORT).show();
            createLocationRequest();
            mRequestingLocationUpdates = true;
            startLocationUpdates();

        } else if (intent.getAction().equals(
                util.ACTION.STOPFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Stop Foreground Intent");
            //stops the service


            IS_SERVICE_RUNNING = false;
            stopLocationUpdates();
            if(addGeofence) {
                stopGeofence();
            }
            Toast.makeText(this, getString(R.string.tracking_stopped),
                    Toast.LENGTH_SHORT).show();
            stopForeground(true);
            stopSelf();
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
       return null;
    }

    void showNotification(Location location){

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(util.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_launcher_background);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(util.getLocationTitle(getApplicationContext()))
                .setContentText(util.getLocationText(location,getApplicationContext()))
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .setTicker(util.getLocationText(location,getApplicationContext()))
                .setOngoing(true);


        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }

        Notification notification = builder.build();

        startForeground(util.NOTIFICATION_ID.FOREGROUND_SERVICE,
                notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //initialize location and geofence objects,location callbacks and geofence broadcast receiver
    void initialize() {

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mGeofencingClient = LocationServices.getGeofencingClient(this);

        mGeofenceList = new ArrayList<>();

        if(addGeofence) {
            //create geofence list
            mGeofenceList.add(new Geofence.Builder()
                    .setRequestId("1")
                    .setCircularRegion(
                            homeLocation.latitude,
                            homeLocation.longitude,
                            mGeofenceRadius
                    )
                    .setExpirationDuration(100000000)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build());

            //handle geofence broadcast
            mGeofenceBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i("service", "broadcast received");
                    if (intent.getBooleanExtra(util.EXTRA.GEOFENCE_SUCCESS, false)) {
                        String msg = intent.getStringExtra(util.EXTRA.GEOFENCE_MESSAGE);
                        Log.i(TAG,msg);
                    } else {
                        String msg = intent.getStringExtra(util.EXTRA.GEOFENCE_MESSAGE);
                        Log.e(TAG,msg);
                    }

                }
            };
        }


        //handle location updates from Google LocationServices
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // ...

                    float distance = mLastLocation.distanceTo(location);

                    //if distance between last location and current location is greater then
                    // 50 then save location
                    if(distance >= 50.00) {
                        UserLocation userLocation = new UserLocation(location.getLatitude(),
                                location.getLongitude(),
                                util.getFormatedCurrentDate(),
                                util.getFormatedCurrentDate().split(" ")[0]);

                        saveLastLocation(userLocation);
                        saveLocationUpdates(userLocation);

                        showNotification(location);

                        mLastLocation = location;
                        //send location broadcast to LivTrack Activity
                        Intent broadcastIntent;
                        broadcastIntent = new Intent(util.ACTION.LOCATION_BROADCAST_ACTION);
                        broadcastIntent.putExtra(util.EXTRA.LOCATION, location);
                        sendBroadcast(broadcastIntent);
                    }
                }
            }
        };
    }

    //get users last location
    void getLastLocation(){
        if(checkPermission()) {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {

                    if(location != null){

                        UserLocation userLocation = new UserLocation(location.getLatitude(),
                                location.getLongitude(),
                                util.getFormatedCurrentDate(),
                                util.getFormatedCurrentDate().split(" ")[0]);

                        saveLastLocation(userLocation);
                        saveLocationUpdates(userLocation);

                        mLastLocation = location;
                        showNotification(location);
                        //send location broadcast to LivTrack Activity
                        Intent broadcastIntent;
                        broadcastIntent = new Intent(util.ACTION.LOCATION_BROADCAST_ACTION);
                        broadcastIntent.putExtra(util.EXTRA.LOCATION,location);
                        sendBroadcast(broadcastIntent);
                    }
                }
            });
        }
    }

    //check whether permissions are enabled
    boolean checkPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            return false;
        } else {
            // Permission has already been granted
            return true;
        }
    }

    //creates Location Request to receive location updates
    void createLocationRequest(){
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mBuilder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
    }

    //starts location updates
    private void startLocationUpdates(){
        if(checkPermission()) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        }
    }

    //stops location updates
    private void stopLocationUpdates(){
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }



    //creates geofence request
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        //builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.setInitialTrigger(0);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    //creates Pending Intent for geofence
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }


    //starts geofence monitoring
    void startGeofence(){

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted
            registerReceiver();
            mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // HomeGeofence added
                            // ...

                            Log.i(TAG,"Geofence Added");

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Failed to add geofences
                            // ...
                            e.printStackTrace();
                            Log.e(TAG,e.getMessage());

                        }
                    });

        }

    }


    //Stops geofence monitoring
    void stopGeofence(){
        mGeofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Log.i(TAG,"Geofence Removed");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to remove geofences
                        // ...
                        Log.e(TAG,"Geofence Remove Error");
                    }
                });

        unRegisterReceiver();
    }

    //registers reciver to recvieve geofence transition updates
    private void registerReceiver() {
        if (mGeofenceBroadcastReceiver != null) {

            registerReceiver(mGeofenceBroadcastReceiver,
                    new IntentFilter(util.ACTION.GEOFENCE_BROADCAST_ACTION));
            isRegRecvr = true;
        }

    }

    //registers reciver to recvieve geofence transition updates
    private void unRegisterReceiver() {
        Log.i("br", "uuuunregistered");
        if (mGeofenceBroadcastReceiver != null && isRegRecvr) {

            unregisterReceiver(mGeofenceBroadcastReceiver);
            isRegRecvr = false;

        }
    }

    //saves user's last location to Firebase Realtime database
    void saveLastLocation(UserLocation userLocation){
        myRef.child("userLastLocation")
                .child(mAuth.getUid()).
                setValue(userLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){

                }else{
                  Log.e(TAG,task.getException().getMessage());

                }
            }
        });
    }


    //pushes user location updates to Firebase Realtime Database
    void saveLocationUpdates(UserLocation userLocation){
        myRef.child("userLocationUpdates")
                .child(mAuth.getUid()).push().
                setValue(userLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isComplete()){

                }else{
                    Log.e(TAG,task.getException().getMessage());
                }
            }
        });
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }
}
