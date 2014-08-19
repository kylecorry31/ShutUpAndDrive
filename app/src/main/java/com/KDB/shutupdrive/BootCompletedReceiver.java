package com.KDB.shutupdrive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by kyle on 7/13/14.
 */
public class BootCompletedReceiver extends BroadcastReceiver {
    private boolean start_service_bootup = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            SharedPreferences getPrefs;
            getPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            start_service_bootup = getPrefs.getBoolean("bootStart", false);
            if (start_service_bootup) {
                Intent serviceIntent = new Intent(context, SpeedService.class);
                context.startService(serviceIntent);
            }
        }
    }
}
