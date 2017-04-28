package com.sesi.parkingmeter.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by QUALITY on 26/04/2017.
 */

public class PreferenceUtilities {

    public static final String SAVE_INITIAL_HOUR = "save_initial_hour";
    public static final String SAVE_FINAL_HOUR = "save_final_hour";
    private static final String DEFAULT_FINAL_HOUR = "00:00";
    public static final String STATUS_BUTTON_CANCEL = "status_button_cancel";
    private static final boolean DEFAULT_STATUS_BUTTON = false;

    public static final String KEY_PARKING_FINISH = "Parking-Finish";
    public static final String KEY_CHARGING_REMINDER_COUNT = "charging-reminder-count";

    private static final int DEFAULT_COUNT = 0;

    synchronized private static void setWaterCount(Context context, int glassesOfWater) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_PARKING_FINISH, glassesOfWater);
        editor.apply();
    }

    public static int getWaterCount(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int glassesOfWater = prefs.getInt(KEY_PARKING_FINISH, DEFAULT_COUNT);
        return glassesOfWater;
    }

    synchronized public static void incrementWaterCount(Context context) {
        int waterCount = PreferenceUtilities.getWaterCount(context);
        PreferenceUtilities.setWaterCount(context, ++waterCount);
    }

    synchronized public static void incrementChargingReminderCount(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int chargingReminders = prefs.getInt(KEY_CHARGING_REMINDER_COUNT, DEFAULT_COUNT);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_CHARGING_REMINDER_COUNT, ++chargingReminders);
        editor.apply();
    }

    public static int getChargingReminderCount(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int chargingReminders = prefs.getInt(KEY_CHARGING_REMINDER_COUNT, DEFAULT_COUNT);
        return chargingReminders;
    }

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
}
