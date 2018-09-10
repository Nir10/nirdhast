package com.niranjan.admin.nirdhast;

import android.content.Context;
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
import com.niranjan.admin.nirdhast.model.UserDetails;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserDetailsActivity extends AppCompatActivity {

    String TAG = UserDetailsActivity.class.getSimpleName();
    @BindView(R.id.et_user_name)
    EditText mUserNameEditText;
    @BindView(R.id.et_user_phone)
    EditText mUserPhoneEditText;
    @BindView(R.id.et_user_address)
    EditText mUserAddressEditText;
    @BindView(R.id.et_user_bloodgroup)
    EditText mUserBloodGroupEditText;

    @BindView(R.id.input_layout_user_name)
    TextInputLayout mUserNameTextInputLayout;
    @BindView(R.id.input_layout_user_phone)
    TextInputLayout mUserPhoneTextInputLayout;
    @BindView(R.id.input_layout_user_address)
    TextInputLayout mUserAddressTextInputLayout;
    @BindView(R.id.input_layout_user_bloodgroup)
    TextInputLayout mUserBloodGroupTextInputLayout;

    private String userName = null;
    private String userPhone = null;
    private String userAddress = null;
    private String userBloodgroup = null;

    FirebaseDatabase database;
    DatabaseReference myRef;
    private FirebaseAuth mAuth;
    ValueEventListener mValueEventListener;
    UserDetails userDetails;
    Bundle saveState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);
        saveState = savedInstanceState;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        myRef.keepSynced(true);

        mUserNameEditText.
                addTextChangedListener(new UserDetailsActivity.MyTextWatcher(mUserNameEditText));
        mUserPhoneEditText.
                addTextChangedListener(new UserDetailsActivity.MyTextWatcher(mUserPhoneEditText));
        mUserAddressEditText.
                addTextChangedListener(new UserDetailsActivity.MyTextWatcher(mUserAddressEditText));
        mUserBloodGroupEditText.
                addTextChangedListener(new UserDetailsActivity.MyTextWatcher(mUserBloodGroupEditText));

        if(saveState == null){
        attachUserDetailsEventListener();
        } else {
           handleSaveState(saveState) ;
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(util.KEYS.USER_NAME,mUserNameEditText.getText().toString().trim());
        outState.putString(util.KEYS.USER_PHONE,mUserPhoneEditText.getText().toString().trim());
        outState.putString(util.KEYS.USER_ADDRESS,mUserAddressEditText.getText().toString().trim());
        outState.putString(util.KEYS.USER_BLOODROUP,mUserBloodGroupEditText.getText().toString().trim());


    }

    public boolean validateName(){
        String name = mUserNameEditText.getText().toString().trim();

        if(name.isEmpty()) {
            mUserNameTextInputLayout.setError(getString(R.string.err_msg_name));
            requestFocus(mUserNameEditText);
            return false;
        }
        mUserNameTextInputLayout.setErrorEnabled(false);
        userName = name;
        return true;
    }

    public boolean validatePhone(){
        String phone = mUserPhoneEditText.getText().toString().trim();

        if(phone.isEmpty()) {
            mUserPhoneTextInputLayout.setError(getString(R.string.err_msg_phone));
            requestFocus(mUserPhoneEditText);
            return false;
        } else {

            if(isValidPhone(phone)){
                mUserPhoneTextInputLayout.setErrorEnabled(false);
                userPhone = phone;
            }else {
                mUserPhoneTextInputLayout.setError(getString(R.string.err_msg_invalid_phone));
                requestFocus(mUserPhoneEditText);
                return false;
            }

        }
        return true;
    }

    private static boolean isValidPhone(String phone) {
        String regEx = "^[0-9]{10}$";
        return phone.matches(regEx);
    }


    public boolean validateAddress(){
        String address = mUserAddressEditText.getText().toString().trim();
        if(address.isEmpty()) {
            mUserAddressTextInputLayout.setError(getString(R.string.err_msg_address));
            requestFocus(mUserAddressEditText);
            return false;
        }
        mUserAddressTextInputLayout.setErrorEnabled(false);
        userAddress = address;
        return true;
    }


    public boolean validateBloodGroup(){
        String bloodgroup = mUserBloodGroupEditText.getText().toString().trim();
        if(bloodgroup.isEmpty()) {
            mUserBloodGroupTextInputLayout.setError(getString(R.string.err_msg_bloodgroup));
            requestFocus(mUserBloodGroupEditText);
            return false;
        } else {

            if(isValidBloodGroup(bloodgroup)){
                mUserBloodGroupTextInputLayout.setErrorEnabled(false);
                userBloodgroup = bloodgroup;
            }else {
                mUserBloodGroupTextInputLayout.setError(getString(R.string.err_msg_invalid_bloodgroup));
                requestFocus(mUserBloodGroupEditText);
                return false;
            }

        }
        return true;
    }

    private static boolean isValidBloodGroup(String bloodgroup) {

        String regEx = "^(A|B|AB|O)[+-]?$";
        return bloodgroup.toUpperCase().matches(regEx);
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
                case R.id.et_user_name: validateName();
                    break;
                case R.id.et_user_phone: validatePhone();
                    break;
                case R.id.et_user_address: validateAddress();
                    break;
                case R.id.et_user_bloodgroup: validateBloodGroup();
                    break;
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_details,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()){
            case R.id.menu_user_details_set:
                saveUserDetails();
                break;
            case android.R.id.home:
                onBackPressed();
               return true;
        }
        return true;
    }

    //saves user details to Firebase Database
    void saveUserDetails(){

        if(!validateName() || !validatePhone() || !validateAddress() || !validateBloodGroup()){
            return;
        }

        userDetails =new UserDetails(userName,userPhone,userAddress,userBloodgroup);

        myRef.child("userDetails").child(mAuth.getUid()).setValue(userDetails)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
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

        if(saveState != null) {
            attachUserDetailsEventListener();
        }
    }

    //attaches a Child Listener that will detect Firebase databse changes
    void attachUserDetailsEventListener(){

        mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserDetails userDetails = dataSnapshot.
                        getValue(UserDetails.class);
                if(userDetails != null) {
                    mUserNameEditText.setText(userDetails.getName());
                    mUserPhoneEditText.setText(userDetails.getPhone());
                    mUserAddressEditText.setText(userDetails.getAddress());
                    mUserBloodGroupEditText.setText(userDetails.getBloodGroup());

                    SharedPreferences sharedPref =
                            getSharedPreferences(util.KEYS.SHARED_PREFS_ID, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    Gson gson = new Gson();
                    String userDetailsString = gson.toJson(userDetails);
                    editor.putString(util.KEYS.USER_DETAILS, userDetailsString);
                    editor.commit();

                    //notifies widget to update User Details
                    util.updateWidget(UserDetailsActivity.this);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        myRef.child("userDetails").child(mAuth.getUid()).
                addListenerForSingleValueEvent(mValueEventListener);

    }

    //Removes child event listner from database
    void dettachUserDetailsValueEventListener(){
        if(mValueEventListener != null){
            myRef.removeEventListener(mValueEventListener);
            mValueEventListener = null;

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dettachUserDetailsValueEventListener();
    }

    //handles devices state on orientation change
    void handleSaveState(Bundle savedInstanceState){
        mUserNameEditText.setText(savedInstanceState.getString(util.KEYS.USER_NAME));
        mUserPhoneEditText.setText(savedInstanceState.getString(util.KEYS.USER_PHONE));
        mUserAddressEditText.setText(savedInstanceState.getString(util.KEYS.USER_ADDRESS));
        mUserBloodGroupEditText.setText(savedInstanceState.getString(util.KEYS.USER_BLOODROUP));

    }
}
