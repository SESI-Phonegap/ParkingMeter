package com.sesi.parkingmeter.utilities;

import android.content.Context;
import android.graphics.Typeface;
import android.support.design.widget.TextInputEditText;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;


public class Utils {

    private Utils(){
        //EMPTY
    }

    public static void changeRobotoRegular(Context context, TextView textView) {
        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "/Roboto-Regular.ttf");
        textView.setTypeface(typeface);
    }

    public static void changeRobotoLight(Context context, TextView textView) {
        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "/Roboto-Light.ttf");
        textView.setTypeface(typeface);
    }

    public static void showDate(TextView cardviewFecha) {

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);

        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        cardviewFecha.setText(new StringBuilder().append(day).append("/")
                .append(month + 1).append("/").append(year));
    }

    public static void showHour(TextView cardviewHora, TextInputEditText tidHhora) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        cardviewHora.setText(hour + ":" + minute);
        tidHhora.setText(hour + ":" + minute);
    }

    public static int convertActualHourToMinutes() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        return convertHourToMinutes(hour, minute);
    }

    public static int convertHourToMinutes(int hour, int minutes) {
        return (hour * Constants.SIXTY_MINUTES) + minutes;
    }

    public static String genPK(){
        return Constants.PK_R200 + Constants.PK_R700 + Constants.PK_R900 + Constants.PK_R400 + Constants.PK_R600;
    }

    public static String obtenerDireccionesURL(LatLng origin, LatLng dest) {
        String sOrigin = "origin=" + origin.latitude + "," + origin.longitude;
        String sDest = "destination=" + dest.latitude + "," + dest.longitude;
        String parameters = Constants.UNITS + sOrigin + "&" + sDest + "&" + Constants.SENSOR_FALSE;
        return Constants.URL_GOOGLE_MAPS_API + Constants.JSON_TYPE + "?" + parameters;
    }

}
