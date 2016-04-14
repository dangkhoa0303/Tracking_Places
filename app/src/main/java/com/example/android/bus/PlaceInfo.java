package com.example.android.bus;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Dell on 4/12/2016.
 */
public class PlaceInfo implements Parcelable{
    private LatLng location;
    private String name;
    private String vicinity;
    private String place_id;
    private String iconURL;

    public PlaceInfo() {

    }

    protected PlaceInfo(Parcel in) {
        location = in.readParcelable(LatLng.class.getClassLoader());
        name = in.readString();
        vicinity = in.readString();
        place_id = in.readString();
        iconURL = in.readString();
    }

    public static final Creator<PlaceInfo> CREATOR = new Creator<PlaceInfo>() {
        @Override
        public PlaceInfo createFromParcel(Parcel in) {
            return new PlaceInfo(in);
        }

        @Override
        public PlaceInfo[] newArray(int size) {
            return new PlaceInfo[size];
        }
    };

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public String getPlace_id() {
        return place_id;
    }

    public void setPlace_id(String place_id) {
        this.place_id = place_id;
    }

    public String getIconURL() {
        return iconURL;
    }

    public void setIconURL(String iconURL) {
        this.iconURL = iconURL;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(location, flags);
        dest.writeString(name);
        dest.writeString(vicinity);
        dest.writeString(place_id);
        dest.writeString(iconURL);
    }
}
