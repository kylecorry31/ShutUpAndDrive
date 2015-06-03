package com.DKB.shutupdrive;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;


/**
 * Created by kyle on 5/30/15.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    GoogleApiClient mGoogleApiClient;
    FloatingActionButton fab;
    TextView statusText, mottoText, descText;
    ImageView titleImage;
    boolean running;
    boolean toast;
    SharedPreferences prefs;
    MenuItem item;
    private static final String FILENAME = "firstTime";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFirst();
        setContentView(R.layout.layout_main);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        statusText = (TextView) findViewById(R.id.status);
        mottoText = (TextView) findViewById(R.id.motto);
        descText = (TextView) findViewById(R.id.desc);
        titleImage = (ImageView) findViewById(R.id.titleImage);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        running = prefs.getBoolean("Running", false);
        toast = false;
        buildGoogleApiClient();
    }

    private boolean isFirst() {
        SharedPreferences getPrefs;
        getPrefs = getSharedPreferences(FILENAME, 0);
        boolean first = getPrefs.getBoolean("firstTime", true);
        if (first) {
            SharedPreferences.Editor editor = getPrefs.edit();
            editor.putBoolean("firstTime", false);
            editor.apply();
            Intent intent = new Intent(getApplicationContext(), Splash.class);
            startActivity(intent);
            return true;
        } else {
            return false;
        }
    }


    protected void setUpUI() {

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Animation slideDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_slide_in_top);
            slideDown.setStartOffset(275);
            titleImage.startAnimation(slideDown);
        }
        Animation slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_slide_in_bottom);
        slideUp.setStartOffset(250);
        Animation fadeIn = AnimationUtils.loadAnimation(getBaseContext(), R.anim.abc_fade_in);
        fadeIn.setStartOffset(250);
        fab.startAnimation(slideUp);
        statusText.startAnimation(fadeIn);
        descText.startAnimation(fadeIn);
        mottoText.startAnimation(fadeIn);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .build();
    }


    @Override
    protected void onStart() {
        super.onStart();
        setUpUI();
        mGoogleApiClient.connect();

    }

    @Override
    protected void onResume() {
        super.onResume();
        fab.setOnClickListener(this);
    }

    @Override
    protected void onPause() {
        fab.setOnClickListener(null);
        super.onPause();
    }

    @Override
    protected void onStop() {
        // stopActivityRecognition();
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    protected void startActivityRecognition() {
        running = true;
        fab.setImageResource(R.drawable.ic_stop_white_24dp);
        prefs.edit().putBoolean("Running", true).apply();
        statusText.setText(getString(R.string.on));
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                mGoogleApiClient,
                Constants.DETECTION_INTERVAL,
                getActivityRecognitionPI()
        )
                .setResultCallback(this);
    }

    // this is for an options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        if (Constants.DEVELOPER) {
            menu.add("Clear Log");
            menu.add("Show Log");
        }
        return true;
    }

    // This determines which dropdown value was touched
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        this.item = item;
        if (item.getTitle().equals("Clear Log")) {
            File logFile = new File("logfile.txt");
            logFile.delete();
            Log.d("MainActivity", "Log Cleared");
        } else if(item.getTitle().equals("Show Log")){
            try {
                FileInputStream inputStream = openFileInput("logfile.txt");
                StringBuilder sb = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                Log.d("Activity Recognition", sb.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            animateOut();
        }
        return false;
    }

    protected void animateOut() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Animation slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_slide_out_top);
            slideUp.setFillAfter(true);
            titleImage.startAnimation(slideUp);
        }
        Animation slideDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_slide_out_bottom);
        slideDown.setFillAfter(true);
        Animation fadeOut = AnimationUtils.loadAnimation(getBaseContext(), R.anim.abc_fade_out);
        fadeOut.setFillAfter(true);
        fab.startAnimation(slideDown);
        statusText.startAnimation(fadeOut);
        descText.startAnimation(fadeOut);
        mottoText.startAnimation(fadeOut);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                switch (item.getItemId()) {
                    case R.id.action_settings:
                        // settings menu
                        Intent openSettings = new Intent(getApplicationContext(), Settings.class);
                        startActivity(openSettings);
                        break;
                    case R.id.action_tutorial:
                        // Tutorial
                        Intent openTut = new Intent(getApplicationContext(), Tutorial.class);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt(Constants.TUT_NUM_KEY, 0);
                        editor.apply();
                        startActivity(openTut);
                        finish();
                        break;
                }
            }
        }, fadeOut.getDuration());
    }

    protected void stopActivityRecognition() {
        running = false;
        fab.setImageResource(R.drawable.ic_play_arrow_white_24dp);
        prefs.edit().putBoolean("Running", false).apply();
        statusText.setText(getString(R.string.off));
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                mGoogleApiClient,
                getActivityRecognitionPI()
        )
                .setResultCallback(this);
    }


    @Override
    public void onConnected(Bundle bundle) {
        if (running || prefs.getBoolean("autoStart", false)) {
            toast = false;
            startActivityRecognition();
        }
        Log.d(Constants.TAG, Constants.SERVICES_CONNECTED);
    }

    private PendingIntent getActivityRecognitionPI() {
        Intent intent = new Intent(this, DetectedActivityIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(getBaseContext(), "Google Play Services Unavailable", Toast.LENGTH_SHORT).show();
        Log.e(Constants.TAG, Constants.SERVICES_FAILED);
    }

    @Override
    public void onResult(Status status) {
        if (status.isSuccess()) {
            Log.d(Constants.TAG, "Successful");
            if (toast)
                Toast.makeText(this, running ? "Started" : "Stopped", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        toast = true;
        if (mGoogleApiClient.isConnected() && !running)
            startActivityRecognition();
        else if (mGoogleApiClient.isConnected() && running)
            stopActivityRecognition();
        else
            Toast.makeText(this, "No connection to Google Play Services", Toast.LENGTH_SHORT).show();
    }


}
