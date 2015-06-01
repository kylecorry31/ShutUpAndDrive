package com.DKB.shutupdrive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by kyle on 8/30/14.
 */
public class NotificationReceiver extends BroadcastReceiver {
    // stops service if user isn't driving
    @Override
    public void onReceive(Context context, Intent intent) {
        context.stopService(new Intent(context, CarMode.class));
    }
}
