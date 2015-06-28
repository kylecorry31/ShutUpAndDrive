package com.DKB.shutupdrive;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by kyle on 6/28/15.
 */
class Utils {

    private static final long MILLIS_IN_SECOND = 1000;

    private static final long MILLIS_IN_MINUTE = 60 * MILLIS_IN_SECOND;

    public static long minutesToMillis(int minutes) {
        return MILLIS_IN_MINUTE * minutes;
    }

    public static final long DETECTION_INTERVAL = minutesToMillis(10);

    public static final double DRIVING_SPEED_THRESHOLD = 8.5;

    public static final boolean DEVELOPER = true;

    public static final int PHONE_BLOCK_CALLS = 1;
    public static final int PHONE_READ_CALLER = 2;
    public static final int PHONE_ALLOW_CALLS = 3;


    public static int getTutNumber(Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        return prefs.getInt(c.getString(R.string.key_tutorial_number), 0);
    }

    public static void setTutNumber(Context c, int tutNum) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        prefs.edit().putInt(c.getString(R.string.key_tutorial_number), tutNum).apply();
    }

    public static void setRunning(Context c, boolean running) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        prefs.edit().putBoolean(c.getString(R.string.key_running), running).apply();
    }

    public static boolean isRunning(Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        return prefs.getBoolean(c.getString(R.string.key_running), false);
    }


    public static boolean isAutoStart(Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        return prefs.getBoolean(c.getString(R.string.key_auto_start), false);
    }

    public static long getNotDrivingTime(Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        return prefs.getLong(c.getString(R.string.key_not_driving_time), 0);
    }

    public static void setNotDrivingTime(Context c, long time) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        prefs.edit().putLong(c.getString(R.string.key_not_driving_time), time).apply();
    }

    public static boolean getGPS(Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        return prefs.getBoolean(c.getString(R.string.key_gps), false);
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

    public static int getPhoneOption(Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        return Integer.valueOf(prefs.getString(c.getString(R.string.key_phone_option), "2"));
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

}
