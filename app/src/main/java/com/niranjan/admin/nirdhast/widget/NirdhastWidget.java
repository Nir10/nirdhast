package com.niranjan.admin.nirdhast.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import com.google.gson.Gson;
import com.niranjan.admin.nirdhast.R;
import com.niranjan.admin.nirdhast.Utils.util;
import com.niranjan.admin.nirdhast.model.Caregiver;
import com.niranjan.admin.nirdhast.model.UserDetails;

/**
 * Implementation of App Widget functionality.
 */
public class NirdhastWidget extends AppWidgetProvider {


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context.getApplicationContext());
        ComponentName thisWidget = new ComponentName(context.getApplicationContext(),
                NirdhastWidget.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        onUpdate(context, appWidgetManager, appWidgetIds);
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        SharedPreferences prefs = context.getSharedPreferences(util.KEYS.SHARED_PREFS_ID,
                Context.MODE_PRIVATE);

            String userString = prefs.getString(util.KEYS.USER_DETAILS, null);
            String caregiver1String = prefs.getString(util.KEYS.CAREGIVER1,null);
            String caregiver2String = prefs.getString(util.KEYS.CAREGIVER2,null);

        UserDetails userDetails;
        Caregiver caregiver1,caregiver2;

        Gson gson = new Gson();


        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.nirdhast_widget);

        if(userString != null){
            userDetails = gson.fromJson(userString,UserDetails.class);
            views.setTextViewText(R.id.appwidget_name,userDetails.getName());
            views.setTextViewText(R.id.appwidget_phone,userDetails.getPhone());
            views.setTextViewText(R.id.appwidget_address,userDetails.getAddress());
            views.setTextViewText(R.id.appwidget_bloodgroup,userDetails.getBloodGroup());

        }

        if(caregiver1String != null){
            caregiver1 = gson.fromJson(caregiver1String,Caregiver.class);
            views.setTextViewText(R.id.appwidget_caregiver1_name,caregiver1.getName());
            views.setTextViewText(R.id.appwidget_caregiver1_phone,caregiver1.getPhone());
        }

        if(caregiver2String != null){
            caregiver2 = gson.fromJson(caregiver2String,Caregiver.class);
            views.setTextViewText(R.id.appwidget_caregiver2_name,caregiver2.getName());
            views.setTextViewText(R.id.appwidget_caregiver2_phone,caregiver2.getPhone());
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

