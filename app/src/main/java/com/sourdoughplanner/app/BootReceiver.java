package com.sourdoughplanner.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Re-arms step reminders after the phone restarts. */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Reminders.restore(c);
        }
    }
}
