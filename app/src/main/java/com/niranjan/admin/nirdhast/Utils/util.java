package com.niranjan.admin.nirdhast.Utils;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;

import com.niranjan.admin.nirdhast.R;
import com.niranjan.admin.nirdhast.widget.NirdhastWidget;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class util {

    static SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    //static SimpleDateFormat dateFormatDB = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

 public interface ACTION {

     String STARTFOREGROUND_ACTION =
          "com.niranjan.admin.nirdhast.action.startforeground";
     String STOPFOREGROUND_ACTION = "com.niranjan.admin.nirdhast.action.stopforeground";
     String MAIN_ACTION = "com.niranjan.admin.nirdhast.action.main";
     String LOCATION_BROADCAST_ACTION =
          "com.niranjan.admin.nirdhast.action.location_broadcast";
     String GEOFENCE_BROADCAST_ACTION =
             "com.niranjan.admin.nirdhast.action.geofence_broadcast";
 }

 public interface NOTIFICATION_ID {
   int FOREGROUND_SERVICE = 101;
 }

 public interface EXTRA{
    String LOCATION = "com.niranjan.admin.nirdhast.extra.location";
    String GEOFENCE = "com.niranjan.admin.nirdhast.extra.HomeGeofence";
    String GEOFENCE_HOME = "com.niranjan.admin.nirdhast.extra.GeofenceHome";
    String GEOFENCE_RADIUS = "com.niranjan.admin.nirdhast.extra.GeofenceRadius";
    String GEOFENCE_ADD = "com.niranjan.admin.nirdhast.extra.GeofenceAdd";
    String GEOFENCE_SUCCESS = "com.niranjan.admin.nirdhast.extra.success";
    String GEOFENCE_MESSAGE = "com.niranjan.admin.nirdhast.extra.message";

    String SMS_MESSAGE = "com.niranjan.admin.nirdhast.extra.sms_message";
    String SMS_CAREGIVER1 = "com.niranjan.admin.nirdhast.extra.sms_caregiver1";
    String SMS_CAREGIVER2 = "com.niranjan.admin.nirdhast.extra.sms_caregiver2";

 }

 public interface KEYS{
     String SHARED_PREFS_ID = "com.niranjan.admin.nirdhast.keys.sharedPrefs";
     String GEOFENCE = "com.niranjan.admin.nirdhast.keys.geofence";
     String CAREGIVER1 = "com.niranjan.admin.nirdhast.keys.caregiver1";
     String CAREGIVER2 = "com.niranjan.admin.nirdhast.keys.caregiver2";
     String USER_DETAILS = "com.niranjan.admin.nirdhast.keys.userDetails";

     String LOCATION = "com.niranjan.admin.nirdhast.keys.location";
     String LOCATION_LIST = "com.niranjan.admin.nirdhast.keys.location_list";
     String HOME_LOCATION = "com.niranjan.admin.nirdhast.keys.home_location";
     String CURRENT_LOCATION = "com.niranjan.admin.nirdhast.keys.current_location";
     String CLICKED_MARKER_LOCATION = "com.niranjan.admin.nirdhast.keys.clicked_marker_location";
     String CLICKED_MARKER_TITLE = "com.niranjan.admin.nirdhast.keys.clicked_marker_title";
     String DATE = "com.niranjan.admin.nirdhast.keys.date";
     String MARKER_ADDRESS = "com.niranjan.admin.nirdhast.keys.marker_address";
     String RADIUS = "com.niranjan.admin.nirdhast.keys.radius";
     String CAREGIVER1_NAME = "com.niranjan.admin.nirdhast.keys.caregiver1_name";
     String CAREGIVER2_NAME = "com.niranjan.admin.nirdhast.keys.caregiver2_name";
     String CAREGIVER1_PHONE = "com.niranjan.admin.nirdhast.keys.caregiver1_phone";
     String CAREGIVER2_PHONE = "com.niranjan.admin.nirdhast.keys.caregiver2_phone";
     String USER_NAME = "com.niranjan.admin.nirdhast.keys.user_name";
     String USER_PHONE = "com.niranjan.admin.nirdhast.keys.user_phone";
     String USER_ADDRESS = "com.niranjan.admin.nirdhast.keys.user_address";
     String USER_BLOODROUP = "com.niranjan.admin.nirdhast.keys.user_bloodgroup";
     String EMAIL = "com.niranjan.admin.nirdhast.keys.email";
     String PASSWORD = "com.niranjan.admin.nirdhast.keys.password";
     String RE_PASSWORD = "com.niranjan.admin.nirdhast.keys.re_password";
 }



    public static String getAddressFromLocation(Context context, Location location){
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(context, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            String address = addresses.get(0).getAddressLine(0);
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();
            return address;
        } catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public static String getLocationTitle(Context context) {
        return context.getString(R.string.location_updated,
                DateFormat.getDateTimeInstance().format(new Date()));
    }

    public static String getLocationText(Location location,Context context) {
        return location == null ? context.getString(R.string.unknown_location) :
                "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
    }

    public static String getFormatedCurrentDate() {
        Date date = new Date();
        return dateFormat.format(date);
    }


    //checks whether GPS is enabled
    public static boolean isLocationServiceEnabled(Context context){
        LocationManager locationManager = null;
        boolean gps_enabled= false;

        if(locationManager ==null)
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        try{
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }catch(Exception ex){
            //do nothing...
        }

        return gps_enabled;

    }

    //display Location Settings Dialog to user
    public static void showLocationSettingDialog(final Context context){
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(context.getResources().getString(R.string.gps_network_not_enabled));
        dialog.setPositiveButton(context.getResources().getString(R.string.open_location_settings),
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(myIntent);
            }
        });
        dialog.setNegativeButton(context.getString(R.string.Cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                // TODO Auto-generated method stub

            }
        });
        dialog.show();
    }

    //notifies widget to update its data
    public static void updateWidget(Context context){
        Intent intent = new Intent(context, NirdhastWidget.class);
        int ids[] = AppWidgetManager.getInstance(context).
                getAppWidgetIds(new ComponentName(context, NirdhastWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
        context.sendBroadcast(intent);
    }

}
