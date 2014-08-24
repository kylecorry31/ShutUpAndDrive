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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.purplebrain.adbuddiz.sdk.AdBuddiz;

public class MainActivity extends ActionBarActivity implements OnClickListener {
    // Author - Kyle Corry(programmer, design), Dylan Kiley(design, research and idea),
    // Brian Thornber(design, research and idea), Arianna Hatcher(research)

    private AdView adView;
    private Button btn;
    private TextView tv;
    private ImageView img;
    private final String deactivated = ActivityUtils.DEACTIVATION;
    String number = "";
    AlarmManager alarmManager;
    PendingIntent pendingIntent;
    SharedPreferences getPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        getPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        AdBuddiz.setPublisherKey(ActivityUtils.PUB_KEY);
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
        if (!alarmRunning()) {
            if (gps())
                startAlarm();
            else
                gpsDialog();
        }
        if (alarmRunning()) {
            btn.setBackgroundColor(getResources().getColor(R.color.blue));
            btn.setText(getResources().getString(R.string.activated));
            tv.setText(getResources().getString(R.string.tap_deactivate));
            img.setImageResource(R.drawable.car_blue);
        }
        btn.setOnClickListener(this);

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
        bootStart();

    }

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
        //turn off
        if (alarmRunning()) {
            btn.setBackgroundColor(getResources().getColor(R.color.red));
            btn.setText(getResources().getString(R.string.not_activated));
            tv.setText(getResources().getString(R.string.tap_activate));
            img.setImageResource(R.drawable.car_red);
            cancelAlarm();
        } else {
            //turn on
            if (gps()) {
                btn.setBackgroundColor(getResources().getColor(R.color.blue));
                btn.setText(getResources().getString(R.string.activated));
                tv.setText(getResources().getString(R.string.tap_deactivate));
                img.setImageResource(R.drawable.car_blue);
                startAlarm();
            } else
                gpsDialog();
        }
    }

    private void startAlarm() {
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent gpsIntent = new Intent(this, GPSAlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, gpsIntent, 0);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), frequencyMins() * ActivityUtils.MILLIS_IN_MINUTE, pendingIntent);
        SharedPreferences.Editor editor = getPrefs.edit();
        editor.putBoolean("alarmRunning", true);
        editor.apply();
        //let the user know the service was started
        Toast.makeText(this, getResources().getString(R.string.service_start), Toast.LENGTH_SHORT).show();
    }

    private void cancelAlarm() {
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            alarmManager = null;
        }
        SharedPreferences.Editor editor = getPrefs.edit();
        editor.putBoolean("alarmRunning", false);
        editor.apply();

        stopService(new Intent(this, CarMode.class));
        if (!number.isEmpty()) {
            //get the sms manager to send text
            SmsManager smsManager = SmsManager.getDefault();
            //send message
            smsManager.sendTextMessage(number, null, deactivated,
                    null, null);
        }
        //let the user know the service was stopped
        Toast.makeText(this, getResources().getString(R.string.service_stop), Toast.LENGTH_SHORT).show();
    }

    private boolean alarmRunning() {
        number = getPrefs.getString("number", "");
        return getPrefs.getBoolean("alarmRunning", false);
    }

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
        alertDialogBuilder.setMessage(getResources().getString(R.string.gps_needed)).setCancelable(false).setPositiveButton("Settings",
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