package com.DKB.shutupdrive;

import android.app.IntentService;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
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
                startService(new Intent(this, CarMode.class));
                if (Constants.DEVELOPER)
                    logDriving(confidence);
            } else if (confidence >= Constants.DETECTION_THRESHOLD) {
                stopService(new Intent(this, CarMode.class));
            }

        }
    }

    private void logDriving(int confidence) {
        try {
            FileOutputStream outputStream = openFileOutput("logfile.txt", MODE_APPEND);
            outputStream.write(new String(new Date().toString() + "  -  Driving: " + confidence + " %\n").getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
