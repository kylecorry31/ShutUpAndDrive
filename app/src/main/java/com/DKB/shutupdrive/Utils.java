package com.DKB.shutupdrive;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.util.Log;

/**
 * Created by kyle on 6/28/15.
 */
class Utils {

    private static final long MILLIS_IN_SECOND = 1000;

    private static final long MILLIS_IN_MINUTE = 60 * MILLIS_IN_SECOND;
    private static final long MILLIS_IN_HOUR = 60 * MILLIS_IN_MINUTE;

    public static long minutesToMillis(int minutes) {
        return MILLIS_IN_MINUTE * minutes;
    }

    public static final long DETECTION_INTERVAL = minutesToMillis(10);

    public static final double DRIVING_SPEED_THRESHOLD = 8.5;

    public static final boolean DEVELOPER = true;

    public static final int PHONE_BLOCK_CALLS = 1;
    public static final int PHONE_READ_CALLER = 2;
    public static final int PHONE_ALLOW_CALLS = 3;

    public static final int PERMISSION_REQUEST_CODE_SMS = 2;
    public static final int PERMISSION_REQUEST_CODE_PHONE = 3;
    public static final int PERMISSION_REQUEST_CODE_LOCATION = 4;
    public static final int PERMISSION_REQUEST_CODE_CONTACTS = 6;

    public static final String ACTION_NOTIFICATION_CLICKED = "com.DKB.shutupdrive.ACTION_NOTIFICATION_CLICKED";


    public static void setRunning(Context c, boolean running) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        prefs.edit().putBoolean(c.getString(R.string.key_running), running).apply();
    }

    public static boolean isRunning(Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        return prefs.getBoolean(c.getString(R.string.key_running), false);
    }

    public static void setStartTime(Context c, long startTime) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        prefs.edit().putLong(c.getString(R.string.key_start_time), startTime).apply();
    }

    public static long getStartTime(Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        return prefs.getLong(c.getString(R.string.key_start_time), 0);
    }

    public static long getTotalTime(Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        return prefs.getLong(c.getString(R.string.key_total_time), 0);
    }

    public static void addTime(Context c, long time) {
        long currentTime = getTotalTime(c);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        prefs.edit().putLong(c.getString(R.string.key_total_time), currentTime + time).apply();
    }

    public static double millisToHours(long millis) {
        return millis / MILLIS_IN_HOUR;
    }


    public static long getNotDrivingTime(Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        return prefs.getLong(c.getString(R.string.key_not_driving_time), 0);
    }

    public static void setNotDrivingTime(Context c, long time) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        prefs.edit().putLong(c.getString(R.string.key_not_driving_time), time).apply();
    }

    public static boolean shouldReadMessages(Context c){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        return prefs.getBoolean(c.getString(R.string.key_read_messages), false);
    }

    public static boolean getGPS(Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        return prefs.getBoolean(c.getString(R.string.key_gps), false);
    }

    public static void setGPS(Context c, boolean gps) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        prefs.edit().putBoolean(c.getString(R.string.key_gps), gps).apply();
    }

    public static boolean getGPSDrive(Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        return prefs.getBoolean(c.getString(R.string.key_gps_driving), false);
    }

    public static void setGPSDrive(Context c, boolean driving) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        prefs.edit().putBoolean(c.getString(R.string.key_gps_driving), driving).apply();
    }

    public static void setAutoReplyMessage(Context c, String msg) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        if (msg.contentEquals("")) {
            msg = c.getString(R.string.default_message);
        }
        prefs.edit().putString(c.getString(R.string.key_auto_reply_message), msg).apply();
    }

    public static String getAutoReplyMessage(Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        String preferenceString = prefs.getString(c.getString(R.string.key_auto_reply_message), c.getString(R.string.default_message));
        return preferenceString.contentEquals("") ? c.getString(R.string.default_message) : preferenceString;
    }

    public static boolean isAutoReply(Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        return prefs.getBoolean(c.getString(R.string.key_auto_reply), true);
    }

    public static void setAutoReply(Context c, boolean auto) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        prefs.edit().putBoolean(c.getString(R.string.key_auto_reply), auto).apply();
    }

    public static int getPhoneOption(Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        return Integer.valueOf(prefs.getString(c.getString(R.string.key_phone_option), "2"));
    }

    public static void setPhoneOption(Context c, int phoneOption) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        prefs.edit().putString(c.getString(R.string.key_phone_option), String.valueOf(phoneOption)).apply();
    }

    public static boolean isFirst(Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        return prefs.getBoolean(c.getString(R.string.key_first_time), true);
    }

    public static void setFirst(Context c, boolean first) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        prefs.edit().putBoolean(c.getString(R.string.key_first_time), first).apply();
    }

    public static final int NOTIFICATION_ID = 753815731;

    public static final int DETECTION_THRESHOLD = 75;

    public static String callerId(Context c, String phoneNumber) {
        if(ContextCompat.checkSelfPermission(c, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            ContentResolver resolver = c.getContentResolver();
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
                Log.e("Utils", e.getMessage());
            }
        }
        String readNumber = "";
        for (int i = 0; i < phoneNumber.length(); i++) {
            readNumber += phoneNumber.charAt(i) + " ";
        }
        return readNumber;
    }

}
