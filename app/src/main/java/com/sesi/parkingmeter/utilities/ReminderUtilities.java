package com.sesi.parkingmeter.utilities;


import android.content.Context;
import android.support.annotation.NonNull;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;
import com.sesi.parkingmeter.sync.ParkingReminderFirebaseJobService;


public class ReminderUtilities {
    private static final String REMINDER_JOB_TAG = "parking_reminder_tag";

    public static boolean sInitialized;
    public static FirebaseJobDispatcher dispatcher;

    private ReminderUtilities(){
        //EMPTY
    }

    public static synchronized void scheduleChargingReminder(@NonNull final Context context, int secondsStart, int syncFlextimeSeconds) {
        if (sInitialized) {
            return;
        }


        Driver driver = new GooglePlayDriver(context);
        dispatcher = new FirebaseJobDispatcher(driver);
        Job constraintReminderJob = dispatcher.newJobBuilder()
                .setService(ParkingReminderFirebaseJobService.class)
                .setTag(REMINDER_JOB_TAG)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(
                        secondsStart,
                        1 + syncFlextimeSeconds))
                .setReplaceCurrent(true)
                .build();

        dispatcher.schedule(constraintReminderJob);
        sInitialized = true;
    }
}
