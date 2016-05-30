package com.DKB.shutupdrive;

/**
 * Created by kyle on 8/19/14.
 */

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.DKB.shutupdrive.utils.DrivingMode;
import com.DKB.shutupdrive.utils.UserSettings;

public class CarMode extends Service {

    public static boolean running = false;
    private DrivingMode drivingMode;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        drivingMode = new DrivingMode(this);
        drivingMode.enable();
        running = true;
        UserSettings.setStartTime(this, System.currentTimeMillis());
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        running = false;
        drivingMode.disable();
        UserSettings.addTime(this, System.currentTimeMillis() - UserSettings.getStartTime(this));
        super.onDestroy();
    }


}
