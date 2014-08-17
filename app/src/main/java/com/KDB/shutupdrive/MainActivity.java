package com.KDB.shutupdrive;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.purplebrain.adbuddiz.sdk.AdBuddiz;

public class MainActivity extends ActionBarActivity implements OnClickListener {
    // Author - Kyle Corry(programmer, design), Dylan Kiley(design, research and idea),
    // Brian Thornber(design, research and idea), Arianna Hatcher(research)

    AdView adView;
    Button start, stop;
    // ImageButton mainBtn;
    Button btn;
    TextView tv;
    ImageView img;
    //TextView activeView;

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
        if (!isServiceRunning()) {
            if (gps())
                startService(new Intent(getBaseContext(), SpeedService.class));
            else
                gpsDialog();
        }
        img = (ImageView) findViewById(R.id.car);
        btn = (Button) findViewById(R.id.button);
        tv = (TextView) findViewById(R.id.tv);
        //mainBtn = (ImageButton) findViewById(R.id.mainButton);
        //activeView = (TextView) findViewById(R.id.activeView);
        if (isServiceRunning()) {
            btn.setBackgroundColor(getResources().getColor(R.color.blue));
            btn.setText(getResources().getString(R.string.activated));
            tv.setText(getResources().getString(R.string.tap_deactivate));
            img.setImageResource(R.drawable.car_blue);
            //mainBtn.setImageResource(R.drawable.pb_active_blue);
            //activeView.setText("Activated");
        }
        btn.setOnClickListener(this);
        // mainBtn.setOnClickListener(this);

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
                Intent openGPSsettings = new Intent("com.KDB.shutupdrive.PREFS");
                startActivity(openGPSsettings);
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
        /*switch (v.getId()) {
        case R.id.startButton:
			startService(new Intent(getBaseContext(), SpeedService.class));
			break;
		case R.id.stopButton:
			stopService(new Intent(getBaseContext(), SpeedService.class));
			break;
		}*/
        if (isServiceRunning()) {
            // mainBtn.setImageResource(R.drawable.pb_not_active_gray);
            btn.setBackgroundColor(getResources().getColor(R.color.red));
            btn.setText(getResources().getString(R.string.not_activated));
            tv.setText(getResources().getString(R.string.tap_activate));
            img.setImageResource(R.drawable.car_red);
            stopService(new Intent(getBaseContext(), SpeedService.class));
            //activeView.setText("Not Activated");
        } else {
            if (gps()) {
                //mainBtn.setImageResource(R.drawable.pb_active_blue);
                btn.setBackgroundColor(getResources().getColor(R.color.blue));
                btn.setText(getResources().getString(R.string.activated));
                tv.setText(getResources().getString(R.string.tap_deactivate));
                img.setImageResource(R.drawable.car_blue);
                startService(new Intent(getBaseContext(), SpeedService.class));
                // activeView.setText("Activated");
            } else
                gpsDialog();

        }
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (SpeedService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private boolean gps() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return true;
        } else {
            return false;
        }
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