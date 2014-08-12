package com.KDB.shutupdrive;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class SpeedService extends Service implements LocationListener {
    static int audioMode, beforeCall;
    int mId;
    public long minTimeGPS;
    public static boolean autoreply = false;
    static String msg = "I am driving right now, I will contact you later --Auto reply message--";
    String deactivated = "Shut Up & Drive! has been deactivated!";
    boolean auto, phone;
    int icon = R.drawable.notification;
    String textNotification = "Shut Up & Drive is monitoring your speed";
    NotificationManager nm;
    SharedPreferences getPrefs;
    LocationManager lm;
    String number;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();

        notification();

        final AudioManager current = (AudioManager) this
                .getSystemService(Context.AUDIO_SERVICE);
        audioMode = current.getRingerMode();
        getPrefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        userSettings();
        locationCall(minTimeGPS);
        if (!phone) {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            PhoneStateListener psl = new PhoneStateListener() {
                public void onCallStateChanged(int state, String incomingNumber) {
                    if (state == TelephonyManager.CALL_STATE_RINGING) {
                        System.out.println("ringing");
                        if (autoreply) {
                            normal();
                        }
                    }
                    if (state == TelephonyManager.CALL_STATE_IDLE) {
                        System.out.println("idle");
                        if (autoreply) {
                            silent();
                        }
                    }
                }
            };
            tm.listen(psl, PhoneStateListener.LISTEN_CALL_STATE);
        }
        return START_STICKY;
    }

    public void locationCall(long minTimeValue) {

        lm = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                minTimeValue, 0, this);
    }

    // notification
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @SuppressLint("NewApi")
    public void notification() {
        mId = 753815731;
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                this).setSmallIcon(icon).setContentTitle("Shut Up & Drive!")
                .setContentText(textNotification).setOngoing(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Intent resultIntent = new Intent(this, MainActivity.class);
            TaskStackBuilder sb = TaskStackBuilder.create(this);
            sb.addParentStack(MainActivity.class);
            sb.addNextIntent(resultIntent);
            PendingIntent pi = sb.getPendingIntent(0,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(pi);
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(pendingIntent);
        }
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(mId, mBuilder.build());
    }

    @Override
    public void onDestroy() {
        nm.cancel(mId);
        lm.removeUpdates(this);
        if (!number.isEmpty()) {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(number, null, deactivated,
                    null, null);
        }
        Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    public void userSettings() {
        //number
        number = getPrefs.getString("number", "");
        //phone
        phone = getPrefs.getBoolean("phone", false);
        // autoreply
        auto = getPrefs.getBoolean("autoReply", true);
        // autoreply message
        msg = getPrefs
                .getString("msg",
                        "I am driving right now, I will contact you later --Auto reply message--");
        if (msg.contentEquals("")) {
            msg = "I am driving right now, I will contact you later --Auto reply message--";
        }
        // sets the time in between gps calls
        String gpsTime = getPrefs.getString("gps", "5");
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
        }
        System.out.println("Min Time is set to " + minTimeGPS);
    }

    public float speed;

    // SilentToNomal and NormalToSilent device
    public void silent() {
        final AudioManager mode = (AudioManager) this
                .getSystemService(Context.AUDIO_SERVICE);
        // Silent Mode
        mode.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    }

    public void vibrate() {
        final AudioManager mode = (AudioManager) this
                .getSystemService(Context.AUDIO_SERVICE);
        // vibrate mode
        mode.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
    }

    public void normal() {
        final AudioManager mode = (AudioManager) this
                .getSystemService(Context.AUDIO_SERVICE);
        // Normal Mode
        mode.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    }

    public void soundMode() {
        if (audioMode == 1) {
            vibrate();
        } else if (audioMode == 2) {
            normal();
        } else {
            silent();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // determines the speed then sets volume and autoreply
        if (location == null) {
            speed = 0;
            autoreply = false;
        } else {
            speed = location.getSpeed();
            System.out.println(speed);
            speed *= 2.23694;
            speed = Math.round(speed);
            if (speed > 10 && speed < 100) {
                silent();
                if (auto) {
                    autoreply = true;
                }
                textNotification = "Shut Up & Drive is running";
                nm.cancel(mId);
                notification();
            } else {
                soundMode();
                // this sets the autoreply to false
                autoreply = false;
                textNotification = "Shut Up & Drive is monitoring your speed";
                nm.cancel(mId);
                notification();
            }
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

}
