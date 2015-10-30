package com.DKB.shutupdrive;

/**
 * Created by kyle on 8/19/14.
 */

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.Locale;

public class CarMode extends Service {

    private int previousAudioMode;
    private int phone;
    private TextToSpeech tts, tts2;
    private PhoneStateListener psl;
    private TelephonyManager tm;
    private AudioManager am;
    public static boolean running = false;
    private BroadcastReceiver textReceiver, notificationReceiver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setUp();
        running = true;
        Utils.setStartTime(this, System.currentTimeMillis());
        return START_STICKY;
    }

    private void setUp() {
        am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        previousAudioMode = am.getRingerMode();
        getUserSettings();
        silent();
        notification();
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED)
            setupTextReceiver();
        if (phone != Utils.PHONE_BLOCK_CALLS && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            createPhoneStateListener();
            tm.listen(psl, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    private void setupTextReceiver() {
        textReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, Intent intent) {
                if (intent.getAction().equals(
                        "android.provider.Telephony.SMS_RECEIVED")) {
                    Bundle bundle = intent.getExtras();
                    SmsMessage[] msgs;
                    String msg_from;
                    String msg = Utils.getAutoReplyMessage(context);
                    if (bundle != null) {
                        try {
                            Object[] pdus = (Object[]) bundle.get("pdus");
                            msgs = new SmsMessage[pdus.length];
                            for (int i = 0; i < msgs.length; i++) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    msgs[i] = SmsMessage
                                            .createFromPdu((byte[]) pdus[i], bundle.getString("format"));
                                } else {
                                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                                }
                                msg_from = msgs[i].getOriginatingAddress();
                                final String phoneNumber = msg_from;
                                if (Utils.shouldReadMessages(context)) {
                                    final String message = msgs[i].getMessageBody();
                                    tts2 = new TextToSpeech(CarMode.this, new TextToSpeech.OnInitListener() {
                                        @SuppressWarnings("deprecation")
                                        @Override
                                        public void onInit(int status) {
                                            if (status != TextToSpeech.ERROR) {
                                                tts2.setLanguage(Locale.US);
                                                String readNumber = "New text from " + Utils.callerId(getBaseContext(), phoneNumber) + ". The message is " + message;
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                                    tts2.speak(readNumber, TextToSpeech.QUEUE_FLUSH, null, null);
                                                else
                                                    tts2.speak(readNumber, TextToSpeech.QUEUE_FLUSH, null);
                                            }
                                        }

                                    });
                                }
                                if (Utils.isAutoReply(getApplicationContext())) {
                                    SmsManager smsManager = SmsManager.getDefault();
                                    smsManager.sendTextMessage(msg_from, null, msg,
                                            null, null);
                                }
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
                    if (phone == Utils.PHONE_READ_CALLER) {
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
        NotificationManagerCompat.from(this).cancel(Utils.NOTIFICATION_ID);
        restorePreviousSoundMode();
        running = false;
        if (phone != Utils.PHONE_BLOCK_CALLS)
            tm.listen(psl, PhoneStateListener.LISTEN_NONE);
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (tts2 != null) {
            tts2.stop();
            tts2.shutdown();
        }
        if (notificationReceiver != null)
            unregisterReceiver(notificationReceiver);
        if (textReceiver != null)
            unregisterReceiver(textReceiver);
        Utils.addTime(this, System.currentTimeMillis() - Utils.getStartTime(this));
        super.onDestroy();
    }

    private void notification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_directions_car_white_24dp)
                        .setContentTitle(getString(R.string.car_mode_on))
                        .setContentText(getString(R.string.exit_car_mode))
                        .setOngoing(true)
                        .setColor(ContextCompat.getColor(this, R.color.primary));
        mBuilder.setContentIntent(createNotDrivingPendingIntent());
        Notification notification = mBuilder.build();
        NotificationManagerCompat.from(this).notify(Utils.NOTIFICATION_ID, notification);
        notificationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Utils.ACTION_NOTIFICATION_CLICKED)) {
                    Utils.setNotDrivingTime(context, System.currentTimeMillis());
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
