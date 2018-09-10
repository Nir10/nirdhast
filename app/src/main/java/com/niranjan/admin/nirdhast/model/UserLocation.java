package com.niranjan.admin.nirdhast.model;

import android.os.Parcel;
import android.os.Parcelable;

public class UserLocation implements Parcelable{
    
    private double latitude;
    private double longitude;
    private String dateTime;
    private String date;

    public UserLocation(){

    }

    public UserLocation(double latitude,double longitude,String dateTime,String date){
        this.latitude = latitude;
        this.longitude = longitude;
        this.dateTime = dateTime;
        this.date = date;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getDate() {
        return date;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public UserLocation(Parcel in){
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.dateTime = in.readString();
        this.date = in.readString();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
        dest.writeString(this.dateTime);
        dest.writeString(this.date);
    }


    @Override
    public String toString() {
        return this.latitude+"--"+this.longitude+"--"+this.dateTime;
    }

    public static final Parcelable.Creator<UserLocation> CREATOR =
            new Parcelable.Creator<UserLocation>() {
        @Override
        public UserLocation createFromParcel(Parcel source) {
            return new UserLocation(source);
        }

        @Override
        public UserLocation[] newArray(int size) {
            return new UserLocation[size];
        }
    };
}
