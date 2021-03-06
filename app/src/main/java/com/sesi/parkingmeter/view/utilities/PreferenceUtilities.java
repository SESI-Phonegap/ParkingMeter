package com.sesi.parkingmeter.view.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;

import java.io.File;


public class PreferenceUtilities {

    public static final String SAVE_INITIAL_HOUR = "save_initial_hour";
    public static final String SAVE_FINAL_HOUR = "save_final_hour";
    private static final String DEFAULT_FINAL_HOUR = "00:00";
    public static final String STATUS_BUTTON_CANCEL = "status_button_cancel";
    private static final int DEFAULT_MIN_ALARM = 10;
    private static final String SAVE_DEFAULT_MIN_ALARM = "save_default_min_alarm";
    private static final boolean DEFAULT_STATUS_BUTTON = false;
    private static final String SAVE_DEFAULT_STATUS_VIBRATE = "save_default_status_vibrate";
    private static final boolean DEFAULT_STATUS_VIBRATE = false;
    private static final String SAVE_DEFAULT_STATUS_SOUND = "save_default_status_sound";
    private static final boolean DEFAULT_STATUS_SOUND = true;
    private static final String SAVE_SELECTED_SOUND = "save_selected_sound";
    private static final Uri DEFAULT_SOUND = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

    private PreferenceUtilities(){
        //EMPTY
    }

    public static void changeStatusButtonCancel(Context context, boolean status) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(STATUS_BUTTON_CANCEL, status);
        editor.apply();
    }

    public static boolean getStatusButtonCancel(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(STATUS_BUTTON_CANCEL, DEFAULT_STATUS_BUTTON);
    }

    public static void savePreferencesFinalHour(Context context, String hour) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SAVE_FINAL_HOUR, hour);
        editor.apply();
    }

    public static String getPreferencesFinalHour(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(SAVE_FINAL_HOUR, DEFAULT_FINAL_HOUR);
    }



    public static String getPreferencesInitialHour(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(SAVE_INITIAL_HOUR, DEFAULT_FINAL_HOUR);
    }

    public static void savePreferenceDefaultMinHour(Context context, int min) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(SAVE_DEFAULT_MIN_ALARM, min);
        editor.apply();

    }

    public static int getPreferenceDefaultMinHour(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(SAVE_DEFAULT_MIN_ALARM, DEFAULT_MIN_ALARM);
    }

    public static void savePreferenceDefaultVibrate(Context context, boolean bStatusVibrate) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(SAVE_DEFAULT_STATUS_VIBRATE, bStatusVibrate);
        editor.apply();
    }

    public static boolean getPreferenceDefaultVibrate(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(SAVE_DEFAULT_STATUS_VIBRATE,DEFAULT_STATUS_VIBRATE);
    }

    public static void savePreferenceDefaultSound(Context context, boolean bStatusSound){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(SAVE_DEFAULT_STATUS_SOUND,bStatusSound);
        editor.apply();
    }

    public static boolean getPreferenceDefaultSound(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(SAVE_DEFAULT_STATUS_SOUND, DEFAULT_STATUS_SOUND);
    }

    public static void saveUriSoundSelected(Context context, String path){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SAVE_SELECTED_SOUND,path);
        editor.apply();
    }

    public static Uri getUriSoundSelected(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String sPath = prefs.getString(SAVE_SELECTED_SOUND,DEFAULT_SOUND.toString());
      //  Uri uriSound = Uri.parse(prefs.getString(SAVE_SELECTED_SOUND,DEFAULT_SOUND.toString()));
        File file = new File(sPath);
        if (file.exists()){
            return Uri.fromFile(file);
        }else {
            return DEFAULT_SOUND;
        }
    }


}
