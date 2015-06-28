package com.DKB.shutupdrive;

/**
 * Created by kyle on 8/19/14.
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

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
        if (phone != Utils.PHONE_BLOCK_CALLS) {
            tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            createPhoneStateListener();
            tm.listen(psl, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }


    private void createPhoneStateListener() {
        psl = new PhoneStateListener() {
            public void onCallStateChanged(int state, String incomingNumber) {
                //incoming number
                final String incoming = incomingNumber;
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
                                    String readNumber = "New call from ";
                                    String name = quickCallerId(incoming);
                                    //if the number is not in the contacts, say the number
                                    if (name.isEmpty()) {
                                        for (int i = 0; i < incoming.length(); i++) {
                                            readNumber = readNumber + incoming.charAt(i) + " ";
                                        }
                                    } else {
                                        readNumber = readNumber + name;
                                    }
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


    private String quickCallerId(String phoneNumber) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        ContentResolver resolver = getContentResolver();
        Cursor cur = resolver.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cur != null && cur.moveToFirst()) {
            String value = cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            if (value != null) {
                cur.close();
                return value;
            }
        }
        try {
            assert cur != null;
            cur.close();
        } catch (NullPointerException e) {
            Log.e("CarMode", e.getMessage());
        }
        return "";
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

        super.onDestroy();
    }

    private void notification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_directions_car_white_24dp)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.car_mode_on))
                        .setOngoing(true)
                        .addAction(R.drawable.ic_cancel_white_24dp, getString(R.string.not_driving), createNotDrivingPendingIntent());
        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(Utils.NOTIFICATION_ID, mBuilder.build());
    }

    private PendingIntent createNotDrivingPendingIntent() {
        Intent i = new Intent(getBaseContext(), NotificationReceiver.class);
        return PendingIntent.getBroadcast(getApplicationContext(), 0, i, 0);
    }

    private void getUserSettings() {
        /*
            if phone != 1
                check permission phone
                    else: phone = 1, notify
            if autoreply
                check permission sms
                    else: auto = false, notify

         */
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
