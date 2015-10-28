package com.DKB.shutupdrive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RebootReceiver extends BroadcastReceiver {


    public RebootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) && Utils.isRunning(context)) {
            context.startService(new Intent(context, RebootService.class));
        }
    }
}
