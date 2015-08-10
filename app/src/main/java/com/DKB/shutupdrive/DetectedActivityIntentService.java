package com.DKB.shutupdrive;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

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
            long waitTime = Utils.getNotDrivingTime(this);
            boolean diffTimeOK = (new Date().getTime() - waitTime) >= Utils.minutesToMillis(10);
            if (confidence >= Utils.DETECTION_THRESHOLD && activityType == DetectedActivity.IN_VEHICLE && diffTimeOK) {
                /*
                    if gps
                        check permission location
                            else: start carmode, notify
                 */
                if (Utils.getGPS(this))
                    startService(new Intent(this, SpeedService.class));
                else
                    startService(new Intent(this, CarMode.class));
            } else if (confidence >= Utils.DETECTION_THRESHOLD) {
                stopService(new Intent(this, CarMode.class));
                Utils.setGPSDrive(this, false);
            }

        }
    }
}
