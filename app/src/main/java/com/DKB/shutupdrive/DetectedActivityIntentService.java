package com.DKB.shutupdrive;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.FileOutputStream;
import java.util.Date;

/**
 * Created by kyle on 5/29/15.
 */
public class DetectedActivityIntentService extends IntentService {


    public DetectedActivityIntentService(String name) {
        super(name);
    }

    public DetectedActivityIntentService() {
        super("Activity Recognition");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result =
                    ActivityRecognitionResult.extractResult(intent);
            DetectedActivity mostProbableActivity = result.getMostProbableActivity();
            int confidence = mostProbableActivity.getConfidence();
            int activityType = mostProbableActivity.getType();
            long waitTime = PreferenceManager.getDefaultSharedPreferences(this).getLong("NotDrivingTime", 0);
            boolean diffTimeOK = (new Date().getTime() - waitTime) >= 10 * Constants.MILLIS_IN_MINUTE;
            if (confidence >= Constants.DETECTION_THRESHOLD && activityType == DetectedActivity.IN_VEHICLE && diffTimeOK) {
                if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("gps", false))
                    startService(new Intent(this, SpeedService.class));
                else
                    startService(new Intent(this, CarMode.class));
            } else if (confidence >= Constants.DETECTION_THRESHOLD) {
                stopService(new Intent(this, CarMode.class));
            }

        }
    }
}
