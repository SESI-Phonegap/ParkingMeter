package com.sesi.parkingmeter;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                openMainActivity();
            }
        };
        handler.postDelayed(runnable,3000);
    }

    public void openMainActivity(){
        Intent intent = new Intent(this,MainDrawerActivity.class);
        startActivity(intent);
    }
}
