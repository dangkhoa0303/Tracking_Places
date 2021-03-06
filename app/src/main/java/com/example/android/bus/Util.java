package com.example.android.bus;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

/**
 * Created by Dell on 4/12/2016.
 */
public class Util {

    public static void flyTo(CameraPosition target, GoogleMap mGoogleMap) {
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(target), 2000, null);
    }

    public static CameraPosition setCameraPosition(LatLng location) {
        CameraPosition target = CameraPosition.builder().target(location)
                .tilt(30)
                .zoom((float) 16)
                .build();
        return target;
    }

    public static void drawCircle(LatLng target, GoogleMap map, String r) {
        if (r == null || r == "") {
            r = "500";
        }
        map.addCircle(new CircleOptions()
                .center(target)
                .radius(Double.parseDouble(r))
                .strokeColor(Color.MAGENTA)
                .strokeWidth(5)
                .fillColor(Color.argb(70, 255, 255, 153))
        );
    }

    public static void AddCurrentLocationMarker(LatLng target, GoogleMap mGoogleMap) {
        MarkerOptions markerOptions = new MarkerOptions()
                .position(target)
                .title("My current location")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_marker));
        mGoogleMap.addMarker(markerOptions);
    }

    public static void AddPlaceMarker(Context context, ArrayList<PlaceInfo> list, GoogleMap map) {
        for (int i = 0; i < list.size(); i++) {
            MarkerOptions marker = new MarkerOptions();
            marker.position(list.get(i).getLocation())
                    .title(list.get(i).getName())
                    .icon(BitmapDescriptorFactory.fromResource(getMarkerIcon(context)));
            map.addMarker(marker);
        }
    }

    public static String LocationString(LatLng latLng) {
        return String.valueOf(latLng.latitude) + "," + String.valueOf(latLng.longitude);
    }

    public static boolean checkInternetConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public static void resetRadius(Context context, String key, String value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor spe = sp.edit();
        spe.putString(key, value);
        spe.apply();
    }

    public static String getRadius(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        //return sp.getString(context.getString(R.string.pref_radius_key), "");
        return sp.getString(context.getString(R.string.pref_radius_key), context.getString(R.string.radius_500_m));
    }

    public static void resetLocationType(Context context, String key, String value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor spe = sp.edit();
        spe.putString(key, value);
        spe.apply();
    }

    public static String getLocationType(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        //return sp.getString(context.getString(R.string.pref_location_type_key), "");
        return sp.getString(context.getString(R.string.pref_location_type_key), context.getString(R.string.place_bus_station));
    }

    public static String getLocationTypeLabel(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String value = sp.getString(context.getString(R.string.pref_location_type_key), context.getString(R.string.place_bus_station));
        String label;
        switch (value) {
            case "bus_station":
                label = "Bus station";
                break;
            case "bank":
                label = "Bank";
                break;
            case "atm":
                label = "ATM";
                break;
            case "cafe":
                label = "Cafe";
                break;
            case "restaurant":
                label = "Restaurant";
                break;
            case "movie_theater":
                label = "Movie theater";
                break;
            default:
                label = "";
                break;
        }
        return label;
    }

    public static int getMarkerIcon(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String value = sp.getString(context.getString(R.string.pref_location_type_key), context.getString(R.string.place_bus_station));
        int icon;
        switch (value) {
            case "bus_station":
                icon = R.drawable.ic_maps_directions_bus;
                break;
            case "bank":
                icon = R.drawable.ic_editor_attach_money;
                break;
            case "atm":
                icon = R.drawable.ic_maps_local_atm;
                break;
            case "cafe":
                icon = R.drawable.ic_maps_local_bar;
                break;
            case "restaurant":
                icon = R.drawable.ic_maps_local_restaurant;
                break;
            case "movie_theater":
                icon = R.drawable.ic_maps_local_movies;
                break;
            default:
                icon = R.drawable.ic_maps_directions_bus;
                break;
        }
        return icon;
    }

}
