package com.DKB.shutupdrive.utils;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.util.Log;

/**
 * Created by kyle on 6/28/15.
 */
public class Utils {

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

    public static double millisToHours(long millis) {
        return millis / MILLIS_IN_HOUR;
    }

    public static final int NOTIFICATION_ID = 753815731;

    public static final int DETECTION_THRESHOLD = 75;

    public static String callerId(Context c, String phoneNumber) {
        if (ContextCompat.checkSelfPermission(c, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
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
