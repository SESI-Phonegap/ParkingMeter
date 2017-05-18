package com.sesi.parkingmeter.task;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.sesi.parkingmeter.fragments.MapFragment;
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
        List<List<HashMap<String, String>>> distance = null;

        try{
            jObject = new JSONObject(jsonData[0]);
            DirectionsJSONParser parser = new DirectionsJSONParser();

            routes = parser.parse(jObject);
            Log.d("DIST-",DirectionsJSONParser.sDistance);
        }catch(Exception e){
            e.printStackTrace();
        }
        return routes;
    }

    @Override
    protected void onPostExecute(List<List<HashMap<String, String>>> result) {
        ArrayList<LatLng> points = null;
        PolylineOptions lineOptions = null;
        MarkerOptions markerOptions = new MarkerOptions();

        for(int i=0;i<result.size();i++){
            points = new ArrayList<LatLng>();
            lineOptions = new PolylineOptions();

            List<HashMap<String, String>> path = result.get(i);

            for(int j=0;j<path.size();j++){
                HashMap<String,String> point = path.get(j);

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
//                Log.d("AA-",point.get("text"));
                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }

            lineOptions.addAll(points);
            lineOptions.width(9);
            lineOptions.color(Color.rgb(0,0,255));
        }
        if(lineOptions!=null) {
            MapFragment.mMap.addPolyline(lineOptions);
            MapFragment.tvDetails.setText(DirectionsJSONParser.sDistance);
            MapFragment.tvDetails.append(DirectionsJSONParser.sDuration);
        }
    }
}