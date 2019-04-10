package com.android.achat.DataModels;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Gaurav on 10-09-2017.
 */

public class User implements Parcelable{

    private String name;
    private String image;
    private String email;
    private String uid=null;
    private String status;
    private String thumbnail;

    public User(){

    }

    public User(String name, String image, String status) {
        this.name = name;
        this.image = image;
        this.status = status;
    }




    public User(String uid,String email, String name, String image, String status) {
        this.uid=uid;
        this.email=email;
        this.name = name;
        this.image = image;
        this.status = status;
    }

    public User(Parcel in){
        String[] data = new String[6];

        in.readStringArray(data);
        this.uid=data[0];
        this.email=data[1];
        this.name =data[2];
        this.image = data[3];
        this.status = data[4];
        this.thumbnail=data[5];
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringArray(new String[]{this.uid,this.email,this.name,this.image,this.status,this.thumbnail});
    }

   public static final Parcelable.Creator<User> CREATOR = new Creator<User>() {
       @Override
       public User createFromParcel(Parcel parcel) {
           return new User(parcel);
       }

       @Override
       public User[] newArray(int i) {
           return new User[i];
       }
   };

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }


}
