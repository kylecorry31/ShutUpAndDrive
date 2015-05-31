package com.KDB.shutupdrive;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
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

import java.util.Random;


/**
 * Created by kyle on 5/30/15.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    GoogleApiClient mGoogleApiClient;
    FloatingActionButton fab;
    TextView statusText, mottoText, descText;
    Random random;
    ImageView titleImage;
    boolean running;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        statusText = (TextView) findViewById(R.id.status);
        mottoText = (TextView) findViewById(R.id.motto);
        descText = (TextView) findViewById(R.id.desc);
        titleImage = (ImageView) findViewById(R.id.titleImage);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        running = prefs.getBoolean("Running", false);
        random = new Random();
        int imageNum = random.nextInt(2);
        int images[] = {R.drawable.road, R.drawable.desert_road};
        titleImage.setImageResource(images[imageNum]);
        Animation slideDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
        Animation slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
        Animation slideRight = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_right);
        titleImage.startAnimation(slideDown);
        fab.startAnimation(slideUp);
        statusText.startAnimation(slideUp);
        descText.startAnimation(slideRight);
        mottoText.startAnimation(slideRight);
        buildGoogleApiClient();
        fab.setOnClickListener(this);
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
        mGoogleApiClient.connect();

    }


    @Override
    protected void onStop() {
        // stopActivityRecognition();
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    protected void startActivityRecognition() {
        running = true;
        fab.setImageResource(R.drawable.ic_stop_white_24dp);
        prefs.edit().putBoolean("Running", true).commit();
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
        return true;
    }

    // This determines which dropdown value was touched
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // settings menu
                Intent openSettings = new Intent(this, Settings.class);
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

    protected void stopActivityRecognition() {
        running = false;
        fab.setImageResource(R.drawable.ic_play_arrow_white_24dp);
        prefs.edit().putBoolean("Running", false).commit();
        statusText.setText(getString(R.string.off));
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                mGoogleApiClient,
                getActivityRecognitionPI()
        )
                .setResultCallback(this);
    }


    @Override
    public void onConnected(Bundle bundle) {
        if (running)
            startActivityRecognition();
        Log.d(Constants.TAG, Constants.SERVICES_CONNECTED);
    }

    private PendingIntent getActivityRecognitionPI() {
        Intent intent = new Intent(this, ResultService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(Constants.TAG, Constants.SERVICES_FAILED);
    }

    @Override
    public void onResult(Status status) {
        if (status.isSuccess()) {
            Log.d(Constants.TAG, "Successful");
        }
    }

    @Override
    public void onClick(View v) {
        if (mGoogleApiClient.isConnected() && !running)
            startActivityRecognition();
        else if (mGoogleApiClient.isConnected() && running)
            stopActivityRecognition();
        else
            Toast.makeText(this, "No connection to Google Play Services", Toast.LENGTH_SHORT).show();
    }
}
