package com.KDB.shutupdrive;

/**
 * Created by kyle on 8/19/14.
 */

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class CarMode extends Service {
    private static int audioMode;
    private int mId;
    public static boolean autoreply = false;
    static String msg = ActivityUtils.DEFAULT_MSG;
    private final String deactivated = ActivityUtils.DEACTIVATION;
    private boolean auto;
    private boolean phone;
    private final int icon = R.drawable.notification;
    private String textNotification = ActivityUtils.SPEED_MONITOR;
    private NotificationManager nm;
    private SharedPreferences getPrefs;
    private String number;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        final AudioManager current = (AudioManager) this
                .getSystemService(Context.AUDIO_SERVICE);
        audioMode = current.getRingerMode();
        getPrefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        userSettings();
        silent();
        if (auto) {
            autoreply = true;
        }
        textNotification = ActivityUtils.RUNNING;
        notification();
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

    @Override
    public void onDestroy() {
        nm.cancel(mId);
        soundMode();
        if (!number.isEmpty()) {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(number, null, deactivated,
                    null, null);
        }
        Toast.makeText(this, getResources().getString(R.string.service_stop), Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    // notification
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @SuppressLint("NewApi")
    void notification() {
        mId = ActivityUtils.NOTIFICATION_ID;
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                this).setSmallIcon(icon).setContentTitle(getResources().getString(R.string.app_name))
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

    void userSettings() {
        //number
        number = getPrefs.getString("number", "");
        //phone
        phone = getPrefs.getBoolean("phone", false);
        // autoreply
        auto = getPrefs.getBoolean("autoReply", true);
        // autoreply message
        msg = getPrefs
                .getString("msg",
                        ActivityUtils.DEFAULT_MSG);
        if (msg.contentEquals("")) {
            msg = ActivityUtils.DEFAULT_MSG;
        }
        SpeedService.msg = msg;
    }

    // SilentToNormal and NormalToSilent device
    void silent() {
        final AudioManager mode = (AudioManager) this
                .getSystemService(Context.AUDIO_SERVICE);
        // Silent Mode
        mode.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    }

    void vibrate() {
        final AudioManager mode = (AudioManager) this
                .getSystemService(Context.AUDIO_SERVICE);
        // vibrate mode
        mode.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
    }

    void normal() {
        final AudioManager mode = (AudioManager) this
                .getSystemService(Context.AUDIO_SERVICE);
        // Normal Mode
        mode.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    }

    void soundMode() {
        if (audioMode == 1) {
            vibrate();
        } else if (audioMode == 2) {
            normal();
        } else {
            silent();
        }
    }
}
