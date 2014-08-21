package com.KDB.shutupdrive;


public final class ActivityUtils {

    // Used to track what type of request is in process
    public enum REQUEST_TYPE {
        ADD, REMOVE
    }

    public static final String APPTAG = "MainActivity";
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
    public static final String PUB_KEY = "db90cfe0-28ed-43fc-8a81-88f83cffead1";
    public static final String DEFAULT_MSG = "I am driving right now, I will contact you later --Auto reply message--";
    public static final String DEACTIVATION = "Shut Up & Drive! has been deactivated!";
    public static final String SPEED_MONITOR = "Shut Up & Drive is monitoring your speed";
    public static final String RUNNING = "Shut Up & Drive is running";
    public static final double KM_TO_MILES = 2.23694;
    public static final int MIN_SPEED = 10;
    public static final int MAX_SPEED = 100;
    public static final int NOTIFICATION_ID = 753815731;


}
