package com.niranjan.admin.nirdhast.Utils;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class NirdhastApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //Enable Firebase Realtime Databse offline
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
