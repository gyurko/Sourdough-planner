package com.sourdoughplanner.app;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.webkit.JavascriptInterface;

/**
 * Methods the page calls as window.AndroidBridge.*.
 * JavascriptInterface methods run on a WebView background thread, so UI work is posted.
 */
final class Bridge {

    private static final int REQ_NOTIFICATIONS = 1;
    private final Activity activity;

    Bridge(Activity activity) {
        this.activity = activity;
    }

    /** json: [{"at": epochMillis, "title": "...", "text": "..."}]. Returns a message for a toast. */
    @JavascriptInterface
    public String scheduleReminders(String json) {
        try {
            Reminders.Result r = Reminders.schedule(activity, json);

            boolean canNotify = Build.VERSION.SDK_INT < 33
                    || activity.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                       == PackageManager.PERMISSION_GRANTED;
            if (!canNotify) {
                activity.runOnUiThread(() -> activity.requestPermissions(
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIFICATIONS));
                return r.count + " reminders set. Allow notifications so they can appear.";
            }
            if (!r.exact) {
                return r.count + " reminders set. Exact alarms are off, so they may arrive a few minutes late.";
            }
            return r.count + " reminders set";
        } catch (Exception e) {
            return "Couldn't set reminders: " + e.getMessage();
        }
    }

    @JavascriptInterface
    public String clearReminders() {
        Reminders.cancelAll(activity);
        return "Reminders cleared";
    }

    /** Number of scheduled reminders still in the future. */
    @JavascriptInterface
    public int reminderCount() {
        return Reminders.futureCount(activity);
    }

    @JavascriptInterface
    public void copy(String text) {
        ClipboardManager cm = activity.getSystemService(ClipboardManager.class);
        if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("Bake schedule", text));
    }

    @JavascriptInterface
    public void share(String text) {
        Intent send = new Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_SUBJECT, "Sourdough bake plan")
                .putExtra(Intent.EXTRA_TEXT, text);
        activity.runOnUiThread(() ->
                activity.startActivity(Intent.createChooser(send, "Share bake plan")));
    }

    @JavascriptInterface
    public String loadSettings() {
        return Reminders.prefs(activity).getString("settings", "");
    }

    @JavascriptInterface
    public void saveSettings(String json) {
        Reminders.prefs(activity).edit().putString("settings", json).apply();
    }
}
