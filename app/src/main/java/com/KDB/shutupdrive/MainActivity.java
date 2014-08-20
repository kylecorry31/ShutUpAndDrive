package com.KDB.shutupdrive;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.KDB.shutupdrive.ActivityUtils.REQUEST_TYPE;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.purplebrain.adbuddiz.sdk.AdBuddiz;

public class MainActivity extends ActionBarActivity implements OnClickListener {
    // Author - Kyle Corry(programmer, design), Dylan Kiley(design, research and idea),
    // Brian Thornber(design, research and idea), Arianna Hatcher(research)

    private AdView adView;
    private Button btn;
    private TextView tv;
    private ImageView img;
    // Store the current request type (ADD or REMOVE)
    private REQUEST_TYPE mRequestType;
    // The activity recognition update request object
    private DetectionRequester mDetectionRequester;
    // The activity recognition update removal object
    private DetectionRemover mDetectionRemover;
    boolean activityRecognition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        AdBuddiz.setPublisherKey("db90cfe0-28ed-43fc-8a81-88f83cffead1");
        AdBuddiz.cacheAds(this);
        adView = (AdView) findViewById(R.id.adView);
        AdRequest.Builder adRequest = new AdRequest.Builder();
        adView.loadAd(adRequest.build());
        AdBuddiz.showAd(this);
        //this is for the developers
        //adView.setVisibility(View.GONE);
        img = (ImageView) findViewById(R.id.car);
        btn = (Button) findViewById(R.id.button);
        tv = (TextView) findViewById(R.id.tv);
        activityRecognition();
        activityRecognitionRunning();
        if (activityRecognition) {
            mDetectionRequester = new DetectionRequester(this);
            mDetectionRemover = new DetectionRemover(this);
        }
        if (!isServiceRunning() && !activityRecognition) {
            if (gps())
                startService(new Intent(getBaseContext(), SpeedService.class));
            else
                gpsDialog();
        } else if (!isServiceRunning() && activityRecognition) {
            onStartUpdates();
            Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
            btn.setBackgroundColor(getResources().getColor(R.color.blue));
            btn.setText(getResources().getString(R.string.activated));
            tv.setText(getResources().getString(R.string.tap_deactivate));
            img.setImageResource(R.drawable.car_blue);
        }

        if (isServiceRunning()) {
            btn.setBackgroundColor(getResources().getColor(R.color.blue));
            btn.setText(getResources().getString(R.string.activated));
            tv.setText(getResources().getString(R.string.tap_deactivate));
            img.setImageResource(R.drawable.car_blue);
        }
        btn.setOnClickListener(this);

    }

    private void activityRecognition() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        activityRecognition = prefs.getBoolean("activityRecognition", false);
        if (!activityRecognition) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("activityRecognitionRunning", false);
            editor.apply();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adView.destroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        adView.resume();
        activityRecognition();
        activityRecognitionRunning();
    }

    @Override
    protected void onPause() {
        super.onPause();
        adView.pause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                moveTaskToBack(true);
                return true;
        }
        return false;
    }

    // this is for an options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // settings menu
                Intent openSettings = new Intent("com.KDB.shutupdrive.PREFS");
                startActivity(openSettings);
                break;
            case R.id.action_tutorial:
                Intent openTut = new Intent(this, Tutorial1.class);
                startActivity(openTut);
                break;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        if (isServiceRunning()) {
            btn.setBackgroundColor(getResources().getColor(R.color.red));
            btn.setText(getResources().getString(R.string.not_activated));
            tv.setText(getResources().getString(R.string.tap_activate));
            img.setImageResource(R.drawable.car_red);
            if (activityRecognition) {
                onStopUpdates();
                Toast.makeText(this, "Service Stopped", Toast.LENGTH_SHORT).show();

            } else {
                stopService(new Intent(getBaseContext(), SpeedService.class));
            }
        } else {
            if (activityRecognition) {
                btn.setBackgroundColor(getResources().getColor(R.color.blue));
                btn.setText(getResources().getString(R.string.activated));
                tv.setText(getResources().getString(R.string.tap_deactivate));
                img.setImageResource(R.drawable.car_blue);
                Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
                onStartUpdates();
            } else if (gps()) {
                btn.setBackgroundColor(getResources().getColor(R.color.blue));
                btn.setText(getResources().getString(R.string.activated));
                tv.setText(getResources().getString(R.string.tap_deactivate));
                img.setImageResource(R.drawable.car_blue);
                startService(new Intent(getBaseContext(), SpeedService.class));
            } else
                gpsDialog();
        }
    }

    /**
     * activity recognition start
     */
    public void onStartUpdates() {
        if (!servicesConnected()) {
            return;
        }
        mRequestType = ActivityUtils.REQUEST_TYPE.ADD;

        mDetectionRequester.requestUpdates();
    }

    /**
     * activity recognition stop
     */
    public void onStopUpdates() {
        if (!servicesConnected()) {
            return;
        }
        mRequestType = ActivityUtils.REQUEST_TYPE.REMOVE;
        if (mDetectionRequester.getRequestPendingIntent() != null)
            mDetectionRemover.removeUpdates(mDetectionRequester.getRequestPendingIntent());
        if (mDetectionRequester != null) {
            if (mDetectionRequester.getRequestPendingIntent() != null)
                mDetectionRequester.getRequestPendingIntent().cancel();
        }
        SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = getPrefs.edit();
        editor.putBoolean("activityRecognitionRunning", false);
        editor.apply();
    }

    /**
     * activity recognition google play services
     */
    private boolean servicesConnected() {
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == resultCode) {
            Log.d(ActivityUtils.APPTAG, "Google Play Services Available");
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0).show();
            return false;
        }
    }

    /*
    * receive activity recognition stuff
    */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case ActivityUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        if (ActivityUtils.REQUEST_TYPE.ADD == mRequestType) {
                            mDetectionRequester.requestUpdates();
                        } else if (ActivityUtils.REQUEST_TYPE.REMOVE == mRequestType) {
                            mDetectionRemover.removeUpdates(
                                    mDetectionRequester.getRequestPendingIntent());
                        }
                        break;
                    default:
                        Log.d(ActivityUtils.APPTAG, "no resolution");
                }
            default:
                Log.d(ActivityUtils.APPTAG,
                        "unknown request code " + requestCode);
                break;
        }
    }

    private boolean isServiceRunning() {
        activityRecognitionRunning();
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (SpeedService.class.getName().equals(service.service.getClassName()) || ActivityRecognitionIntentService.class.getName().equals(service.service.getClassName()) || ActivityUtils.activityRecognitionRunning) {
                return true;
            }
        }
        return false;
    }

    private void activityRecognitionRunning() {
        SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        ActivityUtils.activityRecognitionRunning = getPrefs.getBoolean("activityRecognitionRunning", false);
    }

    private boolean gps() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void gpsDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled. Shut Up & Drive! requires the GPS to be enabled.").setCancelable(false).setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(callGPSSettingIntent);
                    }
                });
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

}