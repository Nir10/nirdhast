package com.niranjan.admin.nirdhast.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Caregiver implements Parcelable{
    private String name;
    private String phone;

    public Caregiver(){

    }
    public Caregiver(String name,String phone){
        this.name = name;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }


    public Caregiver(Parcel in){
        this.name = in.readString();
        this.phone = in.readString();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.phone);
    }


    @Override
    public String toString() {
        return this.name+"--"+this.phone+"--";
    }

    public static final Parcelable.Creator<Caregiver> CREATOR = new Parcelable.Creator<Caregiver>() {
        @Override
        public Caregiver createFromParcel(Parcel source) {
            return new Caregiver(source);
        }

        @Override
        public Caregiver[] newArray(int size) {
            return new Caregiver[size];
        }
    };
}


