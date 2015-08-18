package com.DKB.shutupdrive;

/**
 * Created by kyle on 8/19/14.
 */

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.Date;
import java.util.Locale;

public class CarMode extends Service {

    private static int previousAudioMode;
    public static boolean autoreply = false;
    static String msg;
    private int phone;
    private NotificationManager nm;
    private TextToSpeech tts;
    private PhoneStateListener psl;
    private TelephonyManager tm;
    private AudioManager am;
    private BroadcastReceiver textReceiver, notificationReceiver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setUp();
        return START_STICKY;
    }

    private void setUp() {
        am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        previousAudioMode = am.getRingerMode();
        getUserSettings();
        silent();
        notification();
        if (checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED)
            setupTextReceiver();
        if (phone != Utils.PHONE_BLOCK_CALLS && checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            createPhoneStateListener();
            tm.listen(psl, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    private void setupTextReceiver() {
        textReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(
                        "android.provider.Telephony.SMS_RECEIVED") && autoreply) {
                    Bundle bundle = intent.getExtras();
                    SmsMessage[] msgs;
                    String msg_from;
                    String msg = CarMode.msg;
                    if (bundle != null) {
                        try {
                            Object[] pdus = (Object[]) bundle.get("pdus");
                            msgs = new SmsMessage[pdus.length];
                            for (int i = 0; i < msgs.length; i++) {
                                msgs[i] = SmsMessage
                                        .createFromPdu((byte[]) pdus[i]);
                                msg_from = msgs[i].getOriginatingAddress();
                                SmsManager smsManager = SmsManager.getDefault();
                                smsManager.sendTextMessage(msg_from, null, msg,
                                        null, null);
                            }
                        } catch (Exception e) {
                            Log.d("Text", e.getMessage());
                        }
                    }
                }
            }
        };
        registerReceiver(textReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
    }


    private void createPhoneStateListener() {
        psl = new PhoneStateListener() {
            public void onCallStateChanged(int state, final String incomingNumber) {
                //if the phone is ringing
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    //read out the caller name
                    if (phone == Utils.PHONE_READ_CALLER && checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                        tts = new TextToSpeech(CarMode.this, new TextToSpeech.OnInitListener() {
                            @SuppressWarnings("deprecation")
                            @Override
                            public void onInit(int status) {
                                if (status != TextToSpeech.ERROR) {
                                    tts.setLanguage(Locale.US);
                                    String readNumber = "New call from " + Utils.callerId(getBaseContext(), incomingNumber);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                        tts.speak(readNumber, TextToSpeech.QUEUE_FLUSH, null, null);
                                    else
                                        tts.speak(readNumber, TextToSpeech.QUEUE_FLUSH, null);
                                }
                            }
                        });
                    } else {
                        normal();
                    }

                }
                if (state == TelephonyManager.CALL_STATE_IDLE) {
                    //end text to speech
                    if (tts != null) {
                        tts.stop();
                        tts.shutdown();
                    }
                    if (phone == Utils.PHONE_ALLOW_CALLS) {
                        silent();
                    }
                }
                if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    if (tts != null) {
                        // Stop text to speech if phone is offhook
                        tts.stop();
                        tts.shutdown();
                    }

                    if (phone == Utils.PHONE_ALLOW_CALLS) {
                        silent();
                    }
                }
            }

        };
    }


    @Override
    public void onDestroy() {
        nm.cancel(Utils.NOTIFICATION_ID);
        autoreply = false;
        restorePreviousSoundMode();
        if (phone != Utils.PHONE_BLOCK_CALLS)
            tm.listen(psl, PhoneStateListener.LISTEN_NONE);
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        unregisterReceiver(notificationReceiver);
        unregisterReceiver(textReceiver);
        super.onDestroy();
    }

    private void notification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_directions_car_white_24dp)
                        .setContentTitle(getString(R.string.car_mode_on))
                        .setContentText(getString(R.string.exit_car_mode))
                        .setOngoing(true);
        mBuilder.setContentIntent(createNotDrivingPendingIntent());
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(Utils.NOTIFICATION_ID, mBuilder.build());
        notificationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Utils.ACTION_NOTIFICATION_CLICKED)) {
                    Utils.setNotDrivingTime(context, new Date().getTime());
                    stopSelf();
                }
            }
        };
        registerReceiver(notificationReceiver, new IntentFilter(Utils.ACTION_NOTIFICATION_CLICKED));
    }

    private PendingIntent createNotDrivingPendingIntent() {
        Intent i = new Intent(Utils.ACTION_NOTIFICATION_CLICKED);
        return PendingIntent.getBroadcast(getApplicationContext(), 0, i, 0);
    }

    private void getUserSettings() {
        phone = Utils.getPhoneOption(this);
        autoreply = Utils.isAutoReply(this);
        msg = Utils.getAutoReplyMessage(this);

    }

    private void silent() {
        am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    }

    private void vibrate() {
        am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
    }

    private void normal() {
        am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    }

    private void restorePreviousSoundMode() {
        switch (previousAudioMode) {
            case AudioManager.RINGER_MODE_VIBRATE:
                vibrate();
                break;
            case AudioManager.RINGER_MODE_NORMAL:
                normal();
                break;
            default:
                silent();
                break;
        }
    }
}
