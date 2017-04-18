package com.sesi.parkingmeter;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

/**
 * Created by Chris on 17/04/2017.
 */

public class Utils {

    public static void changeRobotoRegular(Context context, TextView textView){
        Typeface typeface = Typeface.createFromAsset(context.getAssets(),"/Roboto-Regular.ttf");
        textView.setTypeface(typeface);
    }

    public static void changeRobotoLight(Context context,TextView textView){
        Typeface typeface = Typeface.createFromAsset(context.getAssets(),"/Roboto-Light.ttf");
        textView.setTypeface(typeface);
    }
}
