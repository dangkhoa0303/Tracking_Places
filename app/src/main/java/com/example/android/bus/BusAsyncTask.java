package com.example.android.bus;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Dell on 4/11/2016.
 */
public class BusAsyncTask extends AsyncTask<String, Void, ArrayList<BusInfo>> {
    private String radius, location_type;
    private Context context;

    public BusAsyncTask(Context context, String radius, String location_type) {
        this.context = context;
        this.radius = radius;
        this.location_type = location_type;
    }

    private ArrayList<BusInfo> extractJson(String json) {

        String RESULTS = "results";
        String GEOMETRY = "geometry";
        String LOCATION = "location";
        String NAME = "name";
        String ICON = "icon";
        String PLACE_ID = "place_id";
        String VICINITY = "vicinity";
        String LATITUDE = "lat";
        String LONGITUDE = "lng";

        ArrayList<BusInfo> listBus = new ArrayList<>();

        try {
            JSONObject result = new JSONObject(json);
            JSONArray list = result.getJSONArray(RESULTS);
            // in case AsyncTask can not be done and list is a null object
            if (list != null) {
                for (int i = 0; i < list.length(); i++) {
                    JSONObject bus_station = list.getJSONObject(i);

                    BusInfo busInfo = new BusInfo();
                    busInfo.setName(bus_station.getString(NAME));
                    busInfo.setPlace_id(bus_station.getString(PLACE_ID));
                    busInfo.setVicinity(bus_station.getString(VICINITY));
                    busInfo.setIconURL(bus_station.getString(ICON));

                    JSONObject geometry = bus_station.getJSONObject(GEOMETRY);
                    JSONObject location = geometry.getJSONObject(LOCATION);
                    LatLng latLng = new LatLng(Double.parseDouble(location.getString(LATITUDE)), Double.parseDouble(location.getString(LONGITUDE)));
                    busInfo.setLocation(latLng);

                    listBus.add(busInfo);
                }
            } else {
                // do nothing here
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return listBus;
    }

    @Override
    protected ArrayList<BusInfo> doInBackground(String... params) {

        HttpURLConnection httpURLConnection = null;
        BufferedReader bufferedReader = null;
        String json = null;

        try {

            String BASE_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
            String LOCATION = "location";
            String RADIUS = "radius";
            String TYPE = "type";
            String KEY = "key";

            Uri uri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(LOCATION, params[0])
                    .appendQueryParameter(RADIUS, radius)
                    .appendQueryParameter(TYPE, location_type)
                    .appendQueryParameter(KEY, context.getString(R.string.web_server_api_key))
                    .build();

            URL url = new URL(uri.toString());

            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();

            InputStream inputStream = httpURLConnection.getInputStream();

            if (inputStream == null) {
                return null;
            }

            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }

            json = stringBuilder.toString();
            Log.i("AsyncTask", json);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return extractJson(json);
    }

    @Override
    protected void onPostExecute(ArrayList<BusInfo> busInfos) {
        super.onPostExecute(busInfos);
        MainActivity.indicator.setVisibility(View.INVISIBLE);
    }
}
