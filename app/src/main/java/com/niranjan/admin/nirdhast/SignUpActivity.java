package com.niranjan.admin.nirdhast;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.niranjan.admin.nirdhast.Utils.util;
import com.niranjan.admin.nirdhast.model.User;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SignUpActivity extends AppCompatActivity {

    private static String TAG = SignUpActivity.class.getSimpleName();

    @BindView(R.id.et_sign_up_email)
    EditText mSignUpEmailEditText;
    @BindView(R.id.et_sign_up_password)
    EditText mSignUpPwdEditText;
    @BindView(R.id.et_sign_up_re_password)
    EditText mSignUpRePwdEditText;

    @BindView(R.id.input_layout_sign_up_email)
    TextInputLayout mSignUpEmailTextInputLayout;
    @BindView(R.id.input_layout_sign_up_password)
    TextInputLayout mSignUpPwdTextInputLayout;
    @BindView(R.id.input_layout_sign_up_re_password)
    TextInputLayout mSignUpRePwdTextInputLayout;

    @BindView(R.id.btn_sign_up)
    Button mSignUpButton;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRefs;

    private String mUserEmail = null;
    private String mUserPwd = null;
    private String mUserRePwd = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mRefs = mDatabase.getReference("users");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSignUpEmailEditText.
                addTextChangedListener(new SignUpActivity.MyTextWatcher(mSignUpEmailEditText));
        mSignUpPwdEditText.
                addTextChangedListener(new SignUpActivity.MyTextWatcher(mSignUpPwdEditText));
        mSignUpRePwdEditText.
                addTextChangedListener(new SignUpActivity.MyTextWatcher(mSignUpRePwdEditText));


        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(validateSignUpEmail() &&  validateSignUpPwd()&& validateSignUpRePwd()){
                    signUpUser();
                }
            }
        });

        if(savedInstanceState != null){
            handleSaveState(savedInstanceState);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(util.KEYS.EMAIL,mSignUpEmailEditText.getText().toString().trim());
        outState.putString(util.KEYS.PASSWORD,mSignUpPwdEditText.getText().toString().trim());
        outState.putString(util.KEYS.RE_PASSWORD,mSignUpRePwdEditText.getText().toString().trim());
    }

    public boolean validateSignUpEmail(){
        String email = mSignUpEmailEditText.getText().toString().trim();

        if(email.isEmpty()) {
            mSignUpEmailTextInputLayout.setError(getString(R.string.err_msg_email));
            requestFocus(mSignUpEmailEditText);
            return false;
        } else {

            if(isValidSignUpEmail(email)){
                mSignUpEmailTextInputLayout.setErrorEnabled(false);
                mUserEmail = email;
            }else {
                mSignUpEmailTextInputLayout.setError(getString(R.string.err_msg_invalid_name));
                requestFocus(mSignUpEmailEditText);
                return false;
            }

        }
        return true;
    }

    private static boolean isValidSignUpEmail(String email) {

        String regEx = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(regEx);
    }


    public boolean validateSignUpPwd(){
        String pwd = mSignUpPwdEditText.getText().toString().trim();

        if(pwd.isEmpty()) {
            mSignUpPwdTextInputLayout.setError(getString(R.string.err_msg_pwd));
            requestFocus(mSignUpPwdEditText);
            return false;
        } else {

            if(isValidSignUpPwd(pwd)){
                mSignUpPwdTextInputLayout.setErrorEnabled(false);
                mUserPwd = pwd;
            }else {
                mSignUpPwdTextInputLayout.setError(getString(R.string.err_msg_invalid_password));
                requestFocus(mSignUpPwdEditText);
                return false;
            }

        }
        return true;
    }

    private static boolean isValidSignUpPwd(String pwd) {

        if(pwd.trim().length() >= 6){
            return true;
        }
        return false;
    }


    public boolean validateSignUpRePwd(){
        String pwd = mSignUpRePwdEditText.getText().toString().trim();
        if(pwd.isEmpty()) {
            mSignUpRePwdTextInputLayout.setError(getString(R.string.err_msg_pwd));
            requestFocus(mSignUpRePwdEditText);
            return false;
        } else {

            if(isValidSignUpRePwd(pwd,mUserPwd)){
                mSignUpRePwdTextInputLayout.setErrorEnabled(false);
                mUserRePwd = pwd;
            }else {
                mSignUpRePwdTextInputLayout.setError(getString(R.string.err_msg_invalid_re_password));
                requestFocus(mSignUpRePwdEditText);
                return false;
            }

        }
        return true;
    }

    private static boolean isValidSignUpRePwd(String repwd,String pwd) {

        if (repwd.contentEquals(pwd)) {
            return true;
        } else {
            return false;
        }
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
                case R.id.et_sign_up_email: validateSignUpEmail();
                    break;
                case R.id.et_sign_up_password: validateSignUpPwd();
                    break;
                case R.id.et_sign_up_re_password: validateSignUpRePwd();
                    break;
            }
        }
    }


    //create with firebase authentication
    void signUpUser() {
        mAuth.createUserWithEmailAndPassword(mUserEmail, mUserPwd)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success
                            FirebaseUser user = mAuth.getCurrentUser();

                            saveUserToDatabase(user);

                            startActivity(new Intent(SignUpActivity.this,
                                    MainActivity.class));
                            SignUpActivity.this.finish();
                        } else {
                            // sign in fails
                            task.getException().printStackTrace();
                            Toast.makeText(SignUpActivity.this,
                                    task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();

                        }
                        // ...
                    }
                });
    }

    //save new created user credential in Firebase realtime Database
    void saveUserToDatabase(FirebaseUser firebaseUser){
        User user = new User(mUserEmail,mUserPwd);
        mRefs.child(firebaseUser.getUid()).
                setValue(user).addOnCompleteListener(this,
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

    //handles devices state on orientation change
    void handleSaveState(Bundle savedInstanceState){
        mSignUpEmailEditText.setText(savedInstanceState.getString(util.KEYS.EMAIL));
        mSignUpPwdEditText.setText(savedInstanceState.getString(util.KEYS.PASSWORD));
        mSignUpRePwdEditText.setText(savedInstanceState.getString(util.KEYS.RE_PASSWORD));
    }
}
