package com.KDB.shutupdrive;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class MainActivity extends Activity implements OnClickListener {
    // Author - Kyle Corry(programmer), Dylan Kiley(design, research and idea),
    // Brian Thornber(design, research and idea), Arianna Hatcher(research)

    AdView adView;
    Button start, stop;
    ImageButton mainBtn;
    TextView activeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        adView = (AdView) findViewById(R.id.adView);
        AdRequest.Builder adRequest = new AdRequest.Builder();
        adView.loadAd(adRequest.build());
        /*start = (Button) findViewById(R.id.startButton);
        stop = (Button) findViewById(R.id.stopButton);
		start.setOnClickListener(this);
		stop.setOnClickListener(this);*/
        mainBtn = (ImageButton) findViewById(R.id.mainButton);
        activeView = (TextView) findViewById(R.id.activeView);
        if (isServiceRunning()) {
            mainBtn.setImageResource(R.drawable.pb_active_blue);
            activeView.setText("Activated");
        }
        mainBtn.setOnClickListener(this);
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
            mainBtn.setImageResource(R.drawable.pb_not_active_gray);
            stopService(new Intent(getBaseContext(), SpeedService.class));
            activeView.setText("Not Activated");
        } else {
            mainBtn.setImageResource(R.drawable.pb_active_blue);
            startService(new Intent(getBaseContext(), SpeedService.class));
            activeView.setText("Activated");
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

}