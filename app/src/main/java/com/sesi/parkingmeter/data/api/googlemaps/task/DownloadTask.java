package com.sesi.parkingmeter.data.api.googlemaps.task;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class DownloadTask extends AsyncTask<String, Void, String> {

    private TextView tvDetails;
    private GoogleMap googleMap;

    public DownloadTask(TextView tvDetails, GoogleMap googleMap){
        this.tvDetails = tvDetails;
        this.googleMap = googleMap;
    }

    @Override
    protected String doInBackground(String... url2) {

        StringBuilder sbData = new StringBuilder("");

        try{
            URL url = new URL(url2[0]);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                sbData.append(line);
            }
            Log.d("AAA-",sbData.toString());
            bufferedReader.close();
            //data = url[0];
        }catch(Exception e){
            Log.d("ERROR INFO DEL WS",e.toString());
        }
        return sbData.toString();
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        ParserTask parserTask = new ParserTask(tvDetails,googleMap);

        parserTask.execute(result);
    }
}
