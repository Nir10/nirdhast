package com.niranjan.admin.nirdhast.Service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.niranjan.admin.nirdhast.Utils.NetworkUtils;
import com.niranjan.admin.nirdhast.Utils.util;
import com.niranjan.admin.nirdhast.model.Caregiver;


import java.io.IOException;
import java.net.URL;

public class SendMessageIntentService extends IntentService {

    String TAG = SendMessageIntentService.class.getSimpleName();
    public String message = "";
    Caregiver caregiver1,caregiver2;

    public SendMessageIntentService() {
        super("SendMessageIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {

            message = intent.getStringExtra(util.EXTRA.SMS_MESSAGE);
            caregiver1 = intent.getParcelableExtra(util.EXTRA.SMS_CAREGIVER1);
            caregiver2 = intent.getParcelableExtra(util.EXTRA.SMS_CAREGIVER2);

            sendSMSQuery();
        }
    }



    void sendSMSQuery(){

        URL smsUrl = NetworkUtils.buildSMSURL(message,caregiver1,caregiver2);

        String response = null;
        try {
            response = NetworkUtils.getResponseFromHttpUrl(smsUrl);
            Log.i(TAG,response);
        } catch (IOException e){
            e.printStackTrace();
            response = e.getMessage();
            Log.e(TAG,response);
        }
    }
}
