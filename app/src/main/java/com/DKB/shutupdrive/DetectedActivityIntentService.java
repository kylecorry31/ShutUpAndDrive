package com.DKB.shutupdrive;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

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
            if (confidence >= 75 && activityType == DetectedActivity.IN_VEHICLE) {
                startService(new Intent(this, CarMode.class));
            } else if (confidence >= 75) {
                stopService(new Intent(this, CarMode.class));
            }

        }
    }

}
