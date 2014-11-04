package com.KDB.shutupdrive;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Created by kyle on 8/23/14.
 */
public class GPSAlarmReceiver extends WakefulBroadcastReceiver {
    // starts service after alarm started
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, SpeedService.class));
    }
}
