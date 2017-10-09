package com.sesi.parkingmeter;


import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity{

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        if (findViewById(R.id.tablet_view) == null) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
    }


}
