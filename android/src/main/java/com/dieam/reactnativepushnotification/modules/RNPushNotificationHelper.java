package com.dieam.reactnativepushnotification.modules;


import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class RNPushNotificationHelper {
    private Context mContext;

    public RNPushNotificationHelper(Application context) {
        mContext = context;
    }

    public Class getMainActivityClass() {
        String packageName = mContext.getPackageName();
        Intent launchIntent = mContext.getPackageManager().getLaunchIntentForPackage(packageName);
        String className = launchIntent.getComponent().getClassName();
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private AlarmManager getAlarmManager() {
        return (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
    }

    private PendingIntent getScheduleNotificationIntent(Bundle bundle) {
        int notificationID;
        String notificationIDString = bundle.getString("id");

        if ( notificationIDString != null ) {
            notificationID = Integer.parseInt(notificationIDString);
        } else {
            notificationID = (int) System.currentTimeMillis();
        }

        Intent notificationIntent = new Intent(mContext, RNPushNotificationPublisher.class);
        notificationIntent.putExtra(RNPushNotificationPublisher.NOTIFICATION_ID, notificationID);
        notificationIntent.putExtras(bundle);

        return PendingIntent.getBroadcast(mContext, notificationID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void sendNotificationScheduled(Bundle bundle) {
        Class intentClass = getMainActivityClass();
        if (intentClass == null) {
            return;
        }

        Double fireDateDouble = bundle.getDouble("fireDate", 0);
        if (fireDateDouble == 0) {
            return;
        }

        long fireDate = Math.round(fireDateDouble);
        long currentTime = System.currentTimeMillis();

        Log.i("ReactSystemNotification", "fireDate: " + fireDate + ", Now Time: " + currentTime);
        PendingIntent pendingIntent = getScheduleNotificationIntent(bundle);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getAlarmManager().setExact(AlarmManager.RTC_WAKEUP, fireDate, pendingIntent);
        } else {
            getAlarmManager().set(AlarmManager.RTC_WAKEUP, fireDate, pendingIntent);
        }
    }

    public void sendNotification(Bundle bundle) {
        Class intentClass = getMainActivityClass();
        if (intentClass == null) {
            return;
        }

        if (bundle.getString("message") == null) {
            return;
        }

        Resources res = mContext.getResources();
        String packageName = mContext.getPackageName();

        String title = bundle.getString("title");
        if (title == null) {
            ApplicationInfo appInfo = mContext.getApplicationInfo();
            title = mContext.getPackageManager().getApplicationLabel(appInfo).toString();
        }

        NotificationCompat.Builder notification = new NotificationCompat.Builder(mContext)
                .setContentTitle(title)
                .setTicker(bundle.getString("ticker"))
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(bundle.getBoolean("autoCancel", true));

        notification.setContentText(bundle.getString("message"));

        String largeIcon = bundle.getString("largeIcon");

        String subText = bundle.getString("subText");

        if ( subText != null ) {
            notification.setSubText(subText);
        }

        String number = bundle.getString("number");

        if ( number != null ) {
            notification.setNumber(Integer.parseInt(number));
        }

        int smallIconResId;
        int largeIconResId;

        String smallIcon = bundle.getString("smallIcon");

        if ( smallIcon != null ) {
            smallIconResId = res.getIdentifier(smallIcon, "mipmap", packageName);
        } else {
            notification.setContentText("<missing message content>");
        }

        String msgcnt = bundle.getString("msgcnt");
        if (msgcnt != null) {
            notification.setNumber(Integer.parseInt(msgcnt));
        }

        int largeIconResId = res.getIdentifier("ic_launcher", "mipmap", packageName);

        Bitmap largeIconBitmap = BitmapFactory.decodeResource(res, largeIconResId);

        if ( largeIconResId != 0 && ( largeIcon != null || android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP ) ) {
            notification.setLargeIcon(largeIconBitmap);
        }

        notification.setSmallIcon(smallIconResId);
        String bigText = bundle.getString("bigText");

        if (bigText == null ) {
            bigText = bundle.getString("message");
        }

        notification.setStyle(new NotificationCompat.BigTextStyle().bigText(bigText));

        Intent intent = new Intent(mContext, intentClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("notification", bundle);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notification.setSound(defaultSoundUri);

        if ( android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
            notification.setCategory(NotificationCompat.CATEGORY_CALL);

            String color = bundle.getString("color");
            if (color != null) {
                notification.setColor(Color.parseColor(color));
            }
        }

        int notificationID;
        String notificationIDString = bundle.getString("notId");

        if (notificationIDString != null) {
            notificationID = Integer.parseInt(notificationIDString);
        } else {
            notificationID = (int) System.currentTimeMillis();
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, notificationID, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        notification.setContentIntent(pendingIntent);

        notificationManager.notify(notificationID, notification.build());
    }

    public void cancelAll() {
        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.cancelAll();

        Bundle b = new Bundle();
        b.putString("id", "0");
        getAlarmManager().cancel(getScheduleNotificationIntent(b));
    }
}
