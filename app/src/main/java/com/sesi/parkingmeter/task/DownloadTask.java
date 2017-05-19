package com.sesi.parkingmeter.task;

/**
 * Created by Chris on 16/05/2017.
 */

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;


public class DownloadTask extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... url2) {

        String data = "";

        try{
            URL url = new URL(url2[0]);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line = "";

            while ((line = bufferedReader.readLine()) != null) {
                data += line;
            }
            Log.d("AAA-",data);
            bufferedReader.close();
            //data = url[0];
        }catch(Exception e){
            Log.d("ERROR INFO DEL WS",e.toString());
        }
        return data;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        ParserTask parserTask = new ParserTask();

        parserTask.execute(result);
    }
}