package com.niranjan.admin.nirdhast.Utils;

import com.niranjan.admin.nirdhast.model.Caregiver;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class NetworkUtils {
    final public static String
            MSG_91_BASE_URL = "http://api.msg91.com/api/sendhttp.php?";

    static String MOBILES = "";
    static String MESSAGE = "";

    /**
     * Builds the URL used to talk to the Msg91 server using API key.
     * The URL tells Msg91 server to send sms to mobile numbers.
     * @return The URL to use to query the Msg91 server.
     */
    public static URL buildSMSURL(String message,Caregiver caregiver1,Caregiver caregiver2){

        MESSAGE = message;
        MOBILES = "91"+caregiver1.getPhone()+","+"91"+caregiver2.getPhone();

        String urlString =MSG_91_BASE_URL+
                "sender=NIRIND"+
                "&route=4"+
                "&mobiles="+MOBILES+
                "&authkey=MSG_91 API KEY"+
                "&country=0"+
                "&message="+message;

        URL url = null;

        try{
            url = new URL(urlString);
        } catch(MalformedURLException e){
            e.printStackTrace();
        }
        return url;
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if(hasInput){
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    //checks whether internet connection is available
    public static boolean isConnected() throws InterruptedException, IOException {
        String command = "ping -c 1 google.com";
        return (Runtime.getRuntime().exec (command).waitFor() == 0);
    }
}
