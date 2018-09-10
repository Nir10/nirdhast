package com.niranjan.admin.nirdhast;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.niranjan.admin.nirdhast.Utils.util;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = SignInActivity.class.getSimpleName();
    @BindView(R.id.tv_sign_up_title)
    TextView mSignUpTextView;
    @BindView(R.id.tv_forgot_pwd_title)
    TextView mForgotPwdTextView;

    @BindView(R.id.et_sign_in_email)
    EditText mSignInEmailEditText;
    @BindView(R.id.et_sign_in_password)
    EditText mSignInPwdEditText;

    @BindView(R.id.input_layout_sign_in_email)
    TextInputLayout mSignInEmailTextInputLayout;
    @BindView(R.id.input_layout_sign_in_password)
    TextInputLayout mSignInPwdTextInputLayout;

    @BindView(R.id.btn_sign_in)
    Button mSignInButton;

    private FirebaseAuth mAuth;

    private String mUserEmail = null;
    private String mUserPwd = null;

    AlertDialog.Builder alert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);

        if(savedInstanceState != null){
            handleSaveState(savedInstanceState);
        }
        alert = new AlertDialog.Builder(this);
        mAuth = FirebaseAuth.getInstance();

        mSignInEmailEditText.
                addTextChangedListener(new SignInActivity.MyTextWatcher(mSignInEmailEditText));
        mSignInPwdEditText.
                addTextChangedListener(new SignInActivity.MyTextWatcher(mSignInPwdEditText));


        mSignUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignInActivity.this,SignUpActivity.class));
            }
        });

        mForgotPwdTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPwdResetDialog();
            }
        });

        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateSignInEmail() && validateSignInPwd()){
                    signInUser();
                }
            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(util.KEYS.EMAIL,mSignInEmailEditText.getText().toString().trim());
        outState.putString(util.KEYS.PASSWORD,mSignInPwdEditText.getText().toString().trim());
    }

    public boolean validateSignInEmail(){
        String email = mSignInEmailEditText.getText().toString().trim();

        if(email.isEmpty()) {
            mSignInEmailTextInputLayout.setError(getString(R.string.err_msg_email));
            requestFocus(mSignInEmailEditText);
            return false;
        } else {

            if(isValidSignInEmail(email)){
                mSignInEmailTextInputLayout.setErrorEnabled(false);
                mUserEmail = email;
            }else {
                mSignInEmailTextInputLayout.setError(getString(R.string.err_msg_invalid_email));
                requestFocus(mSignInEmailEditText);
                return false;
            }

        }
        return true;
    }

    private static boolean isValidSignInEmail(String email) {

        String regEx = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(regEx);
    }


    public boolean validateSignInPwd(){
        String pwd = mSignInPwdEditText.getText().toString().trim();
        if(pwd.isEmpty()) {
            mSignInPwdTextInputLayout.setError(getString(R.string.err_msg_pwd));
            requestFocus(mSignInPwdEditText);
            return false;
        } else {

            if(isValidSignInPwd(pwd)){
                mSignInPwdTextInputLayout.setErrorEnabled(false);
                mUserPwd = pwd;
            }else {
                mSignInPwdTextInputLayout.setError(getString(R.string.err_msg_invalid_password));
                requestFocus(mSignInPwdEditText);
                return false;
            }

        }
        return true;
    }

    private static boolean isValidSignInPwd(String pwd) {

        if(pwd.trim().length() >= 6) {
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
                case R.id.et_sign_in_email: validateSignInEmail();
                    break;
                case R.id.et_sign_in_password: validateSignInPwd();
                    break;
            }
        }
    }

    void signInUser(){
        mAuth.signInWithEmailAndPassword(mUserEmail, mUserPwd)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success
                            FirebaseUser user = mAuth.getCurrentUser();

                            startActivity(new Intent(SignInActivity.this,
                                    MainActivity.class));
                            SignInActivity.this.finish();
                        } else {
                            // If sign in fails
                            task.getException().printStackTrace();
                            Toast.makeText(SignInActivity.this,
                                    task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();

                        }

                    }
                });
    }



    void showPwdResetDialog(){
        final EditText edittext = new EditText(this);
        alert.setMessage(R.string.email_hint);
        alert.setTitle(R.string.password_reset_title);

        alert.setView(edittext);

        alert.setPositiveButton(getString(R.string.password_reset_send),
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //What ever you want to do with the value
                //Editable YouEditTextValue = edittext.getText();
                //OR
                String email = edittext.getText().toString();

                if(isValidSignInEmail(email)){
                    mAuth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                        Toast.makeText(SignInActivity.this,
                                                getString(R.string.password_sent_msg),
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        task.getException().printStackTrace();
                                        Toast.makeText(SignInActivity.this,
                                                task.getException().getMessage()
                                                , Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    Toast.makeText(SignInActivity.this,
                            getString(R.string.err_msg_invalid_email)
                            , Toast.LENGTH_SHORT).show();
                }

            }
        });
        alert.show();
    }

    //handles devices state on orientation change
    void handleSaveState(Bundle savedInstanceState){
        mSignInEmailEditText.setText(savedInstanceState.getString(util.KEYS.EMAIL));
        mSignInPwdEditText.setText(savedInstanceState.getString(util.KEYS.PASSWORD));
    }
}
