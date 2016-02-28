package com.DKB.shutupdrive;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;

/**
 * Created by kyle on 2/28/16.
 */
public class DrivingMode {

    private Context context;
    private RingerMode ringerMode;
    private boolean enabled = false;
    private TTS tts;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    private BroadcastReceiver notificationReceiver;
    private TextMessageReceiver textMessageReceiver;

    public DrivingMode(Context context) {
        this.context = context;
        ringerMode = new RingerMode(context);
        tts = new TTS(context);

    }

    private void setupTextReceiver() {
        textMessageReceiver = new TextMessageReceiver(context, new TextMessageReceiver.OnReceiveText() {
            @Override
            public void onReceive(String phoneNumber, String message) {
                String msg = UserSettings.getAutoReplyMessage(context);
                if (UserSettings.shouldReadMessages(context)) {
                    tts.speak("New text from " + Utils.callerId(context, phoneNumber) + ". The message is " + message);
                }
                if (UserSettings.isAutoReply(context)) {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phoneNumber, null, msg,
                            null, null);
                }
            }
        });
        textMessageReceiver.start();
    }

    private void createPhoneStateListener() {
        phoneStateListener = new PhoneStateListener() {
            public void onCallStateChanged(int state, final String incomingNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (UserSettings.getPhoneOption(context) == Utils.PHONE_READ_CALLER) {
                            tts.speak("New call from " + Utils.callerId(context, incomingNumber));
                        } else {
                            ringerMode.toNormalMode();
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        tts.stop();
                        if (UserSettings.getPhoneOption(context) == Utils.PHONE_ALLOW_CALLS) {
                            ringerMode.toSilentMode();
                        }
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        tts.stop();
                        if (UserSettings.getPhoneOption(context) == Utils.PHONE_ALLOW_CALLS) {
                            ringerMode.toSilentMode();
                        }
                        break;
                }
            }

        };
    }


    private void showNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_directions_car_white_24dp)
                        .setContentTitle(context.getString(R.string.car_mode_on))
                        .setContentText(context.getString(R.string.exit_car_mode))
                        .setOngoing(true)
                        .setColor(ContextCompat.getColor(context, R.color.primary));
        mBuilder.setContentIntent(createNotDrivingPendingIntent());
        Notification notification = mBuilder.build();
        NotificationManagerCompat.from(context).notify(Utils.NOTIFICATION_ID, notification);
        notificationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Utils.ACTION_NOTIFICATION_CLICKED)) {
                    UserSettings.setNotDrivingTime(context, System.currentTimeMillis());
                    context.stopService(new Intent(context, CarMode.class));
                }
            }
        };
        context.registerReceiver(notificationReceiver, new IntentFilter(Utils.ACTION_NOTIFICATION_CLICKED));
    }

    private PendingIntent createNotDrivingPendingIntent() {
        Intent i = new Intent(Utils.ACTION_NOTIFICATION_CLICKED);
        return PendingIntent.getBroadcast(context, 0, i, 0);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void enable() {
        if (isEnabled())
            return;
        ringerMode.toSilentMode();
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED)
            setupTextReceiver();
        if (UserSettings.getPhoneOption(context) != Utils.PHONE_BLOCK_CALLS && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            createPhoneStateListener();
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
        showNotification();
        enabled = true;
    }


    public void disable() {
        if (!isEnabled())
            return;
        ringerMode.restoreAudioProfileOnStart();
        if (UserSettings.getPhoneOption(context) != Utils.PHONE_BLOCK_CALLS)
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        tts.stop();
        textMessageReceiver.stop();
        NotificationManagerCompat.from(context).cancel(Utils.NOTIFICATION_ID);
        if (notificationReceiver != null)
            context.unregisterReceiver(notificationReceiver);
        enabled = false;
    }

}
