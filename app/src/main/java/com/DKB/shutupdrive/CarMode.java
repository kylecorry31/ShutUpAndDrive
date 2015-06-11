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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Locale;

public class CarMode extends Service {

    private static int previousAudioMode;
    private int mId;
    public static boolean autoreply = false;
    static String msg = Constants.DEFAULT_MSG;
    private boolean auto;
    private int phone;
    private NotificationManager nm;
    private SharedPreferences getPrefs;
    TextToSpeech tts;
    PhoneStateListener psl;
    TelephonyManager tm;
    AudioManager am;

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
        getPrefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        getUserSettings();
        silent();
        autoreply = auto;
        notification();
        if (phone != 1) {
            tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            createPhoneStateListener();
            tm.listen(psl, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }


    protected void createPhoneStateListener() {
        psl = new PhoneStateListener() {
            public void onCallStateChanged(int state, String incomingNumber) {
                //incoming number
                final String incoming = incomingNumber;
                //if the phone is ringing
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    //read out the caller name
                    if (phone == 2) {
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
                    if (phone == 3) {
                        silent();
                    }
                }
                if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    if (tts != null) {
                        // Stop text to speech if phone is offhook
                        tts.stop();
                        tts.shutdown();
                    }

                    if (phone == 3) {
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
            Log.e("CarMode", "Cursor caused a null pointer exception");
        }
        return "";
    }

    @Override
    public void onDestroy() {
        nm.cancel(mId);
        autoreply = false;
        restorePreviousSoundMode();
        if (phone != 1)
            tm.listen(psl, PhoneStateListener.LISTEN_NONE);
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        
        super.onDestroy();
    }

    void notification() {
        mId = Constants.NOTIFICATION_ID;
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_directions_car_white_24dp)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(Constants.RUNNING)
                        .setOngoing(true)
                        .addAction(R.drawable.ic_cancel_white_24dp, "Not driving", createNotDrivingPendingIntent());
        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(mId, mBuilder.build());
    }

    PendingIntent createNotDrivingPendingIntent() {
        Intent i = new Intent(getBaseContext(), NotificationReceiver.class);
        return PendingIntent.getBroadcast(getApplicationContext(), 0, i, 0);
    }

    void getUserSettings() {
        /*
            if phone != 1
                check permission phone
                    else: phone = 1, notify
            if autoreply
                check permission sms
                    else: auto = false, notify

         */
        phone = Integer.valueOf(getPrefs.getString("phoneOpt", "2"));
        auto = getPrefs.getBoolean("autoReply", true);
        msg = getPrefs
                .getString("msg",
                        Constants.DEFAULT_MSG);
        if (msg.contentEquals("")) {
            msg = Constants.DEFAULT_MSG;
        }
    }

    void silent() {
        am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    }

    void vibrate() {
        am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
    }

    void normal() {
        am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    }

    void restorePreviousSoundMode() {
        switch (previousAudioMode) {
            case 1:
                vibrate();
                break;
            case 2:
                normal();
                break;
            default:
                silent();
                break;
        }
    }
}
