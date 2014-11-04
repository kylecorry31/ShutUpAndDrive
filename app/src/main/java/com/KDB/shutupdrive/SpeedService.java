package com.KDB.shutupdrive;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class SpeedService extends Service implements LocationListener {
    private LocationManager lm;
    //the speed of the phone
    private float speed;

    //required by service (service runs in background)
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //request location updates
        locationCall();
        //keep service running
        return START_STICKY;
    }

    void locationCall() {

        if (airplaneModeOff()) {
            if (gps()) {
                //get the location service
                lm = (LocationManager) this
                        .getSystemService(Context.LOCATION_SERVICE);
                //get gps and set to receive location updates at a user specified time
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 0, this);
            } else {
                gpsNotification();

            }
        } else {
            stopSelf();
        }
    }

    private void gpsNotification() {
        // Lets user know that it needs gps via notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(getResources().getString(R.string.gps_needed))
                .setSmallIcon(R.drawable.notification)
                .setContentIntent(getContentIntent());
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(7493840, builder.build());
    }

    private PendingIntent getContentIntent() {
        // This is for the notification...opens gps if clicked
        Intent gpsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        return PendingIntent.getActivity(getApplicationContext(), 0, gpsIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private boolean airplaneModeOff() {
        // Checks if airplane mode is off
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1)
            return Settings.System.getInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 0;
        else
            return Settings.Global.getInt(getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) == 0;
    }



    //called on location received from gps
    @Override
    public void onLocationChanged(Location location) {
        lm.removeUpdates(this);
        // determines the speed
        if (location == null) {
            //if gps does not send location
            speed = 0;
        } else {
            //if location is received
            //get the speed of the phone
            speed = location.getSpeed();
            Log.d("Speed Service", "The speed is " + speed);
            //convert speed to miles per hour
            speed *= ActivityUtils.KM_TO_MILES;
            //round the speed
            speed = Math.round(speed);
            //determine if driving or not
            if (alarmRunning()) {
                if (speed > ActivityUtils.MIN_SPEED && speed < ActivityUtils.MAX_SPEED) {

                    //enable driving mode
                    if (!isServiceRunning())
                        startService(new Intent(this, CarMode.class));
                } else {
                    //disable driving mode
                    stopService(new Intent(this, CarMode.class));
                    //debug mode
                    //if (!isServiceRunning())
                    //   startService(new Intent(this, CarMode.class));
                }
            }

        }

        stopSelf();
    }


    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (CarMode.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private boolean alarmRunning() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getBoolean("alarmRunning", false);
    }

    private boolean gps() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
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
