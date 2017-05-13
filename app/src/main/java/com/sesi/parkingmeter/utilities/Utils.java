package com.sesi.parkingmeter.utilities;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Camera;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputEditText;
import android.widget.TextView;

import java.util.Calendar;

/**
 * Created by Chris on 17/04/2017.
 */

public class Utils {

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

    public static int convertActualHourToMinutes() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        int result = convertHourToMinutes(hour, minute);

        return result;
    }

    public static int convertHourToMinutes(int hour, int minutes) {

        int convertMinutes = (hour * SIXTY_MINUTES) + minutes;

        return convertMinutes;
    }




}
