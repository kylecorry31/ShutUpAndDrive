package com.KDB.shutupdrive;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Created by kyle on 7/13/14.
 */
public class BootCompletedReceiver extends BroadcastReceiver {
    private boolean start_service_bootup = false;
    private boolean activityRecognition;
    private Context mContext;
    // Store the current request type (ADD or REMOVE)
    private ActivityUtils.REQUEST_TYPE mRequestType;
    // The activity recognition update request object
    private DetectionRequester mDetectionRequester;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            SharedPreferences getPrefs;
            getPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            start_service_bootup = getPrefs.getBoolean("bootStart", false);
            activityRecognition = getPrefs.getBoolean("activityRecognition", false);
            if (activityRecognition) {
                mDetectionRequester = new DetectionRequester(context);
            }
            if (start_service_bootup && !activityRecognition) {
                Intent serviceIntent = new Intent(context, SpeedService.class);
                context.startService(serviceIntent);
            } else if (start_service_bootup && activityRecognition) {
                onStartUpdates();
            }
        }
    }

    public void onStartUpdates() {
        if (!servicesConnected()) {
            return;
        }
        mRequestType = ActivityUtils.REQUEST_TYPE.ADD;

        mDetectionRequester.requestUpdates();
    }

    private boolean servicesConnected() {
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
        if (ConnectionResult.SUCCESS == resultCode) {
            Log.d(ActivityUtils.APPTAG, "Google Play Services Available");
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(resultCode, new Activity(), 0).show();
            return false;
        }
    }
}
