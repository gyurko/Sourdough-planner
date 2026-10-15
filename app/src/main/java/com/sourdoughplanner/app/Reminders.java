package com.sourdoughplanner.app;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Schedules one alarm per bake step; ReminderReceiver turns each into a notification. */
final class Reminders {

    static final String CHANNEL = "bake_steps";
    private static final String PREFS = "sourdough";
    private static final String KEY_EVENTS = "events";
    private static final String KEY_COUNT = "count";

    private Reminders() {}

    static final class Result {
        final int count;
        final boolean exact;
        Result(int count, boolean exact) { this.count = count; this.exact = exact; }
    }

    static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    static void ensureChannel(Context c) {
        NotificationManager nm = c.getSystemService(NotificationManager.class);
        if (nm == null) return;
        NotificationChannel ch = new NotificationChannel(
                CHANNEL, "Bake steps", NotificationManager.IMPORTANCE_HIGH);
        ch.setDescription("A reminder at each step of your sourdough schedule");
        nm.createNotificationChannel(ch);
    }

    /** Replaces any existing reminders with the steps in json. Past steps are skipped. */
    static Result schedule(Context c, String json) throws JSONException {
        cancelAll(c);
        JSONArray steps = new JSONArray(json);
        AlarmManager am = c.getSystemService(AlarmManager.class);
        boolean exact = Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms();
        long now = System.currentTimeMillis();
        int scheduled = 0;

        for (int i = 0; i < steps.length(); i++) {
            JSONObject s = steps.getJSONObject(i);
            long at = s.getLong("at");
            if (at <= now) continue;
            PendingIntent pi = pending(c, i, s.optString("title"), s.optString("text"),
                    PendingIntent.FLAG_UPDATE_CURRENT);
            if (exact) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
            }
            scheduled++;
        }
        prefs(c).edit()
                .putString(KEY_EVENTS, json)
                .putInt(KEY_COUNT, steps.length())
                .apply();
        return new Result(scheduled, exact);
    }

    static void cancelAll(Context c) {
        SharedPreferences p = prefs(c);
        int count = p.getInt(KEY_COUNT, 0);
        AlarmManager am = c.getSystemService(AlarmManager.class);
        for (int i = 0; i < count; i++) {
            PendingIntent pi = pending(c, i, null, null, PendingIntent.FLAG_NO_CREATE);
            if (pi != null) {
                am.cancel(pi);
                pi.cancel();
            }
        }
        p.edit().putInt(KEY_COUNT, 0).putString(KEY_EVENTS, "[]").apply();
    }

    static int futureCount(Context c) {
        try {
            JSONArray steps = new JSONArray(prefs(c).getString(KEY_EVENTS, "[]"));
            long now = System.currentTimeMillis();
            int n = 0;
            for (int i = 0; i < steps.length(); i++) {
                if (steps.getJSONObject(i).getLong("at") > now) n++;
            }
            return n;
        } catch (JSONException e) {
            return 0;
        }
    }

    /** Alarms are wiped on reboot; put back the ones still in the future. */
    static void restore(Context c) {
        String json = prefs(c).getString(KEY_EVENTS, "[]");
        try {
            schedule(c, json);
        } catch (JSONException ignored) {
            // Nothing valid stored.
        }
    }

    private static PendingIntent pending(Context c, int id, String title, String text, int flags) {
        // A distinct action per step keeps the PendingIntents from matching each other.
        Intent i = new Intent(c, ReminderReceiver.class)
                .setAction(c.getPackageName() + ".STEP_" + id)
                .putExtra("id", id)
                .putExtra("title", title)
                .putExtra("text", text);
        return PendingIntent.getBroadcast(c, id, i, flags | PendingIntent.FLAG_IMMUTABLE);
    }
}
