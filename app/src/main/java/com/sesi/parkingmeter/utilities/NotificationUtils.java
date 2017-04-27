package com.sesi.parkingmeter.utilities;

/**
 * Created by QUALITY on 26/04/2017.
 */


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.sesi.parkingmeter.MainDrawerActivity;
import com.sesi.parkingmeter.R;
import com.sesi.parkingmeter.sync.ParkingReminderIntentService;
import com.sesi.parkingmeter.sync.ReminderTask;


public class NotificationUtils {


    private static final int ALARM_REMINDER_PENDING_INTENT_ID = 3417;
    private static final int ACTION_ALARM_PENDING_INTENT_ID = 1;
    private static final int ACTION_IGNORE_PENDING_INTENT_ID = 14;

    public static void remindUserBecauseCharging(Context context){
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setColor(ContextCompat.getColor(context,R.color.colorPrimary))
                .setSmallIcon(R.drawable.alarm)
                .setLargeIcon(largeIcon(context))
                .setContentTitle(context.getString(R.string.charging_reminder_notification_title))
                .setContentText(context.getString(R.string.charging_reminder_notification_body))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(
                        context.getString(R.string.charging_reminder_notification_body)))
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(contentIntent(context))
                .addAction(FinishParkingAction(context))
                .addAction(ignoreReminderAction(context))
                .setAutoCancel(true);

            notificationBuilder.setPriority(Notification.PRIORITY_HIGH);


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(ALARM_REMINDER_PENDING_INTENT_ID,notificationBuilder.build());

    }

    private static NotificationCompat.Action ignoreReminderAction(Context context){
        Intent ignoreReminderIntent = new Intent(context, ParkingReminderIntentService.class);
        ignoreReminderIntent.setAction(ReminderTask.ACTION_DISMISS_NOTIFICATION);

        PendingIntent ignoreReminderPendingIntent = PendingIntent.getService(
                context,
                ACTION_IGNORE_PENDING_INTENT_ID,
                ignoreReminderIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action ignoreReminderAction = new NotificationCompat.Action(R.drawable.ic_cancel_black_24px
                ,"No thanks",
                ignoreReminderPendingIntent);
        return ignoreReminderAction;
    }

    private static NotificationCompat.Action FinishParkingAction(Context context){
        Intent incrementWaterIntent = new Intent(context, ParkingReminderIntentService.class);
        incrementWaterIntent.setAction(ReminderTask.ACTION_FINISH_PARKING);

        PendingIntent ignoreReminderPendingIntent = PendingIntent.getService(
                context,
                ACTION_ALARM_PENDING_INTENT_ID,
                incrementWaterIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Action FinishParkingAction = new NotificationCompat.Action(R.drawable.alarm_black
                ,"I did it!",
                ignoreReminderPendingIntent);
        return FinishParkingAction;
    }

    public static void clearAllNotifications(Context context){
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }
    public static PendingIntent contentIntent(Context context){

        Intent startActivityIntent = new Intent(context, MainDrawerActivity.class);

        return PendingIntent.getActivity(
                context,
                ALARM_REMINDER_PENDING_INTENT_ID,
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static Bitmap largeIcon(Context context){
        Resources resources = context.getResources();

        Bitmap largeIcon = BitmapFactory.decodeResource(resources, R.drawable.alarm_black);
        return largeIcon;
    }
}