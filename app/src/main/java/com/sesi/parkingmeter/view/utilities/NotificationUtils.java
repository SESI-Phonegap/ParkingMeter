package com.sesi.parkingmeter.view.utilities;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.sesi.parkingmeter.view.activity.MainDrawerActivity;
import com.sesi.parkingmeter.R;
import com.sesi.parkingmeter.jobservice.ParkingReminderIntentService;
import com.sesi.parkingmeter.jobservice.ReminderTask;


public class NotificationUtils {


    private static final int ALARM_REMINDER_PENDING_INTENT_ID = 3417;
    private static final int ACTION_ALARM_PENDING_INTENT_ID = 1;
    private static final int ACTION_IGNORE_PENDING_INTENT_ID = 14;

    private NotificationUtils(){
        //Empty
    }
    public static void remindUserBecauseCharging(Context context) {
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        int smallIcon;
        if (Build.VERSION.SDK_INT >= 21) {
            smallIcon = R.drawable.alarm;
        } else {
            smallIcon = R.drawable.alarm_png;
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setSmallIcon(smallIcon)
                .setLargeIcon(largeIcon(context))
                .setContentTitle(context.getString(R.string.charging_reminder_notification_title))
                .setContentText(context.getString(R.string.charging_reminder_notification_body))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(
                        context.getString(R.string.charging_reminder_notification_body))
                        .setBigContentTitle(context.getString(R.string.charging_reminder_notification_title)))
                //   .setDefaults(Notification.DEFAULT_VIBRATE)


                .setLights(Color.RED, 3000, 3000)
                .setContentIntent(contentIntent(context))
               // .addAction(finishParkingAction(context))
              //  .addAction(ignoreReminderAction(context))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true);

        if (PreferenceUtilities.getPreferenceDefaultSound(context)) {
            notificationBuilder.setSound(PreferenceUtilities.getUriSoundSelected(context));
        }
        if (PreferenceUtilities.getPreferenceDefaultVibrate(context)) {
            notificationBuilder.setVibrate(new long[]{1000, 3000, 1000, 3000, 1000});
        }

        notificationBuilder.setPriority(Notification.PRIORITY_HIGH);


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(ALARM_REMINDER_PENDING_INTENT_ID, notificationBuilder.build());

        ReminderUtilities.dispatcher.cancelAll();

    }

    private static NotificationCompat.Action ignoreReminderAction(Context context) {
        Intent ignoreReminderIntent = new Intent(context, ParkingReminderIntentService.class);
        ignoreReminderIntent.setAction(ReminderTask.ACTION_DISMISS_NOTIFICATION);

        PendingIntent ignoreReminderPendingIntent = PendingIntent.getService(
                context,
                ACTION_IGNORE_PENDING_INTENT_ID,
                ignoreReminderIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action ignoreReminderAction;
        if (Build.VERSION.SDK_INT >= 21) {
            ignoreReminderAction = new NotificationCompat.Action(R.drawable.ic_cancel_black_24px
                    , "No thanks",
                    ignoreReminderPendingIntent);
        } else {
            ignoreReminderAction = new NotificationCompat.Action(R.drawable.close_circle
                    , "No thanks",
                    ignoreReminderPendingIntent);
        }
        return ignoreReminderAction;
    }

    private static NotificationCompat.Action finishParkingAction(Context context) {
        Intent incrementWaterIntent = new Intent(context, ParkingReminderIntentService.class);
        incrementWaterIntent.setAction(ReminderTask.ACTION_FINISH_PARKING);

        PendingIntent ignoreReminderPendingIntent = PendingIntent.getService(
                context,
                ACTION_ALARM_PENDING_INTENT_ID,
                incrementWaterIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        int iIcon;
        if (Build.VERSION.SDK_INT >= 21) {
            iIcon = R.drawable.alarm_black;
        } else {
            iIcon = R.drawable.alarm_black_png;
        }

        return new NotificationCompat.Action(iIcon, "I did it!", ignoreReminderPendingIntent);
    }

    public static void clearAllNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public static PendingIntent contentIntent(Context context) {

        Intent startActivityIntent = new Intent(context, MainDrawerActivity.class);
        startActivityIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        //startActivityIntent.setFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);

        return PendingIntent.getActivity(
                context,
                ALARM_REMINDER_PENDING_INTENT_ID,
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static Bitmap largeIcon(Context context) {
        Resources resources = context.getResources();

        int iIcon;
        if (Build.VERSION.SDK_INT >= 21) {
            iIcon = R.drawable.alarm_black;
        } else {
            iIcon = R.drawable.alarm_black_png;
        }
        return BitmapFactory.decodeResource(resources, iIcon);
    }
}