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

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
    Tracker tracker;
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFirst();
        setContentView(R.layout.layout_main);
        tracker = ((MyApplication) getApplication()).tracker;
        tracker.setScreenName("Main Screen");
        fab = (FloatingActionButton) findViewById(R.id.fab);
        statusText = (TextView) findViewById(R.id.status);
        mottoText = (TextView) findViewById(R.id.motto);
        descText = (TextView) findViewById(R.id.desc);
        titleImage = (ImageView) findViewById(R.id.titleImage);
        adView = (AdView) findViewById(R.id.adView);
        createAds();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        running = prefs.getBoolean("Running", false);
        toast = false;
        buildGoogleApiClient();
    }

    private void createAds(){
        if (!Constants.DEVELOPER) {
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        } else {
            adView.setVisibility(View.GONE);
        }
    }

    private boolean isFirst() {
        SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean first = getPrefs.getBoolean("firstTime", true);
        if (first) {
            getPrefs.edit().putBoolean("firstTime", false).apply();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        this.item = item;
        animateOut();
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
    }

    @Override
    public void onResult(Status status) {
        if (status.isSuccess()) {
            if (toast)
                Toast.makeText(this, running ? "Started" : "Stopped", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        toast = true;
        if (mGoogleApiClient.isConnected() && !running) {
            startActivityRecognition();
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Control")
                    .setAction("click")
                    .setLabel("Start service")
                    .build());
            prefs.edit().putLong("NotDrivingTime", 0).apply();
        } else if (mGoogleApiClient.isConnected() && running) {
            stopActivityRecognition();
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Control")
                    .setAction("click")
                    .setLabel("Stop service")
                    .build());
            prefs.edit().putLong("NotDrivingTime", 0).apply();
        } else
            Toast.makeText(this, "No connection to Google Play Services", Toast.LENGTH_SHORT).show();
    }


}
