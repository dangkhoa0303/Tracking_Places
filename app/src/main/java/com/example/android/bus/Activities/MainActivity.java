package com.example.android.bus.Activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bus.PlaceInfo;
import com.example.android.bus.R;
import com.example.android.bus.Service.PlacesService;
import com.example.android.bus.Util;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnMarkerClickListener {

    private String LOG_TAG = "Bus App Test";

    private GoogleMap mGoogleMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private boolean onMapReady = false;
    private boolean onLocationReady = false;

    private Location mLocation;
    private LatLng mCurrentLocation;

    private FloatingActionButton locationBtn;
    private TextView labelTextView;
    private static ProgressBar indicator;

    private static String radius;
    private static String locationType;

    private PlacesServiceReceiver receiver;

    private ArrayList<PlaceInfo> list;
    private String SAVE_LIST_KEY = "save_list";
    private String SAVE_RADIUS_KEY = "save_radius";
    private String SAVE_LOCATION_TYPE_KEY = "save_location_type";
    private String saved_radius;
    private String saved_locationType;

    // save list, radius and locationType whenever onPause() is called
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVE_RADIUS_KEY, radius);
        outState.putString(SAVE_LOCATION_TYPE_KEY, locationType);
        outState.putParcelableArrayList(SAVE_LIST_KEY, list);

        // when settingsActivity comes back to this activity, it jumps straight ahead to onResume instead of onCreate
        // therefore, the values of saved_radius and saved_locationType stay unchanged and are not used correctly in updating UI
        // we need store like this in order to change the values of these two
        saved_radius = radius;
        saved_locationType = locationType;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // load the sotred values into list, saved_radius and saved_locationType
        if (savedInstanceState != null) {
            list = savedInstanceState.getParcelableArrayList(SAVE_LIST_KEY);
            saved_radius = savedInstanceState.getString(SAVE_RADIUS_KEY);
            saved_locationType = savedInstanceState.getString(SAVE_LOCATION_TYPE_KEY);
        } else {
            list = new ArrayList<>();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // register for receiving intent from IntentService by using IntentFilter and BroadcastReceiver
        IntentFilter intentFilter = new IntentFilter(PlacesServiceReceiver.PLACE_LIST_RESPONSE);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new PlacesServiceReceiver(this);
        registerReceiver(receiver, intentFilter);

        // generate mGoogleApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // progressBar
        indicator = (ProgressBar) findViewById(R.id.progressBar);
        indicator.setVisibility(View.INVISIBLE);

        // label textview
        labelTextView = (TextView) findViewById(R.id.placeLabel);

        // on Button click
        locationBtn = (FloatingActionButton) findViewById(R.id.locationBtn);
        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLocationRequest = LocationRequest.create();
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                if (onLocationReady) {
                    mCurrentLocation = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
                    Log.i("OnButtonPressed", Util.LocationString(mCurrentLocation));
                    Util.flyTo(Util.setCameraPosition(mCurrentLocation), mGoogleMap);
                } else {
                    Toast.makeText(getApplicationContext(), "No location", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // generate map fragment
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id) {
            case R.id.satellite:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.hybrid:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.terrain:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.action_settings:
                Intent i = new Intent(this, Settings.class);
                startActivity(i);
                break;
            default:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }

    @Override
    protected void onDestroy() {
        // when this activity is destroyed, unregister for receiving intent
        this.unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (mLocation == null) {
            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("No permission for accessing location !!!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(getApplicationContext(), "Something happened in connection !", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        onLocationReady = true;
        mLocation = location;

        //LatLng destination = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        mCurrentLocation = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        radius = Util.getRadius(this);
        locationType = Util.getLocationType(this);

        labelTextView.setText(Util.getLocationTypeLabel(this));
        SetCurrentLocation();

        // check if list is not null, and radius and locationType are unchanged, then just simple addPlaceMarkers without calling IntentService
        if (list != null && (saved_radius != null && saved_radius.equals(radius)) && (saved_locationType != null && saved_locationType.equals(locationType))) {
            Util.AddPlaceMarker(getApplicationContext(), list, mGoogleMap);
        } else {
            // otherwise, call IntentService to load new Places and add new Markers
            RequestList();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(), "Failed to connect to the Internet !", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        onMapReady = true;
        mGoogleMap = googleMap;
        mGoogleMap.setOnMarkerClickListener(this);

        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (locationBtn.getVisibility() == View.VISIBLE && labelTextView.getVisibility() == View.VISIBLE) {
                    locationBtn.setVisibility(View.INVISIBLE);
                    labelTextView.setVisibility(View.INVISIBLE);
                } else {
                    locationBtn.setVisibility(View.VISIBLE);
                    labelTextView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return false;
    }

    private void RequestList() {
        if (Util.checkInternetConnection(getApplicationContext())) {
            indicator.setVisibility(View.VISIBLE);
            Intent intent = new Intent(this, PlacesService.class);
            intent.putExtra(PlacesService.CURRENT_LOCATION, Util.LocationString(mCurrentLocation));
            intent.putExtra(PlacesService.CURRENT_RADIUS, radius);
            intent.putExtra(PlacesService.LOCATION_TYPE, locationType);
            startService(intent);
        } else {
            Toast.makeText(getApplicationContext(), "No internet connection!", Toast.LENGTH_SHORT).show();
        }
    }

    private void SetCurrentLocation() {
        if (onMapReady) {
            Util.flyTo(Util.setCameraPosition(mCurrentLocation), mGoogleMap);
            mGoogleMap.clear();
            Util.AddCurrentLocationMarker(mCurrentLocation, mGoogleMap);
            Util.drawCircle(mCurrentLocation, mGoogleMap, radius);
        }
    }

    /*
    Declare an inner class of Activity that will receive intent from IntentService
    This class extends from BroadcastReceiver
     */
    public class PlacesServiceReceiver extends BroadcastReceiver {

        /* This String is used as a filter for MainActivity to receive the result from IntentService
        It can be anything we like. However, in conventional way, it is better to use the package name as String filter
         */

        public static final String PLACE_LIST_RESPONSE = "com.example.android.bus.intent.action.PLACE_LIST_RESPONSE";
        private Context context;

        public PlacesServiceReceiver(Context context) {
            this.context = context;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            list = intent.getParcelableArrayListExtra(PlacesService.LIST_RESPONSE);
            // update main UI
            Util.AddPlaceMarker(context, list, mGoogleMap);
            Toast.makeText(context, "" + list.size() + " " + labelTextView.getText().toString() + "(s)", Toast.LENGTH_SHORT).show();
            MainActivity.indicator.setVisibility(View.INVISIBLE);
        }
    }
}
