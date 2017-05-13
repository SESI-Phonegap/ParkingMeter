package com.sesi.parkingmeter.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;


public class PreferenceUtilities {

    public static final String SAVE_INITIAL_HOUR = "save_initial_hour";
    public static final String SAVE_FINAL_HOUR = "save_final_hour";
    private static final String DEFAULT_FINAL_HOUR = "00:00";
    public static final String STATUS_BUTTON_CANCEL = "status_button_cancel";
    public static final int DEFAULT_MIN_ALARM = 10;
    public static final String SAVE_DEFAULT_MIN_ALARM = "save_default_min_alarm";
    private static final boolean DEFAULT_STATUS_BUTTON = false;
    public static final String SAVE_DEFAULT_STATUS_VIBRATE = "save_default_status_vibrate";
    private static final boolean DEFAULT_STATUS_VIBRATE = true;
    public static final String SAVE_DEFAULT_STATUS_SOUND = "save_default_status_sound";
    private static final boolean DEFAULT_STATUS_SOUND = true;


    public static void changeStatusButtonCancel(Context context, boolean status) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(STATUS_BUTTON_CANCEL, status);
        editor.apply();
    }

    public static boolean getStatusButtonCancel(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean status = prefs.getBoolean(STATUS_BUTTON_CANCEL, DEFAULT_STATUS_BUTTON);

        return status;
    }

    public static void savePreferencesFinalHour(Context context, String hour) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SAVE_FINAL_HOUR, hour);
        editor.apply();
    }

    public static String getPreferencesFinalHour(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String hour = prefs.getString(SAVE_FINAL_HOUR, DEFAULT_FINAL_HOUR);

        return hour;
    }

    public static void savePreferenceHourIni(Context context, String hour) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SAVE_INITIAL_HOUR, hour);
        editor.apply();
    }

    public static String getPreferencesInitialHour(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String hour = prefs.getString(SAVE_INITIAL_HOUR, DEFAULT_FINAL_HOUR);

        return hour;
    }

    public static void savePreferenceDefaultMinHour(Context context, int min) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(SAVE_DEFAULT_MIN_ALARM, min);
        editor.apply();

    }

    public static int getPreferenceDefaultMinHour(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int min = prefs.getInt(SAVE_DEFAULT_MIN_ALARM, DEFAULT_MIN_ALARM);
        return min;
    }

    public static void savePreferenceDefaultVibrate(Context context, boolean bStatusVibrate) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(SAVE_DEFAULT_STATUS_VIBRATE, bStatusVibrate);
        editor.apply();
    }

    public static boolean getPreferenceDefaultVibrate(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean bStatus = prefs.getBoolean(SAVE_DEFAULT_STATUS_VIBRATE,DEFAULT_STATUS_VIBRATE);
        return bStatus;
    }

    public static void savePreferenceDefaultSound(Context context, boolean bStatusSound){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(SAVE_DEFAULT_STATUS_SOUND,bStatusSound);
        editor.apply();
    }

    public static boolean getPreferenceDefaultSound(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean bStatus = prefs.getBoolean(SAVE_DEFAULT_STATUS_SOUND, DEFAULT_STATUS_SOUND);
        return bStatus;
    }


}
