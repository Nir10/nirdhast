package com.niranjan.admin.nirdhast;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.niranjan.admin.nirdhast.Utils.NetworkUtils;
import com.niranjan.admin.nirdhast.Utils.util;
import com.niranjan.admin.nirdhast.model.UserGeofence;
import com.niranjan.admin.nirdhast.model.UserLocation;

import java.io.IOException;
import java.text.ParseException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GeofenceActivity extends AppCompatActivity
        implements OnMapReadyCallback,GoogleMap.OnMapClickListener{
    GoogleMap mGoogleMap;
    boolean isMapReady = false;
    public final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1;

    @BindView(R.id.et_radius)
    EditText mRadiusEditText;

    @BindView(R.id.input_layout_radius)
    TextInputLayout inputLayoutRadius;

    @BindView(R.id.tv_geofence_adress)
    TextView mAddressTextView;

    float mRadius;

    Marker homeLocMarker;

    CircleOptions circleOptions;
    Circle circle;

    FusedLocationProviderClient mFusedLocationClient;

    Location mLocation;

    Bundle savedState;
    UserGeofence geofence;

    @BindView(R.id.layout_geofence)
    LinearLayout layoutGeofence;
    @BindView(R.id.layout_geofence_error)
    LinearLayout layoutError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savedState = savedInstanceState;

        setContentView(R.layout.activity_geofence);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.geofenceMap);
        mapFragment.getMapAsync(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mRadiusEditText.addTextChangedListener(new GeofenceActivity.MyTextWatcher(mRadiusEditText));

        if(checkInternetConnection()) {
            if (!checkPermission()) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            }
            showGeofenceLayout();
        } else {
            hideGeofenceLayout();
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String radius = mRadiusEditText.getText().toString().trim();
        if(!radius.isEmpty()){
            outState.putString(util.KEYS.RADIUS,radius);
        }
        if(homeLocMarker != null){
            Location homeLocMarkerLocation = new Location("provider");
            homeLocMarkerLocation.setLatitude(homeLocMarker.getPosition().latitude);
            homeLocMarkerLocation.setLongitude(homeLocMarker.getPosition().longitude);
            outState.putParcelable(util.KEYS.HOME_LOCATION,homeLocMarkerLocation);
        }

        String markerAddress = mAddressTextView.getText().toString().trim();
        if(!markerAddress.isEmpty()){
            outState.putString(util.KEYS.MARKER_ADDRESS,markerAddress);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setOnMapClickListener(this);
        isMapReady = true;

        if(savedState == null) {
            SharedPreferences sharedPref =
                    getSharedPreferences(util.KEYS.SHARED_PREFS_ID, Context.MODE_PRIVATE);

            if (sharedPref.contains(util.KEYS.GEOFENCE)) {
                Gson gson = new Gson();
                String json = sharedPref.getString(util.KEYS.GEOFENCE, null);
                geofence = gson.fromJson(json, UserGeofence.class);
                LatLng mLastLocation = new LatLng(geofence.getLatitude(), geofence.getLongitude());

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(mLastLocation)
                        .zoom(17).tilt(33)
                        .build();
                mGoogleMap.animateCamera(CameraUpdateFactory.
                        newCameraPosition(cameraPosition));

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
                mRadiusEditText.setText(String.valueOf(geofence.getRadius()));
                Location location = new Location("provider");
                location.setLatitude(geofence.getLatitude());
                location.setLongitude(geofence.getLongitude());

                Resources res = getResources();
                String addr = res.getString(R.string.marker_address_only,
                        util.getAddressFromLocation(GeofenceActivity.this, location));
                mAddressTextView.setText(addr);

            } else {
                getLastLocation();
            }
        } else {
            handleSavedState(savedState);
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {

        if(isMapReady){
            if(homeLocMarker == null){
             homeLocMarker = mGoogleMap.
                     addMarker(new MarkerOptions().position(latLng).
                             title(getString(R.string.marker_home)));
             homeLocMarker.showInfoWindow();

            } else {

                homeLocMarker.setPosition(latLng);
                homeLocMarker.showInfoWindow();
            }
            Location location = new Location("provider");
            location.setLatitude(latLng.latitude);
            location.setLongitude(latLng.longitude);

            Resources res = getResources();
            String addr = res.getString(R.string.marker_address_only,
                    util.getAddressFromLocation(GeofenceActivity.this,location));
            mAddressTextView.setText(addr);
        }
    }


    //TextWatcher Class
    private class MyTextWatcher implements TextWatcher {

        private View view;
        private MyTextWatcher(View view ){
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            switch(view.getId()) {
                case R.id.et_radius: validateRadius();
                    break;
            }
        }
    }

    public boolean validateRadius(){
        String radius = mRadiusEditText.getText().toString().trim();

        if(radius.isEmpty()) {
            inputLayoutRadius.setError(getString(R.string.err_msg_radius));
            requestFocus(mRadiusEditText);
            return false;
        } else {

            if(isValidRadius(radius)){
                inputLayoutRadius.setErrorEnabled(false);
                mRadius = Float.parseFloat(radius);
            }else {
                inputLayoutRadius.setError(getString(R.string.err_msg_invalid_radius));
                requestFocus(mRadiusEditText);
                return false;
            }

        }

        return true;
    }

    private void requestFocus(View view) {

        if(view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private static boolean isValidRadius(String radius) {

            float rad = Float.parseFloat(radius.trim());

        if(rad >= 100.0) {
            return true;
        } else {
            return false;
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

                                    LatLng currentLocation = new LatLng(location.getLatitude(),
                                            location.getLongitude());

                                    if (isMapReady) {

                                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                                .target(currentLocation)
                                                .zoom(17).tilt(33)
                                                .build();
                                        mGoogleMap.animateCamera(CameraUpdateFactory.
                                                newCameraPosition(cameraPosition));

                                        if (homeLocMarker == null) {
                                            homeLocMarker = mGoogleMap.addMarker(new MarkerOptions().
                                                    position(currentLocation)
                                                    .title(getString(R.string.marker_current)));
                                            homeLocMarker.showInfoWindow();
                                        } else {
                                            homeLocMarker.setPosition(currentLocation);
                                            homeLocMarker.showInfoWindow();
                                        }

                                    }
                                    Resources res = getResources();
                                    String addr = res.getString(R.string.marker_address_only,
                                            util.getAddressFromLocation(GeofenceActivity.this
                                                    ,location));
                                    mAddressTextView.setText(addr);
                                }
                            }
                        });
            } else {
                util.showLocationSettingDialog(this);
            }
        } else {
            Toast.makeText(this, getString(R.string.enable_location_permission)
                    , Toast.LENGTH_SHORT).show();
        }
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
                } else {
                    // permission denied, boo! Disable the
                    Toast.makeText(this, getString(R.string.enable_location_permission),
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_geofence,menu);

        if(!checkInternetConnection()){
            menu.findItem(R.id.menu_set).setVisible(false);
            menu.findItem(R.id.menu_geofence_clear).setVisible(false);
        }
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()){
            case R.id.menu_set:
                saveGeofence();
                break;
            case R.id.menu_geofence_clear:
                clearGeofence();
                break;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return true;
    }

    void saveGeofence(){

        if(validateRadius() && (homeLocMarker != null)){
            circleOptions = new CircleOptions().center(homeLocMarker.getPosition()).
                    fillColor(Color.argb(63,0,0,255))
                    .strokeColor(Color.BLUE).radius(mRadius);
            if(isMapReady){
                if(circle != null){
                    circle.remove();
                    circle= null;
                }
                circle = mGoogleMap.addCircle(circleOptions);
            }

            SharedPreferences sharedPref =
                    getSharedPreferences(util.KEYS.SHARED_PREFS_ID,Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            Gson gson = new Gson();
            UserGeofence geofence =
                    new UserGeofence(homeLocMarker.getPosition().latitude,
                            homeLocMarker.getPosition().longitude,mRadius);
            String json = gson.toJson(geofence);
            editor.putString(util.KEYS.GEOFENCE, json);
            editor.commit();
        }

    }

    void clearGeofence(){
        mRadiusEditText.setText("");
        if(homeLocMarker != null) {
            homeLocMarker.remove();
            homeLocMarker = null;
        }
        if(circle != null){
            circle.remove();
            circle = null;
        }
        mAddressTextView.setText("");

        SharedPreferences sharedPref =
                getSharedPreferences(util.KEYS.SHARED_PREFS_ID,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(util.KEYS.GEOFENCE);
        editor.commit();
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

    void showGeofenceLayout(){
        layoutGeofence.setVisibility(View.VISIBLE);
        layoutError.setVisibility(View.INVISIBLE);
    }

    void hideGeofenceLayout(){
        layoutGeofence.setVisibility(View.INVISIBLE);
        layoutError.setVisibility(View.VISIBLE);
    }


    //saves device state on orientation change
    void handleSavedState(Bundle savedInstanceState){

        if(savedInstanceState.containsKey(util.KEYS.HOME_LOCATION)){
            if(isMapReady) {
                mLocation = savedInstanceState.getParcelable(util.KEYS.HOME_LOCATION);
                LatLng latLng = new LatLng(mLocation.getLatitude(),
                        mLocation.getLongitude());
                homeLocMarker = mGoogleMap.
                        addMarker(new MarkerOptions().
                                position(latLng).title(getString(R.string.marker_home)));
                homeLocMarker.showInfoWindow();
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(latLng)
                        .zoom(13).tilt(33)
                        .build();
                mGoogleMap.animateCamera(CameraUpdateFactory.
                        newCameraPosition(cameraPosition));

                SharedPreferences sharedPref =
                        getSharedPreferences(util.KEYS.SHARED_PREFS_ID, Context.MODE_PRIVATE);

                    if (sharedPref.contains(util.KEYS.GEOFENCE)) {
                        Gson gson = new Gson();
                        String json = sharedPref.getString(util.KEYS.GEOFENCE, null);
                        geofence = gson.fromJson(json, UserGeofence.class);

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
        }

        if(savedInstanceState.containsKey(util.KEYS.MARKER_ADDRESS)){
            String address = savedInstanceState.getString(util.KEYS.MARKER_ADDRESS);
            mAddressTextView.setText(address);
        }

    }
}
