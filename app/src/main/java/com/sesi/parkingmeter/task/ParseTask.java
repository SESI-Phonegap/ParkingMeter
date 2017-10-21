package com.sesi.parkingmeter.task;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.sesi.parkingmeter.fragments.HomeFragment;
import com.sesi.parkingmeter.fragments.ParkingType2Fragment;
import com.sesi.parkingmeter.utilities.DirectionsJSONParser;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Chris on 16/05/2017.
 */

class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> > {

    @Override
    protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

        JSONObject jObject;
        List<List<HashMap<String, String>>> routes = null;

        try{
            jObject = new JSONObject(jsonData[0]);
            DirectionsJSONParser parser = new DirectionsJSONParser();

            routes = parser.parse(jObject);
            Log.d("DIST-",DirectionsJSONParser.sDistance);
        }catch(Exception e){
            Log.d("Exception:",e.getMessage());
        }
        return routes;
    }

    @Override
    protected void onPostExecute(List<List<HashMap<String, String>>> result) {
        ArrayList<LatLng> points = null;
        PolylineOptions lineOptions = null;

        for(int i=0;i<result.size();i++){
            points = new ArrayList<>();
            lineOptions = new PolylineOptions();

            List<HashMap<String, String>> path = result.get(i);

            for(int j=0;j<path.size();j++){
                HashMap<String,String> point = path.get(j);

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }

            lineOptions.addAll(points);
            lineOptions.width(9);
            lineOptions.color(Color.rgb(0,0,255));
        }
        if(lineOptions!=null) {
            if (null != HomeFragment.mMap && null != HomeFragment.tvDetails) {
                HomeFragment.mMap.addPolyline(lineOptions);
                HomeFragment.tvDetails.setText(DirectionsJSONParser.sDistance);
                HomeFragment.tvDetails.append(DirectionsJSONParser.sDuration);
            }

            if (null != ParkingType2Fragment.mMap && null != ParkingType2Fragment.tvDetails) {
                ParkingType2Fragment.mMap.addPolyline(lineOptions);
                ParkingType2Fragment.tvDetails.setText(DirectionsJSONParser.sDistance);
                ParkingType2Fragment.tvDetails.append(DirectionsJSONParser.sDuration);
            }

        }
    }
}