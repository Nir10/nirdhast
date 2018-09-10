package com.niranjan.admin.nirdhast;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.niranjan.admin.nirdhast.Service.LocationService;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.btn_liv_track)
    Button livTrackButton;
    @BindView(R.id.btn_geofence)
    Button geofenceButton;
    @BindView(R.id.btn_history)
    Button historyButton;
    @BindView(R.id.btn_caregivers)
    Button caregiversButton;
    @BindView(R.id.btn_user_details)
    Button userDetailsButton;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mAuth = FirebaseAuth.getInstance();

        livTrackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,LiveTrackActivity.class);
                startActivity(intent);
            }
        });

        geofenceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(LocationService.IS_SERVICE_RUNNING){
                    Toast.makeText(MainActivity.this,
                            getString(R.string.geofence_tracking_set), Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(MainActivity.this,
                            GeofenceActivity.class);
                    startActivity(intent);
                }
            }
        });

        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(LocationService.IS_SERVICE_RUNNING){
                    Toast.makeText(MainActivity.this,
                            getString(R.string.history_tracking_set),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                    startActivity(intent);
                }
            }
        });

        caregiversButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(LocationService.IS_SERVICE_RUNNING){
                    Toast.makeText(MainActivity.this,
                            getString(R.string.caregiver_tracking_set), Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(MainActivity.this, CareGiversActivity.class);
                    startActivity(intent);
                }
            }
        });

        userDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(LocationService.IS_SERVICE_RUNNING){
                    Toast.makeText(MainActivity.this,
                            getString(R.string.user_details_tracking_set), Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(MainActivity.this, UserDetailsActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            startActivity(new Intent(MainActivity.this,SignInActivity.class));
            this.finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.menu_main_sign_out){
            if(!LocationService.IS_SERVICE_RUNNING) {
                mAuth.signOut();
                startActivity(new Intent(MainActivity.this, SignInActivity.class));
                MainActivity.this.finish();
            } else {
                Toast.makeText(MainActivity.this,
                        getString(R.string.sign_out_tracking_set), Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }
}
