package com.sesi.parkingmeter.sync;

import android.content.Context;

import com.sesi.parkingmeter.utilities.NotificationUtils;
import com.sesi.parkingmeter.utilities.PreferenceUtilities;


public class ReminderTask {

    public static final String ACTION_CHARGING_REMINDER = "charging-reminder";
    public static final  String ACTION_DISMISS_NOTIFICATION = "dismiss-notification";
    public static final String ACTION_FINISH_PARKING ="increment-water-count";
    public static void executeTask(Context context, String action){
        if(ACTION_FINISH_PARKING.equals(action)){
            incrementWaterCount(context);
        }else if( ACTION_DISMISS_NOTIFICATION.equals(action)){
            NotificationUtils.clearAllNotifications(context);
        }else if(ACTION_CHARGING_REMINDER.equals(action)){
            issueChargingReminder(context);
        }
    }

    private static void issueChargingReminder(Context context) {
     //   PreferenceUtilities.incrementChargingReminderCount(context);
        NotificationUtils.remindUserBecauseCharging(context);
    }

    private static void incrementWaterCount(Context context){
      //  PreferenceUtilities.incrementWaterCount(context);

        NotificationUtils.clearAllNotifications(context);
    }
}
