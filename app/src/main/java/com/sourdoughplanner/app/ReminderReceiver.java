package com.sourdoughplanner.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Fired by AlarmManager at each bake step; posts the step as a notification. */
public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context c, Intent intent) {
        Reminders.ensureChannel(c);

        int id = intent.getIntExtra("id", 0);
        String title = intent.getStringExtra("title");
        String text = intent.getStringExtra("text");
        if (title == null || title.isEmpty()) title = "Sourdough step";
        if (text == null) text = "";

        Intent open = new Intent(c, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent tap = PendingIntent.getActivity(c, 0, open,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Notification n = new Notification.Builder(c, Reminders.CHANNEL)
                .setSmallIcon(R.drawable.ic_stat_loaf)
                .setColor(c.getColor(R.color.accent))
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new Notification.BigTextStyle().bigText(text))
                .setCategory(Notification.CATEGORY_REMINDER)
                .setShowWhen(true)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentIntent(tap)
                .build();

        NotificationManager nm = c.getSystemService(NotificationManager.class);
        if (nm != null) nm.notify(1000 + id, n);
    }
}
