package com.niranjan.admin.nirdhast;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.niranjan.admin.nirdhast.Utils.util;
import com.niranjan.admin.nirdhast.model.Caregiver;
import com.niranjan.admin.nirdhast.widget.NirdhastWidget;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CareGiversActivity extends AppCompatActivity {

    private final String TAG = CareGiversActivity.class.getSimpleName();
    @BindView(R.id.et_caregiver1_name)
    EditText mCaregiver1NameEditText;
    @BindView(R.id.et_caregiver1_phone)
    EditText mCaregiver1PhoneEditText;
    @BindView(R.id.et_caregiver2_name)
    EditText mCaregiver2NameEditText;
    @BindView(R.id.et_caregiver2_phone)
    EditText mCaregiver2PhoneEditText;

    @BindView(R.id.input_layout_caregiver1_name)
    TextInputLayout mCaregiver1NameTextInputLayout;
    @BindView(R.id.input_layout_caregiver1_phone)
    TextInputLayout mCaregiver1PhoneTextInputLayout;
    @BindView(R.id.input_layout_caregiver2_name)
    TextInputLayout mCaregiver2NameTextInputLayout;
    @BindView(R.id.input_layout_caregiver2_phone)
    TextInputLayout mCaregiver2PhoneTextInputLayout;

    FirebaseDatabase database;
    DatabaseReference myRef;
    private FirebaseAuth mAuth;

    ValueEventListener mValueEventListener;

    private String caregiver1Name = null;
    private String caregiver1Phone = null;
    private String caregiver2Name = null;
    private String caregiver2Phone = null;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    Bundle saveState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_care_givers);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        saveState = savedInstanceState;

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        myRef.keepSynced(true);

        mCaregiver1NameEditText.
                addTextChangedListener(new CareGiversActivity.MyTextWatcher(mCaregiver1NameEditText));
        mCaregiver1PhoneEditText.
                addTextChangedListener(new CareGiversActivity.MyTextWatcher(mCaregiver1PhoneEditText));
        mCaregiver2NameEditText.
                addTextChangedListener(new CareGiversActivity.MyTextWatcher(mCaregiver2NameEditText));
        mCaregiver2PhoneEditText.
                addTextChangedListener(new CareGiversActivity.MyTextWatcher(mCaregiver2PhoneEditText));

        if(saveState == null) {
            attachCaregiversValueEventListener();
        } else {
            handleSaveState(saveState);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(util.KEYS.CAREGIVER1_NAME,
                mCaregiver1NameEditText.getText().toString().trim());
        outState.putString(util.KEYS.CAREGIVER1_PHONE,
                mCaregiver1PhoneEditText.getText().toString().trim());
        outState.putString(util.KEYS.CAREGIVER2_NAME,
                mCaregiver2NameEditText.getText().toString().trim());
        outState.putString(util.KEYS.CAREGIVER2_PHONE,
                mCaregiver2PhoneEditText.getText().toString().trim());

    }

    public boolean validateCaregiver1Name(){
        String name = mCaregiver1NameEditText.getText().toString().trim();

        if(name.isEmpty()) {
            mCaregiver1NameTextInputLayout.setError(getString(R.string.err_msg_name));
            requestFocus(mCaregiver1NameEditText);
            return false;
        }
        mCaregiver1NameTextInputLayout.setErrorEnabled(false);
        caregiver1Name = name;
        return true;
    }


    public boolean validateCaregiver1Phone(){
        String phone = mCaregiver1PhoneEditText.getText().toString().trim();

        if(phone.isEmpty()) {
            mCaregiver1PhoneTextInputLayout.setError(getString(R.string.err_msg_phone));
            requestFocus(mCaregiver1PhoneEditText);
            return false;
        } else {

            if(isValidPhone(phone)){
                mCaregiver1PhoneTextInputLayout.setErrorEnabled(false);
                caregiver1Phone = phone;
            }else {
                mCaregiver1PhoneTextInputLayout.setError(getString(R.string.err_msg_invalid_phone));
                requestFocus(mCaregiver1PhoneEditText);
                return false;
            }

        }
        return true;
    }

    private static boolean isValidPhone(String phone) {

        String regEx = "^[0-9]{10}$";
        return phone.matches(regEx);

    }



    public boolean validateCaregiver2Name(){
        String name = mCaregiver2NameEditText.getText().toString().trim();

        if(name.isEmpty()) {
            mCaregiver2NameTextInputLayout.setError(getString(R.string.err_msg_name));
            requestFocus(mCaregiver2NameEditText);
            return false;
        }
        mCaregiver2NameTextInputLayout.setErrorEnabled(false);
        caregiver2Name = name;
        return true;
    }


    public boolean validateCaregiver2Phone(){
        String phone = mCaregiver2PhoneEditText.getText().toString().trim();

        if(phone.isEmpty()) {
            mCaregiver2PhoneTextInputLayout.setError(getString(R.string.err_msg_phone));
            requestFocus(mCaregiver2PhoneEditText);
            return false;
        } else {

            if(isValidPhone(phone)){
                mCaregiver2PhoneTextInputLayout.setErrorEnabled(false);
                caregiver2Phone = phone;
            }else {
                mCaregiver2PhoneTextInputLayout.setError(getString(R.string.err_msg_invalid_phone));
                requestFocus(mCaregiver2PhoneEditText);
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
                case R.id.et_caregiver1_name: validateCaregiver1Name();
                    break;
                case R.id.et_caregiver1_phone: validateCaregiver1Phone();
                    break;
                case R.id.et_caregiver2_name: validateCaregiver2Name();
                    break;
                case R.id.et_caregiver2_phone: validateCaregiver2Phone();
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_caregivers,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()){
            case R.id.menu_caregiver_set:
                saveCareGiverDetails();
                break;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return true;
    }

    void saveCareGiverDetails(){
        if(!validateCaregiver1Name() || !validateCaregiver1Phone()){
            return;
        }

        if(!validateCaregiver2Name() || !validateCaregiver2Phone()){
            return;
        }

        saveCareGiverToDatabase();

        if(saveState != null){
            attachCaregiversValueEventListener();
        }
    }

    //Caregiver details to Firebase Realtime Database
    void saveCareGiverToDatabase(){


            Caregiver caregiver1 = new Caregiver(caregiver1Name,caregiver1Phone);
            myRef.child("caregivers").child(mAuth.getUid()).child("caregiver1").
                    setValue(caregiver1).addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.i(TAG,"success");
                } else {
                    task.getException().printStackTrace();
                    Log.e(TAG,task.getException().getMessage());
                }
            }
        });
            Caregiver caregiver2 = new Caregiver(caregiver2Name,caregiver2Phone);

            myRef.child("caregivers").child(mAuth.getUid()).child("caregiver2")
                    .setValue(caregiver2).addOnCompleteListener(this,
                        new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.i(TAG,"success");
                } else {
                    task.getException().printStackTrace();
                    Log.e(TAG,task.getException().getMessage());
                }
            }
        });

    }


    //Add valueEvent listener to listen Firabase Realtime database changes
    void attachCaregiversValueEventListener(){

        mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG,dataSnapshot.toString());
                Caregiver caregiver1 = dataSnapshot.child("caregiver1").
                        getValue(Caregiver.class);
                sharedPref =
                        getSharedPreferences(util.KEYS.SHARED_PREFS_ID, Context.MODE_PRIVATE);
                editor = sharedPref.edit();
                if(caregiver1 != null) {
                    mCaregiver1NameEditText.setText(caregiver1.getName());
                    mCaregiver1PhoneEditText.setText(caregiver1.getPhone());
                    Gson gson = new Gson();
                    String caregiver1String = gson.toJson(caregiver1);
                    editor.putString(util.KEYS.CAREGIVER1, caregiver1String);
                }

                Caregiver caregiver2 = dataSnapshot.child("caregiver2").
                        getValue(Caregiver.class);
                if(caregiver2 != null) {
                    mCaregiver2NameEditText.setText(caregiver2.getName());
                    mCaregiver2PhoneEditText.setText(caregiver2.getPhone());
                    Gson gson = new Gson();
                        String caregiver2String = gson.toJson(caregiver2);
                        editor.putString(util.KEYS.CAREGIVER2, caregiver2String);
                }
                editor.commit();
                util.updateWidget(CareGiversActivity.this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        myRef.child("caregivers").child(mAuth.getUid())
                .addListenerForSingleValueEvent(mValueEventListener);
    }


    //Remove Value event listener from Firebase Realtime database
    void dettachCaregiversValueEventListener(){
        if(mValueEventListener != null){
            myRef.removeEventListener(mValueEventListener);
            mValueEventListener = null;

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dettachCaregiversValueEventListener();
    }

    //handles devices state on orientation change
    void handleSaveState(Bundle savedInstanceState){
        mCaregiver1NameEditText.setText(savedInstanceState.getString(util.KEYS.CAREGIVER1_NAME));
        mCaregiver1PhoneEditText.setText(savedInstanceState.getString(util.KEYS.CAREGIVER1_PHONE));
        mCaregiver2NameEditText.setText(savedInstanceState.getString(util.KEYS.CAREGIVER2_NAME));
        mCaregiver2PhoneEditText.setText(savedInstanceState.getString(util.KEYS.CAREGIVER2_PHONE));

    }
}
