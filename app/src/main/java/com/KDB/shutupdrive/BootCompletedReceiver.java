package com.KDB.shutupdrive;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;

/**
 * Created by kyle on 7/13/14.
 */
public class BootCompletedReceiver extends BroadcastReceiver {
    private boolean start_service_bootup = false;
    AlarmManager alarmManager;
    PendingIntent pendingIntent;
    SharedPreferences getPrefs;
    Context c;

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            c = context;
            getPrefs = PreferenceManager.getDefaultSharedPreferences(c);
            start_service_bootup = getPrefs.getBoolean("bootStart", false);
            setUp();
            if (start_service_bootup) {
                if (alarmRunning()) {
                    alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            SystemClock.elapsedRealtime(),
                            frequencyMins() * ActivityUtils.MILLIS_IN_MINUTE,
                            pendingIntent);
                } else {
                    alarmManager.cancel(pendingIntent);
                }
            }
        }
    }

    private void setUp() {
        alarmManager = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        Intent gpsIntent = new Intent(c, GPSAlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(c, 0, gpsIntent, 0);
    }

    private boolean alarmRunning() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        return prefs.getBoolean("alarmRunning", false);
    }

    private int frequencyMins() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        String gpsTime = prefs.getString("gps", "10");
        return Integer.valueOf(gpsTime);
    }
}
