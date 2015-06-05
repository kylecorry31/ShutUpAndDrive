package com.DKB.shutupdrive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Date;

/**
 * Created by kyle on 8/30/14.
 */
public class NotificationReceiver extends BroadcastReceiver {
    // stops service if user isn't driving

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putLong("NotDrivingTime", new Date().getTime()).apply();
        context.stopService(new Intent(context, CarMode.class));
    }
}
