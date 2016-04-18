package com.dieam.reactnativepushnotification.modules;


import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

public class RNPushNotificationHelper {
    private Application mApplication;
    private Context mContext;

    public RNPushNotificationHelper(Application application, Context context) {
        mApplication = application;
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

    public void sendNotification(Bundle bundle) {
        Class intentClass = getMainActivityClass();
        if (intentClass == null) {
            return;
        }

        Resources res = mApplication.getResources();
        String packageName = mApplication.getPackageName();

        int msgcnt = Integer.parseInt(bundle.getString("msgcnt", "0"));

        if (msgcnt == 0) {
          this.cancelAll();
          return;
        }


        NotificationCompat.Builder notification = new NotificationCompat.Builder(mContext)
                .setContentTitle(bundle.getString("title"))
                .setTicker(bundle.getString("title"))
                .setDefaults(Notification.DEFAULT_ALL)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setAutoCancel(true)
                .setNumber(msgcnt);

        String message = bundle.getString("message");
        if (message != null) {
            notification.setContentText(message);
        } else {
            this.cancelAll();
            return;
        }

        int largeIconResId = res.getIdentifier("ic_launcher", "mipmap", packageName);
        int smallIconResId = res.getIdentifier("ic_notification", "drawable", packageName);

        if (smallIconResId == 0) {
          smallIconResId = android.R.drawable.ic_dialog_info;
        }

        Bitmap largeIconBitmap = BitmapFactory.decodeResource(res, largeIconResId);

        notification.setLargeIcon(largeIconBitmap);
        notification.setSmallIcon(smallIconResId);

        int notificationID;
        String notificationIDString = bundle.getString("notId");

        if (notificationIDString != null) {
            notificationID = Integer.parseInt(notificationIDString);
        } else {
            notificationID = (int) System.currentTimeMillis();
        }

        Intent intent = new Intent(mContext, intentClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("notification", bundle);

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
    }
}
