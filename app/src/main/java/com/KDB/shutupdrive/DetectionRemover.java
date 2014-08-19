package com.KDB.shutupdrive;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.ActivityRecognitionClient;

public class DetectionRemover
        implements ConnectionCallbacks, OnConnectionFailedListener {

    private Context mContext;

    private ActivityRecognitionClient mActivityRecognitionClient;

    private PendingIntent mCurrentIntent;

    public DetectionRemover(Context context) {
        mContext = context;
        mActivityRecognitionClient = null;

    }

    public void removeUpdates(PendingIntent requestIntent) {
        SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = getPrefs.edit();
        editor.putBoolean("activityRecognitionRunning", false);
        editor.apply();
        mContext.stopService(new Intent(mContext, CarMode.class));
        mCurrentIntent = requestIntent;
        requestConnection();
    }

    private void requestConnection() {
        getActivityRecognitionClient().connect();
    }

    public ActivityRecognitionClient getActivityRecognitionClient() {

        if (mActivityRecognitionClient == null) {
            setActivityRecognitionClient(new ActivityRecognitionClient(mContext, this, this));
        }
        return mActivityRecognitionClient;
    }

    private void requestDisconnection() {

        getActivityRecognitionClient().disconnect();
        Log.d("Detection Remover", "Disconnected");
        setActivityRecognitionClient(null);
    }

    public void setActivityRecognitionClient(ActivityRecognitionClient client) {
        mActivityRecognitionClient = client;

    }

    @Override
    public void onConnected(Bundle connectionData) {
        // If debugging, log the connection
        Log.d(ActivityUtils.APPTAG, "connected to google play services");
        // Send a request to Location Services to remove activity recognition updates
        continueRemoveUpdates();
    }

    /**
     * Once the connection is available, send a request to remove activity recognition updates.
     */
    private void continueRemoveUpdates() {

        // Remove the updates
        if (mActivityRecognitionClient != null && mCurrentIntent != null)
            mActivityRecognitionClient.removeActivityUpdates(mCurrentIntent);
        
        /*
         * Cancel the PendingIntent. This stops Intents from arriving at the IntentService, even if
         * request fails. 
         */
        if (mCurrentIntent != null)
            mCurrentIntent.cancel();

        // Disconnect the client
        requestDisconnection();

        Log.d("Detection Remover", "Removed activity recognition updates");
    }

    /*
     * Called by Location Services once the activity recognition client is disconnected.
     */
    @Override
    public void onDisconnected() {

        // In debug mode, log the disconnection
        Log.d(ActivityUtils.APPTAG, "disconnected from google play services");

        // Destroy the current activity recognition client
        mActivityRecognitionClient = null;
    }

    /*
     * Implementation of OnConnectionFailedListener.onConnectionFailed
     * If a connection or disconnection request fails, report the error
     * connectionResult is passed in from Location Services
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {

            try {
                connectionResult.startResolutionForResult((Activity) mContext,
                        ActivityUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

            /*
             * Thrown if Google Play services canceled the original
             * PendingIntent
             */
            } catch (SendIntentException e) {
                // display an error or log it here.
            }

        /*
         * If no resolution is available, display Google
         * Play service error dialog. This may direct the
         * user to Google Play Store if Google Play services
         * is out of date.
         */
        } else {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                    connectionResult.getErrorCode(),
                    (Activity) mContext,
                    ActivityUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
            if (dialog != null) {
                dialog.show();
            }
        }
    }
}
