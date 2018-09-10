package com.niranjan.admin.nirdhast;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.util.Strings;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.niranjan.admin.nirdhast.Service.LocationService;
import com.niranjan.admin.nirdhast.Utils.NetworkUtils;
import com.niranjan.admin.nirdhast.Utils.util;
import com.niranjan.admin.nirdhast.model.UserGeofence;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LiveTrackActivity extends AppCompatActivity
        implements OnMapReadyCallback{

    GoogleMap mGoogleMap;
    boolean isMapReady = false;
    public final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1;
    BroadcastReceiver mBroadcastReceiver;
    boolean isRegRecvr = false;

    Marker currentLocMarker,homeLocMarker;

    int MAP_ZOOM = 15;

    CircleOptions circleOptions;
    Circle circle;

    FusedLocationProviderClient mFusedLocationClient;

    @BindView(R.id.tv_live_track_address)
    TextView mAddressTextView;
    @BindView(R.id.layout_liv_track)
    LinearLayout layoutLiveTrack;
    @BindView(R.id.layout_liv_track_error)
    LinearLayout layoutError;

    UserGeofence geofence = null;
    boolean ADD_GEOFENCE = false;
    Menu mMenu;

    Bundle savedState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_track);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        savedState = savedInstanceState;

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.trackingMap);
        mapFragment.getMapAsync(this);
        ButterKnife.bind(this);

        if(checkInternetConnection()) {
            if (checkPermission()) {
                initialize();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            }
           showLivTrackLayout();
        } else {
            hideLivTrackLayout();
        }


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(currentLocMarker != null){
            Location location = new Location("provider");
            location.setLatitude(currentLocMarker.getPosition().latitude);
            location.setLongitude(currentLocMarker.getPosition().longitude);
            outState.putParcelable(util.KEYS.CURRENT_LOCATION,location);
        }

        String address = mAddressTextView.getText().toString().trim();
        if(!address.isEmpty()){
            outState.putString(util.KEYS.MARKER_ADDRESS,address);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        isMapReady = true;

        if(savedState == null) {
            SharedPreferences sharedPref =
                    getSharedPreferences(util.KEYS.SHARED_PREFS_ID, Context.MODE_PRIVATE);

            if (sharedPref.contains(util.KEYS.GEOFENCE)) {
                Gson gson = new Gson();
                String json = sharedPref.getString(util.KEYS.GEOFENCE, null);
                geofence = gson.fromJson(json, UserGeofence.class);

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(geofence.getLatitude(), geofence.getLongitude()))
                        .zoom(MAP_ZOOM).tilt(33)
                        .build();
                mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                if (homeLocMarker == null) {
                    homeLocMarker = mGoogleMap.addMarker(new MarkerOptions().
                            position(new LatLng(geofence.getLatitude(), geofence.getLongitude()))
                            .title(getString(R.string.marker_home)));
                    homeLocMarker.showInfoWindow();
                }


                circleOptions = new CircleOptions().
                        center(new LatLng(geofence.getLatitude(), geofence.getLongitude())).
                        fillColor(Color.argb(63, 0, 0, 255))
                        .strokeColor(Color.BLUE).radius(geofence.getRadius());
                if (circle != null) {
                    circle.remove();
                    circle = null;
                }
                circle = mGoogleMap.addCircle(circleOptions);
                Location location = new Location("provider");
                location.setLatitude(geofence.getLatitude());
                location.setLongitude(geofence.getLongitude());

                Resources res = getResources();
                String addr = res.getString(R.string.marker_address_only,
                        util.getAddressFromLocation(LiveTrackActivity.this,location));
                mAddressTextView.setText(addr);
                ADD_GEOFENCE = true;

            }
            if(!LocationService.IS_SERVICE_RUNNING) {
                getLastLocation();
            }
        } else {
            handleSavedState(savedState);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tracking,menu);

        if(!checkInternetConnection()){
            menu.findItem(R.id.menu_tracking).setVisible(false);
        } else {
            if (LocationService.IS_SERVICE_RUNNING) {
                menu.findItem(R.id.menu_tracking).setTitle(getString(R.string.menu_stop));
            } else {
                menu.findItem(R.id.menu_tracking).setTitle(getString(R.string.menu_start));
            }
        }
        mMenu = menu;
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent service = new Intent(LiveTrackActivity.this, LocationService.class);

        if(item.getItemId() == R.id.menu_tracking){

            if(checkUserDetailsAndCaregiversAreSet()) {
                //checkLocationSettingsEnabled();
                if(util.isLocationServiceEnabled(this)) {
                    if (!LocationService.IS_SERVICE_RUNNING) {
                        service.setAction(util.ACTION.STARTFOREGROUND_ACTION);
                        LocationService.IS_SERVICE_RUNNING = true;
                        registerReceiver();
                        item.setTitle(R.string.menu_stop);
                    } else {
                        service.setAction(util.ACTION.STOPFOREGROUND_ACTION);
                        LocationService.IS_SERVICE_RUNNING = false;
                        unRegisterReceiver();
                        item.setTitle(R.string.menu_start);
                    }
                    if (ADD_GEOFENCE) {
                        service.putExtra(util.EXTRA.GEOFENCE_ADD, ADD_GEOFENCE);
                        service.putExtra(util.EXTRA.GEOFENCE, geofence);
                    }

                    startService(service);
                } else {
                    util.showLocationSettingDialog(this);
                }
            } else {
                Toast.makeText(this, getString(R.string.enter_user_caregiver_details),
                        Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    void initialize(){

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("service", "broadcast received");
                Log.v("data recvd",
                        intent.getBooleanExtra("SUCCESS",
                        false) + "\ncardtype" +
                                intent.getIntExtra("cardType", 0));

                if(intent.hasExtra(util.EXTRA.LOCATION)) {
                    Location location = intent.getParcelableExtra(util.EXTRA.LOCATION);

                    if(isMapReady) {
                        LatLng currentLocation =
                                new LatLng(location.getLatitude(), location.getLongitude());
                        if (currentLocMarker == null) {
                            currentLocMarker = mGoogleMap.
                                    addMarker(new MarkerOptions().position(currentLocation)
                                            .title(getString(R.string.marker_current))
                                            .icon(BitmapDescriptorFactory.
                                                    defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                            currentLocMarker.showInfoWindow();

                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(currentLocation)
                                    .zoom(MAP_ZOOM).tilt(33)
                                    .build();
                            mGoogleMap.animateCamera(CameraUpdateFactory.
                                    newCameraPosition(cameraPosition));
                        } else {

                            currentLocMarker.setTitle(getString(R.string.marker_current));
                            currentLocMarker.setPosition(currentLocation);
                            currentLocMarker.showInfoWindow();

                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(currentLocation)
                                    .zoom(MAP_ZOOM).tilt(33)
                                    .build();
                            mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        }

                        Resources res = getResources();
                        String addrtime = res.getString(R.string.marker_address_time,
                                util.getAddressFromLocation(LiveTrackActivity.this,location)
                                , util.getFormatedCurrentDate());

                        mAddressTextView.setText(addrtime);
                    }
                }

            }
        };

    }

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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    initialize();
                } else {
                    // permission denied
                    Toast.makeText(this, getString(R.string.enable_location_permission)
                            , Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
        }
    }

    private void registerReceiver() {
        if (mBroadcastReceiver != null) {
            registerReceiver(mBroadcastReceiver, new IntentFilter(util.ACTION.LOCATION_BROADCAST_ACTION));
            isRegRecvr = true;
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
            unRegisterReceiver();
    }

    @Override
    protected void onResume() {
        if(LocationService.IS_SERVICE_RUNNING){
            registerReceiver();
        }
        super.onResume();
    }

    private void unRegisterReceiver() {
        Log.i("br", "uuuunregistered");
        if (mBroadcastReceiver != null && isRegRecvr) {
            unregisterReceiver(mBroadcastReceiver);
            isRegRecvr = false;
        }
    }

    void getLastLocation(){
        if(checkPermission()) {
            if(util.isLocationServiceEnabled(this)) {
                mFusedLocationClient.getLastLocation().
                        addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {

                        if (location != null) {

                            if (isMapReady) {

                                LatLng currentLocation =
                                        new LatLng(location.getLatitude(), location.getLongitude());

                                if (currentLocMarker == null) {
                                    currentLocMarker = mGoogleMap.addMarker(new MarkerOptions()
                                            .position(currentLocation).
                                                    title(getString(R.string.marker_current))
                                            .icon(BitmapDescriptorFactory.
                                                    defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                                    currentLocMarker.showInfoWindow();

                                    CameraPosition cameraPosition = new CameraPosition.Builder()
                                            .target(currentLocation)
                                            .zoom(MAP_ZOOM).tilt(33)
                                            .build();
                                    mGoogleMap.
                                            animateCamera(CameraUpdateFactory.
                                                    newCameraPosition(cameraPosition));
                                } else {
                                    currentLocMarker.setTitle(getString(R.string.marker_current));
                                    currentLocMarker.setPosition(currentLocation);
                                    CameraPosition cameraPosition = new CameraPosition.Builder()
                                            .target(currentLocation)
                                            .zoom(MAP_ZOOM).tilt(33)
                                            .build();
                                    mGoogleMap.animateCamera(CameraUpdateFactory.
                                            newCameraPosition(cameraPosition));
                                }

                                Resources res = getResources();
                                String addrtime = res.getString(R.string.marker_address_time,
                                        util.getAddressFromLocation(LiveTrackActivity.this,location)
                                        , util.getFormatedCurrentDate());

                                mAddressTextView.setText(addrtime);
                            }


                        }

                    }
                });
            } else{
                util.showLocationSettingDialog(this);
            }
        } else {
            Toast.makeText(this, getString(R.string.enable_location_permission),
                    Toast.LENGTH_SHORT).show();
        }
    }

    boolean checkUserDetailsAndCaregiversAreSet(){

        SharedPreferences sharedPrefs =
                getSharedPreferences(util.KEYS.SHARED_PREFS_ID,MODE_PRIVATE);
        if(sharedPrefs.contains(util.KEYS.USER_DETAILS)
                && sharedPrefs.contains(util.KEYS.CAREGIVER1)
                && sharedPrefs.contains(util.KEYS.CAREGIVER2)){
            return  true;
        }
        return false;
    }

    boolean checkInternetConnection(){
        try {
            if (NetworkUtils.isConnected()) {
                return true;
            } else {
                return false;
            }
        } catch (InterruptedException e){
            e.printStackTrace();
            return false;
        } catch (IOException e){
            e.printStackTrace();
            return false;
        }

    }

    void showLivTrackLayout(){
        layoutLiveTrack.setVisibility(View.VISIBLE);
        layoutError.setVisibility(View.INVISIBLE);
    }

    void hideLivTrackLayout(){
        layoutLiveTrack.setVisibility(View.INVISIBLE);
        layoutError.setVisibility(View.VISIBLE);
    }


    //saves device state on orientation change
    void handleSavedState(Bundle savedInstanceState){

        SharedPreferences sharedPref =
                getSharedPreferences(util.KEYS.SHARED_PREFS_ID, Context.MODE_PRIVATE);

        if (sharedPref.contains(util.KEYS.GEOFENCE)) {
            if(isMapReady) {
                Gson gson = new Gson();
                String json = sharedPref.getString(util.KEYS.GEOFENCE, null);
                geofence = gson.fromJson(json, UserGeofence.class);

                LatLng homeLatLng = new LatLng(geofence.getLatitude(),
                        geofence.getLongitude());
                CameraPosition homeCameraPosition = new CameraPosition.Builder()
                        .target(homeLatLng)
                        .zoom(13).tilt(33)
                        .build();
                mGoogleMap.animateCamera(CameraUpdateFactory.
                        newCameraPosition(homeCameraPosition));

                if(homeLocMarker == null) {
                    homeLocMarker = mGoogleMap.
                            addMarker(new MarkerOptions().
                                    position(homeLatLng).title(getString(R.string.marker_home)));
                    homeLocMarker.showInfoWindow();
                } else {
                    homeLocMarker.setPosition(homeLatLng);
                    homeLocMarker.showInfoWindow();
                }

                circleOptions = new CircleOptions().
                        center(new LatLng(geofence.getLatitude(), geofence.getLongitude())).
                        fillColor(Color.argb(63, 0, 0, 255))
                        .strokeColor(Color.BLUE).radius(geofence.getRadius());
                if (circle != null) {
                    circle.remove();
                    circle = null;
                }
                circle = mGoogleMap.addCircle(circleOptions);
            }
        }

        if(savedInstanceState.containsKey(util.KEYS.CURRENT_LOCATION)){
            if(isMapReady) {
                Location mLocation = savedInstanceState.getParcelable(util.KEYS.CURRENT_LOCATION);
                LatLng latLng = new LatLng(mLocation.getLatitude(),
                        mLocation.getLongitude());
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(latLng)
                        .zoom(13).tilt(33)
                        .build();
                mGoogleMap.animateCamera(CameraUpdateFactory.
                        newCameraPosition(cameraPosition));

                if(currentLocMarker == null) {
                    currentLocMarker = mGoogleMap.
                            addMarker(new MarkerOptions().
                                    position(latLng).title(getString(R.string.marker_current))
                                    .icon(BitmapDescriptorFactory.
                                            defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                    currentLocMarker.showInfoWindow();
                } else {
                    currentLocMarker.setPosition(latLng);
                    currentLocMarker.showInfoWindow();
                }

            }
        }

        if(savedInstanceState.containsKey(util.KEYS.MARKER_ADDRESS)){
            String address = savedInstanceState.getString(util.KEYS.MARKER_ADDRESS);
            mAddressTextView.setText(address);
        }

    }
}
