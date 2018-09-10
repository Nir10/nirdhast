package com.niranjan.admin.nirdhast;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import com.niranjan.admin.nirdhast.Utils.NetworkUtils;
import com.niranjan.admin.nirdhast.Utils.util;
import com.niranjan.admin.nirdhast.model.UserLocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HistoryActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener{

    final String TAG = HistoryActivity.class.getSimpleName();

    public final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1;

    GoogleMap mGoogleMap;
    boolean isMapReady = false;

    @BindView(R.id.tv_date)
    TextView mDateTextView;
    @BindView(R.id.btn_date)
    Button mDateButton;
    @BindView(R.id.tv_marker_address)
    TextView mMarkerAdressTextView;

    @BindView(R.id.layout_history)
    LinearLayout layoutHistory;
    @BindView(R.id.layout_history_error)
    LinearLayout layoutError;

    Marker currentLocMarker;

    FusedLocationProviderClient mFusedLocationClient;

    FirebaseDatabase database;
    DatabaseReference myRef;
    private FirebaseAuth mAuth;
    ValueEventListener mValueEventListener;
    ChildEventListener mChildEventListener;

    List<UserLocation> mLocationList;
    List<Marker> mMarkerList;
    Location mLastLocation,clickedMarkerLocation;

    String clickedMarkerTitle = null;

    String date;
    String time;
    Bundle savedState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savedState = savedInstanceState;

        setContentView(R.layout.activity_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);

        mLocationList = new ArrayList<>();
        mMarkerList = new ArrayList<>();

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.historyMap);
        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        myRef.keepSynced(true);

        if(checkInternetConnection()){
            showHistoryLayout();
            if(checkPermission()) {

                if(savedInstanceState == null) {
                    getLastLocation();
                }
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            }
        } else {
            hideHistoryLayout();
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String date = mDateTextView.getText().toString().trim();
        if(!date.isEmpty()) {
            outState.putString(util.KEYS.DATE, date);
        }
        if(mLastLocation != null){
            outState.putParcelable(util.KEYS.LOCATION,mLastLocation);
        }
        if(mLocationList.size()>0){
            outState.putParcelableArrayList(util.KEYS.LOCATION_LIST,
                    (ArrayList<UserLocation>)mLocationList);
        }
        String address = mMarkerAdressTextView.getText().toString().trim();
        if(!address.isEmpty()){
            outState.putString(util.KEYS.MARKER_ADDRESS,address);
        }

        if(clickedMarkerLocation != null){
            outState.putParcelable(util.KEYS.CLICKED_MARKER_LOCATION,clickedMarkerLocation);
        }

        if(clickedMarkerTitle != null){
            outState.putString(util.KEYS.CLICKED_MARKER_TITLE,clickedMarkerTitle);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        isMapReady = true;
        mGoogleMap.setOnMarkerClickListener(this);

        if(savedState != null) {
            handleSavedState(savedState);
        }
    }


    void attachLocationsValueEventListener(){

        for(Marker marker:mMarkerList){
            marker.remove();
        }
        if(currentLocMarker != null) {
            currentLocMarker.remove();
            currentLocMarker = null;
        }

        if(clickedMarkerLocation != null){
            clickedMarkerLocation = null;
        }

        if(clickedMarkerTitle != null){
            clickedMarkerTitle = null;
        }

        mMarkerAdressTextView.setText("");
        mMarkerList = new ArrayList<>();
        mLocationList = new ArrayList<>();
        mLastLocation = null;

        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG,dataSnapshot.toString());

                UserLocation userLocation = dataSnapshot.getValue(UserLocation.class);
                mLocationList.add(userLocation);
                LatLng latLng = new LatLng(userLocation.getLatitude(),userLocation.getLongitude());

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(latLng)
                        .zoom(13).tilt(33)
                        .build();
                mGoogleMap.animateCamera(CameraUpdateFactory.
                        newCameraPosition(cameraPosition));

                mMarkerList.add(mGoogleMap.addMarker(new MarkerOptions().
                        position(latLng)
                        .title(userLocation.getDateTime())));


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG,dataSnapshot.toString());
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG,dataSnapshot.toString());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        Query myMostViewedPostsQuery = myRef.child("userLocationUpdates").child(mAuth.getUid())
                .orderByChild("date").equalTo(date);

        myMostViewedPostsQuery.addChildEventListener(mChildEventListener);

    }

       void dettachLocationsValueEventListener(){
        if(mValueEventListener != null){
            myRef.removeEventListener(mValueEventListener);
            mValueEventListener = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dettachLocationsValueEventListener();
    }

    public void showDatePicker() {

        final Calendar c = Calendar.getInstance();

        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {


                        String newDayOfMonth;
                        if(dayOfMonth > 0 && dayOfMonth<10) {
                            newDayOfMonth = "0" + dayOfMonth;
                        } else{
                            newDayOfMonth = ""+dayOfMonth;
                        }

                        String newMonthOfYear;
                        if((monthOfYear+1) > 0 && (monthOfYear+1 < 10)){
                            newMonthOfYear = "0"+(monthOfYear+1);
                        } else {
                            newMonthOfYear = ""+(monthOfYear+1);
                        }


                        date = newDayOfMonth + "/" + newMonthOfYear + "/" + year;

                        if(checkInternetConnection()){
                            mDateTextView.setText(date);
                            attachLocationsValueEventListener();
                        } else {
                            hideHistoryLayout();
                        }


                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        LatLng latLng = marker.getPosition();

        Location location = new Location("provider");
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);

        clickedMarkerLocation = location;
        clickedMarkerTitle = marker.getTitle();

        if(checkInternetConnection()) {
            //String address = util.getAddressFromLocation(this, location);
            Resources res = getResources();
            String addr = res.getString(R.string.marker_address_only,
                    util.getAddressFromLocation(HistoryActivity.this
                            ,location));
            mMarkerAdressTextView.setText(addr);
        } else {
         hideHistoryLayout();
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

    void showHistoryLayout(){
        layoutHistory.setVisibility(View.VISIBLE);
        layoutError.setVisibility(View.INVISIBLE);
    }

    void hideHistoryLayout(){
        layoutHistory.setVisibility(View.INVISIBLE);
        layoutError.setVisibility(View.VISIBLE);
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
                    //getLastLocation();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, getString(R.string.enable_location_permission),
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
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

                                    mLastLocation = location;
                                    LatLng currentLocation = new LatLng(location.getLatitude(),
                                            location.getLongitude());

                                    if (isMapReady) {

                                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                                .target(currentLocation)
                                                .zoom(17).tilt(33)
                                                .build();
                                        mGoogleMap.animateCamera(CameraUpdateFactory.
                                                newCameraPosition(cameraPosition));

                                        if (currentLocMarker == null) {
                                            currentLocMarker = mGoogleMap.addMarker(new MarkerOptions().
                                                    position(currentLocation).
                                                    title(getString(R.string.marker_current)));
                                            currentLocMarker.showInfoWindow();
                                        } else {
                                            currentLocMarker.setPosition(currentLocation);
                                            currentLocMarker.showInfoWindow();
                                        }
                                    }
                                }
                            }
                        });
            } else {
                util.showLocationSettingDialog(this);
            }
        } else {
            Toast.makeText(this, getString(R.string.enable_location_permission),
                    Toast.LENGTH_SHORT).show();
        }
    }


    //saves device state on orientation change
    void handleSavedState(Bundle savedInstanceState){
        if(savedInstanceState.containsKey(util.KEYS.LOCATION_LIST)){
            mLocationList = savedInstanceState.
                    getParcelableArrayList(util.KEYS.LOCATION_LIST);
            if(isMapReady){
                for (UserLocation userLoc:mLocationList) {
                    LatLng latLng = new LatLng(userLoc.getLatitude(),
                            userLoc.getLongitude());
                    Marker marker = mGoogleMap.
                            addMarker(new MarkerOptions().
                                    position(latLng).title(userLoc.getDateTime()));
                    if(savedInstanceState.containsKey(util.KEYS.CLICKED_MARKER_TITLE)){
                        String markerTitle =
                                savedInstanceState.getString(util.KEYS.CLICKED_MARKER_TITLE);
                        if(marker.getTitle().contentEquals(markerTitle)){
                            marker.showInfoWindow();
                            clickedMarkerTitle = markerTitle;
                        }

                    }
                    mMarkerList.add(marker);
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(latLng)
                            .zoom(13).tilt(33)
                            .build();
                    mGoogleMap.animateCamera(CameraUpdateFactory.
                            newCameraPosition(cameraPosition));
                }

            }
        }
        if(savedInstanceState.containsKey(util.KEYS.LOCATION)){
            if(isMapReady) {
                mLastLocation = savedInstanceState.getParcelable(util.KEYS.LOCATION);
                LatLng latLng = new LatLng(mLastLocation.getLatitude(),
                        mLastLocation.getLongitude());
                currentLocMarker = mGoogleMap.
                        addMarker(new MarkerOptions().
                                position(latLng).title(getString(R.string.marker_current)));
                currentLocMarker.showInfoWindow();
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(latLng)
                        .zoom(13).tilt(33)
                        .build();
                mGoogleMap.animateCamera(CameraUpdateFactory.
                        newCameraPosition(cameraPosition));
            }
        }

        if(savedInstanceState.containsKey(util.KEYS.DATE)){
            String date = savedInstanceState.getString(util.KEYS.DATE);
            mDateTextView.setText(date);
        }

        if(savedInstanceState.containsKey(util.KEYS.MARKER_ADDRESS)){
            String address = savedInstanceState.getString(util.KEYS.MARKER_ADDRESS);
            mMarkerAdressTextView.setText(address);
        }

    }
}
