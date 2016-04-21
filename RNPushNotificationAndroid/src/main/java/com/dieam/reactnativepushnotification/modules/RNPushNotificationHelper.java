package com.dieam.reactnativepushnotification.modules;


import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import org.json.JSONObject;

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
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setAutoCancel(true)
                .setNumber(msgcnt)
                .setColor(Color.parseColor("#009DDC"))
                .setGroupSummary(true);

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

        String notificationID = null;
        String groupID = null;
        try {
            String digestString = bundle.getString("digest");
            if (digestString == null) {
                throw new RuntimeException("No digest");
            }
            JSONObject digest = new JSONObject(bundle.getString("digest"));
            notificationID = digest.getString("notified_id");

            groupID = digest.getString("workspace");
        } catch (Exception e) {
            if (notificationID == null) {
                notificationID = String.valueOf(System.currentTimeMillis());
            }
        }

        if (groupID != null) {
            notification.setGroup(groupID);
        }

        Intent intent = new Intent(mContext, intentClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("notification", bundle);

        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);

        notification.setContentIntent(pendingIntent);

        notificationManager.notify(notificationID, 0, notification.build());
    }

    public void cancelAll() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);

        notificationManager.cancelAll();
    }
}
