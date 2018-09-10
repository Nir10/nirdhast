package com.niranjan.admin.nirdhast.model;

import android.os.Parcel;
import android.os.Parcelable;

public class UserDetails implements Parcelable{

    private String name;
    private String phone;
    private String address;
    private String bloodGroup;

    public UserDetails(){
    }

    public UserDetails(String name,String phone,String address,String bloodGroup){
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.bloodGroup = bloodGroup;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public UserDetails(Parcel in){
        this.name = in.readString();
        this.phone = in.readString();
        this.address = in.readString();
        this.bloodGroup = in.readString();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.phone);
        dest.writeString(this.address);
        dest.writeString(this.bloodGroup);
    }


    @Override
    public String toString() {
        return this.name+"--"+this.phone+"--"+this.name+"--"+this.phone+"--";
    }

    public static final Parcelable.Creator<UserDetails> CREATOR = new Parcelable.Creator<UserDetails>() {
        @Override
        public UserDetails createFromParcel(Parcel source) {
            return new UserDetails(source);
        }

        @Override
        public UserDetails[] newArray(int size) {
            return new UserDetails[size];
        }
    };
}
