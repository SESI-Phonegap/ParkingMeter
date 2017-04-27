package com.sesi.parkingmeter.utilities;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputEditText;
import android.widget.TextView;

import java.util.Calendar;

/**
 * Created by Chris on 17/04/2017.
 */

public class Utils {

    private final static  String SAVE_FINAL_HOUR = "save_final_hour";
    private final static String DEFAULT_FINAL_HOUR = "00:00";
    private final static String STATUS_BUTTON_CANCEL = "status_button_cancel";
    private final static boolean DEFAULT_STATUS_BUTTON = false;
    private final static int SIXTY_MINUTES = 60;

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

    public static int convertHourToMinutes(int hour, int minutes) {

        int convertMinutes = (hour * SIXTY_MINUTES) + minutes;

        return convertMinutes;
    }

    public static void changeStatusButtonCancel(Context context, boolean status){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(STATUS_BUTTON_CANCEL,status);
        editor.apply();
    }

    public static boolean getStatusButtonCancel(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean status = prefs.getBoolean(STATUS_BUTTON_CANCEL,DEFAULT_STATUS_BUTTON);

        return  status;
    }

    public static void savePreferencesFinalHour(Context context, String hour){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SAVE_FINAL_HOUR,hour);
        editor.apply();
    }

    public static String getPreferencesFinalHour(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String hour = prefs.getString(SAVE_FINAL_HOUR,DEFAULT_FINAL_HOUR);

        return hour;
    }


}
