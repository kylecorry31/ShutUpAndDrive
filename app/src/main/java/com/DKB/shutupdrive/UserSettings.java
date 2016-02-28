package com.DKB.shutupdrive;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

/**
 * Created by kyle on 2/16/16.
 */
public class UserSettings {


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

    public static long getNotDrivingTime(Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        return prefs.getLong(c.getString(R.string.key_not_driving_time), 0);
    }

    public static void setNotDrivingTime(Context c, long time) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        prefs.edit().putLong(c.getString(R.string.key_not_driving_time), time).apply();
    }

    public static boolean shouldReadMessages(Context c) {
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !prefs.getBoolean(c.getString(R.string.key_marshmallow), false)) {
            prefs.edit().putBoolean(c.getString(R.string.key_marshmallow), true).apply();
            return true;
        }
        return prefs.getBoolean(c.getString(R.string.key_first_time), true);
    }

    public static void setFirst(Context c, boolean first) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        prefs.edit().putBoolean(c.getString(R.string.key_first_time), first).apply();
    }

}
