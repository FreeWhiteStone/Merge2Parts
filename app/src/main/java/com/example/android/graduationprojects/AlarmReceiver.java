package com.example.android.graduationprojects;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.HashMap;

import static android.content.Context.NOTIFICATION_SERVICE;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        PendingIntent pIntent = PendingIntent.getActivity(context.getApplicationContext(), (int) System.currentTimeMillis(), intent, 0);

        Database db = new Database(context.getApplicationContext());
        HashMap<String, String> map = db.getProperImage();
        String plantName=(map.get("plant_name"));

        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            notification = new Notification.Builder(context.getApplicationContext())
                    .setSmallIcon(R.drawable.imageedit_6_7307893399)
                    .setContentTitle("Irrigation Time")
                    .setContentText("Please irrigate "+plantName)
                    .setContentIntent(pIntent).build();
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0, notification);

        Log.d("alarmReceiver","aaa");
    }
}