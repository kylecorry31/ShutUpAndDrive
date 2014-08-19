package com.KDB.shutupdrive;


public final class ActivityUtils {

    // Used to track what type of request is in process
    public enum REQUEST_TYPE {
        ADD, REMOVE
    }

    public static final String APPTAG = "ActivitySample";
    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    // Constants used to establish the activity update interval
    public static final int MILLISECONDS_PER_SECOND = 1000;

    //public static final int DETECTION_INTERVAL_SECONDS = 120;

    public static final int SECONDS_PER_MINUTE = 60;

    public static final int DETECTION_INTERVAL_MINUTES = 10;

    public static final int DETECTION_INTERVAL_MILLISECONDS =
            MILLISECONDS_PER_SECOND * SECONDS_PER_MINUTE * DETECTION_INTERVAL_MINUTES;
    public static boolean activityRecognitionRunning = false;

}
