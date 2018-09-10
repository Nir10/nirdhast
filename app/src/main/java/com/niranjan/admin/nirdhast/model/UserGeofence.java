package com.niranjan.admin.nirdhast.model;

import android.os.Parcel;
import android.os.Parcelable;

public class UserGeofence implements Parcelable {

    private double latitude;
    private double longitude;
    private float radius;

    public UserGeofence(){

    }
    public UserGeofence(double latitude,double longitude,float radius){
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public float getRadius() {
        return radius;
    }


    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }


    public UserGeofence(Parcel in){
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.radius = in.readFloat();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
        dest.writeFloat(this.radius);
    }


    @Override
    public String toString() {
        return this.latitude+"--"+this.longitude+"--"+this.radius;
    }

    public static final Creator<UserGeofence> CREATOR = new Creator<UserGeofence>() {
        @Override
        public UserGeofence createFromParcel(Parcel source) {
            return new UserGeofence(source);
        }

        @Override
        public UserGeofence[] newArray(int size) {
            return new UserGeofence[size];
        }
    };
}
