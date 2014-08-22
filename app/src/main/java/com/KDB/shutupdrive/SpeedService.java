package com.KDB.shutupdrive;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.widget.Toast;

public class SpeedService extends Service implements LocationListener {
    private long minTimeGPS;
    public static boolean autoreply = false;
    private final String deactivated = ActivityUtils.DEACTIVATION;
    private SharedPreferences getPrefs;
    private LocationManager lm;
    private String number;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, getResources().getString(R.string.service_start), Toast.LENGTH_SHORT).show();
        getPrefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        userSettings();
        locationCall(minTimeGPS);
        return START_STICKY;
    }

    void locationCall(long minTimeValue) {

        lm = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                minTimeValue, 0, this);
    }


    @Override
    public void onDestroy() {
        stopService(new Intent(this, CarMode.class));
        lm.removeUpdates(this);
        if (!number.isEmpty()) {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(number, null, deactivated,
                    null, null);
        }
        Toast.makeText(this, getResources().getString(R.string.service_stop), Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    void userSettings() {
        //number
        number = getPrefs.getString("number", "");

        // sets the time in between gps calls
        String gpsTime = getPrefs.getString("gps", "7");
        if (gpsTime.contentEquals("1")) {
            minTimeGPS = 5 * 1000;
        } else if (gpsTime.contentEquals("2")) {
            minTimeGPS = 15 * 1000;
        } else if (gpsTime.contentEquals("3")) {
            minTimeGPS = 30 * 1000;
        } else if (gpsTime.contentEquals("4")) {
            minTimeGPS = 60 * 1000;
        } else if (gpsTime.contentEquals("5")) {
            minTimeGPS = 2 * 60 * 1000;
        } else if (gpsTime.contentEquals("6")) {
            minTimeGPS = 5 * 60 * 1000;
        } else if (gpsTime.contentEquals("7")) {
            minTimeGPS = 10 * 60 * 1000;
        } else if (gpsTime.contentEquals("8")) {
            minTimeGPS = 15 * 60 * 1000;
        } else if (gpsTime.contentEquals("9")) {
            minTimeGPS = 20 * 60 * 1000;
        }
        System.out.println("Min Time is set to " + minTimeGPS);
    }

    private float speed;

    @Override
    public void onLocationChanged(Location location) {
        // determines the speed then sets volume and autoreply
        if (location == null) {
            speed = 0;
            autoreply = false;
        } else {
            speed = location.getSpeed();
            System.out.println(speed);
            speed *= ActivityUtils.KM_TO_MILES;
            speed = Math.round(speed);
            if (speed > ActivityUtils.MIN_SPEED && speed < ActivityUtils.MAX_SPEED) {
                startService(new Intent(this, CarMode.class));
            } else {
                stopService(new Intent(this, CarMode.class));
                //startService(new Intent(this, CarMode.class));
            }
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

}
