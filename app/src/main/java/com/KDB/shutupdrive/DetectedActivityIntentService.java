package com.KDB.shutupdrive;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

/**
 * Created by kyle on 5/29/15.
 */
public class DetectedActivityIntentService extends IntentService {

    private LocalBroadcastManager broadcastManager;

    public DetectedActivityIntentService(String name) {
        super(name);
    }

    public DetectedActivityIntentService() {
        super("Activity Recognition");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        broadcastManager = LocalBroadcastManager.getInstance(this);
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result =
                    ActivityRecognitionResult.extractResult(intent);
            DetectedActivity mostProbableActivity = result.getMostProbableActivity();
            int confidence = mostProbableActivity.getConfidence();
            int activityType = mostProbableActivity.getType();
            if (confidence >= 75 && activityType == DetectedActivity.IN_VEHICLE) {
                startService(new Intent(this, CarMode.class));
            } else if (confidence >= 75){
                stopService(new Intent(this, CarMode.class));
            }

        }
    }

}
