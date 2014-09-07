package com.KDB.shutupdrive;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;

/**
 * Created by kyle on 9/7/14.
 */
public class PackageReceiver extends BroadcastReceiver {
    AlarmManager alarmManager;
    PendingIntent pendingIntent;
    SharedPreferences getPrefs;
    Context c;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("App Replaced", "Replaced!");
        if (intent.getAction().equals("android.intent.action.MY_PACKAGE_REPLACED")) {
            c = context;
            getPrefs = PreferenceManager.getDefaultSharedPreferences(c);
            setUp();
            if (alarmRunning()) {
                cancelAlarm();
                alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime(),
                        frequencyMins() * ActivityUtils.MILLIS_IN_MINUTE,
                        pendingIntent);
                Log.d("Package Receiver", "Alarm Restarted");
            } else{
                alarmManager.cancel(pendingIntent);
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

    private void cancelAlarm() {
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            alarmManager = null;
        }
        Log.d("Package Receiver", "Alarm Canceled");
        c.stopService(new Intent(c, CarMode.class));
    }
}
