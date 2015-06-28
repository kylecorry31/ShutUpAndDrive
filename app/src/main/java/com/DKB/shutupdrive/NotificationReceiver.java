package com.DKB.shutupdrive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.util.Date;

/**
 * Created by kyle on 8/30/14.
 */
public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Utils.setNotDrivingTime(context, new Date().getTime());
        context.stopService(new Intent(context, CarMode.class));
    }
}
