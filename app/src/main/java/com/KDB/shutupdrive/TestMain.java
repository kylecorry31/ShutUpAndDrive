package com.KDB.shutupdrive;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.melnykov.fab.FloatingActionButton;
import com.purplebrain.adbuddiz.sdk.AdBuddiz;

public class TestMain extends ActionBarActivity implements OnClickListener {
    // Author - Kyle Corry(programmer, design), Dylan Kiley(design, research and idea),
    // Brian Thornber(design, research and idea), Arianna Hatcher(research)
    private AdView adView;
    private FloatingActionButton btn;
    private TextView tv;
    private final String deactivated = ActivityUtils.DEACTIVATION;
    String number = "";
    AlarmManager alarmManager;
    PendingIntent pendingIntent;
    SharedPreferences getPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_layout);
        getPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        adView = (AdView) findViewById(R.id.adView);
        if (!ActivityUtils.DEVELOPER_EDITION) {
            // Full screen ads will appear around 60% of the time
            if (Math.random() < 0.6) {
                AdBuddiz.setPublisherKey(ActivityUtils.PUB_KEY);
                AdBuddiz.cacheAds(this);
                AdBuddiz.showAd(this);
            }
            // This sets up the adview

            AdRequest.Builder adRequest = new AdRequest.Builder();
            adView.loadAd(adRequest.build());
        }
        if (ActivityUtils.DEVELOPER_EDITION)
            adView.setVisibility(View.GONE);
        // This is for the floating action button on the bottom
        btn = (FloatingActionButton) findViewById(R.id.fab);
        // Ths is the textView at the bottom with the status of the service
        tv = (TextView) findViewById(R.id.status);
        if (!alarmRunning()) {
            if (gps()) {
                // Starts service if gps is on, else lets the user know that they need GPS on
                startAlarm();
                //let the user know the service was started
                Toast.makeText(this, getResources().getString(R.string.service_start), Toast.LENGTH_SHORT).show();
            } else {
                gpsDialog();
            }
        }
        // If the service was already running, set the textview to on
        if (alarmRunning()) {
            // btn.setColor(getResources().getColor(R.color.blue));
            tv.setText("ON");
        }
        btn.setOnClickListener(this);

    }

    // called when app is closed, stops ads
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!ActivityUtils.DEVELOPER_EDITION)
            adView.destroy();
    }

    //This is called after returning from the settings menu
    @Override
    protected void onResume() {
        super.onResume();
        if (!ActivityUtils.DEVELOPER_EDITION)
            adView.resume();
        bootStart();
        Log.d("Resume", "Resumed");
        // Checks if the GPS frequency was changed, and restarts the service if it was
        if (getPrefs.getBoolean("gpsChange", false) && alarmRunning()) {
            cancelAlarm(true);
            Log.d("Main Activity", Integer.toString(frequencyMins()));
            startAlarm();
            SharedPreferences.Editor editor = getPrefs.edit();
            editor.putBoolean("gpsChange", false);
            editor.apply();
        }

    }

    // This checks if the app should start once the phone is rebooted
    private void bootStart() {
        boolean boot = getPrefs.getBoolean("bootStart", false);
        ComponentName receiver = new ComponentName(this, BootCompletedReceiver.class);
        PackageManager packageManager = getPackageManager();
        if (boot) {
            packageManager.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        } else {
            packageManager.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!ActivityUtils.DEVELOPER_EDITION)
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
        if(ActivityUtils.DEVELOPER_EDITION) {
            MenuItem item = menu.add("Enable Drive Mode");
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    Intent carmode = new Intent(getBaseContext(), CarMode.class);
                    startService(carmode);
                    return true;
                }
            });
        }
        return true;
    }

    // This determines which dropdown value was touched
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // settings menu
                Intent openSettings = new Intent("com.KDB.shutupdrive.SETTINGS");
                startActivity(openSettings);
                break;
            case R.id.action_tutorial:
                // Tutorial
                Intent openTut = new Intent(this, Tutorial1.class);
                startActivity(openTut);
                break;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        //turn off
        if (alarmRunning()) {
            tv.setText("OFF");
            cancelAlarm(false);
        } else {
            //turn on
            if (gps()) {

                tv.setText("ON");
                startAlarm();
                //let the user know the service was started
                Toast.makeText(this, getResources().getString(R.string.service_start), Toast.LENGTH_SHORT).show();
            } else
                gpsDialog();
        }
    }

    // I use the repeating alarm to check speed at a set interval, this greatly reduces the amount of battery used
    private void startAlarm() {
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent gpsIntent = new Intent(this, GPSAlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, gpsIntent, 0);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), frequencyMins() * ActivityUtils.MILLIS_IN_MINUTE, pendingIntent);
        SharedPreferences.Editor editor = getPrefs.edit();
        // Stores that the alarm is running
        editor.putBoolean("alarmRunning", true);
        editor.apply();

    }

    // Cancels the service
    private void cancelAlarm(boolean self) {
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            alarmManager = null;
        }
        SharedPreferences.Editor editor = getPrefs.edit();
        editor.putBoolean("alarmRunning", false);
        editor.apply();
        stopService(new Intent(this, CarMode.class));
        number = getPrefs.getString("number", "");
        // Deactivation notification
        if (!number.isEmpty() && !self) {

            //get the sms manager to send text
            SmsManager smsManager = SmsManager.getDefault();
            //send message
            smsManager.sendTextMessage(number, null, deactivated,
                    null, null);
        }
        //let the user know the service was stopped
        if (!self)
            Toast.makeText(this, getResources().getString(R.string.service_stop), Toast.LENGTH_SHORT).show();
    }

    // Checks to see if the service is running
    private boolean alarmRunning() {
        return getPrefs.getBoolean("alarmRunning", false);
    }

    // Gets the gps frequency
    private int frequencyMins() {
        String gpsTime = getPrefs.getString("gps", "10");
        return Integer.valueOf(gpsTime);
    }

    //determines if gps is on
    private boolean gps() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    //this is shown if the user does not have gps on
    private void gpsDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(getResources().getString(R.string.gps_needed)).setCancelable(false).setPositiveButton("SETTINGS",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(callGPSSettingIntent);
                    }
                });
        alertDialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

}