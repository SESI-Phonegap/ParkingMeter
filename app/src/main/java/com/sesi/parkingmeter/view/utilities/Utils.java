package com.sesi.parkingmeter.view.utilities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.os.Parcelable;
import android.support.design.widget.TextInputEditText;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.sesi.parkingmeter.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


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

        String formatHour = hour + ":" + minute;
        cardviewHora.setText(formatHour);
        tidHhora.setText(formatHour);
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

    public static void sharedSocial(Context context) {
        List<Intent> targetShareIntents = new ArrayList<>();
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resInfos = pm.queryIntentActivities(shareIntent, 0);
        if (!resInfos.isEmpty()) {
            for (ResolveInfo resInfo : resInfos) {
                String packageName = resInfo.activityInfo.packageName;
                Log.i("Package Name", packageName);

/*         if (packageName.contains("com.twitter.android") || packageName.contains("com.facebook.katana")
                 || packageName.contains("com.whatsapp") || packageName.contains("com.google.android.apps.plus")
                 || packageName.contains("com.google.android.talk") || packageName.contains("com.slack")
                 || packageName.contains("com.google.android.gm") || packageName.contains("com.facebook.orca")
                 || packageName.contains("com.yahoo.mobile") || packageName.contains("com.skype.raider")
                 || packageName.contains("com.android.mms")|| packageName.contains("com.linkedin.android")
                 || packageName.contains("com.google.android.apps.messaging")) {*/
                if (packageName.contains("com.twitter.android") || packageName.contains("com.facebook.katana")
                        || packageName.contains("com.whatsapp")
                        || packageName.contains("com.google.android.apps.plus")) {
                    Intent intent = new Intent();

                    intent.setComponent(new ComponentName(packageName, resInfo.activityInfo.name));
                    intent.putExtra(Constants.APPNAME, resInfo.loadLabel(pm).toString());
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, Constants.URL_PLAYSTORE_APP);
                    intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.compartir));
                    intent.setPackage(packageName);
                    targetShareIntents.add(intent);
                }
            }
            if (!targetShareIntents.isEmpty()) {
                Collections.sort(targetShareIntents, new Comparator<Intent>() {
                    @Override
                    public int compare(Intent o1, Intent o2) {
                        return o1.getStringExtra("AppName").compareTo(o2.getStringExtra("AppName"));
                    }
                });
                Intent chooserIntent = Intent.createChooser(targetShareIntents.remove(0), "Select app to share");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetShareIntents.toArray(new Parcelable[]{}));
                context.startActivity(chooserIntent);
            } else {
                Toast.makeText(context.getApplicationContext(), "No app to share.", Toast.LENGTH_LONG).show();
            }
        }
    }

}
