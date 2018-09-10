package com.niranjan.admin.nirdhast.model;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable{

    private String email;
    private String pwd;


    public User(){

    }

    public User(String email,String pwd){
        this.email = email;
        this.pwd = pwd;
    }


    public String getEmail() {
        return email;
    }

    public String getPwd() {
        return pwd;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }



    public User(Parcel in){
        this.email = in.readString();
        this.pwd = in.readString();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.email);
        dest.writeString(this.pwd);
    }


    @Override
    public String toString() {
        return this.email+"--"+this.pwd+"--";
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
