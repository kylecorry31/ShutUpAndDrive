package com.DKB.shutupdrive;

import android.app.PendingIntent;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.ShareActionProvider;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;


/**
 * Created by kyle on 5/30/15.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    private GoogleApiClient mGoogleApiClient;
    private FloatingActionButton fab;
    private TextView statusText;
    private TextView mottoText;
    private TextView descText;
    private ImageView titleImage;
    private boolean running;
    private boolean toast;
    private MenuItem item;
    private AdView adView;


    private static final String PLAY_STORE_LINK = "https://play.google.com/store/apps/details?id=com.DKB.shutupdrive";
    private static final String PREVENT = "You can too by downloading “Shut Up & Drive!” for Android at ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        statusText = (TextView) findViewById(R.id.status);
        mottoText = (TextView) findViewById(R.id.motto);
        descText = (TextView) findViewById(R.id.desc);
        titleImage = (ImageView) findViewById(R.id.titleImage);
        adView = (AdView) findViewById(R.id.adView);
        createAds();
        running = Utils.isRunning(this);
        toast = false;
        buildGoogleApiClient();
        startService(new Intent(this, CarMode.class));
    }

    private void createAds() {
        if (!Utils.DEVELOPER) {
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        } else {
            adView.setVisibility(View.GONE);
        }
    }


    private void setUpUI() {
        slideDownTitleImage();
        showFAB();
        fadeInText();
    }

    private void slideDownTitleImage() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Animation slideDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_slide_in_top);
            slideDown.setStartOffset(275);
            titleImage.startAnimation(slideDown);
        }
    }

    private void showFAB() {
        Animation fabIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.design_fab_in);
        fabIn.setStartOffset(250);
        fabIn.setDuration(250);
        fab.startAnimation(fabIn);
    }

    private void fadeInText() {
        Animation fadeIn = AnimationUtils.loadAnimation(getBaseContext(), R.anim.abc_fade_in);
        fadeIn.setStartOffset(250);
        statusText.startAnimation(fadeIn);
        descText.startAnimation(fadeIn);
        mottoText.startAnimation(fadeIn);
    }

    private synchronized void buildGoogleApiClient() {
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
        fab.show();
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


    private void startActivityRecognition() {
        running = true;
        fab.setImageResource(R.drawable.ic_stop_white_24dp);
        fab.setContentDescription(getString(R.string.stop_button));
        Utils.setRunning(this, true);
        statusText.setText(getString(R.string.on));
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                mGoogleApiClient,
                Utils.DETECTION_INTERVAL,
                getActivityRecognitionPI()
        )
                .setResultCallback(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (Utils.DEVELOPER)
            menu.add(getString(R.string.tutorial));
        MenuItem item = menu.findItem(R.id.action_share);
        ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, "I’ve driven without distractions for " +
                (int) Math.round(Utils.millisToHours(Utils.getTotalTime(this)))
                + " hours!\n\n" + PREVENT + PLAY_STORE_LINK);
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(i);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        this.item = item;
        animateOut();
        return false;
    }

    private void animateOut() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Animation slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_slide_out_top);
            slideUp.setFillAfter(true);
            titleImage.startAnimation(slideUp);
        }
        Animation fadeOut = AnimationUtils.loadAnimation(getBaseContext(), R.anim.abc_fade_out);
        fadeOut.setFillAfter(true);
        fab.hide();
        statusText.startAnimation(fadeOut);
        descText.startAnimation(fadeOut);
        mottoText.startAnimation(fadeOut);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (item.getTitle().equals(getString(R.string.tutorial))) {
                    // Tutorial
                    Intent openTut = new Intent(getApplicationContext(), Tutorial.class);
                    startActivity(openTut);
                    finish();
                } else if (item.getItemId() == R.id.action_settings) {
                    // settings menus
                    Intent openSettings = new Intent(getApplicationContext(), Settings.class);
                    startActivity(openSettings);
                }
            }
        }, fadeOut.getDuration());
    }

    private void stopActivityRecognition() {
        running = false;
        fab.setImageResource(R.drawable.ic_play_arrow_white_24dp);
        fab.setContentDescription(getString(R.string.start_button));
        Utils.setRunning(this, false);
        statusText.setText(getString(R.string.off));
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                mGoogleApiClient,
                getActivityRecognitionPI()
        )
                .setResultCallback(this);
    }


    @Override
    public void onConnected(Bundle bundle) {
        if (running) {
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
        Toast.makeText(getBaseContext(), getString(R.string.common_google_play_services_unsupported_text), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResult(Status status) {
        if (status.isSuccess() && toast)
            Toast.makeText(this, running ? getString(R.string.started) : getString(R.string.stopped), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        toast = true;
        if (mGoogleApiClient.isConnected() && !running) {
            startActivityRecognition();
            UserSettings.setNotDrivingTime(this, 0);
            UserSettings.setGPSDrive(this, false);
        } else if (mGoogleApiClient.isConnected()) {
            stopActivityRecognition();
            stopService(new Intent(this, CarMode.class));
            UserSettings.setNotDrivingTime(this, 0);
            UserSettings.setGPSDrive(this, false);
        } else
            Toast.makeText(this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();

    }

}
