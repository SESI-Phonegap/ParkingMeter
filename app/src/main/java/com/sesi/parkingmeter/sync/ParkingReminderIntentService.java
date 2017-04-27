package com.sesi.parkingmeter.sync;


import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

public class ParkingReminderIntentService extends IntentService {

    public ParkingReminderIntentService() {
        super("ParkingReminderIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String action = intent.getAction();
        ReminderTask.executeTask(this,action);
    }
}
