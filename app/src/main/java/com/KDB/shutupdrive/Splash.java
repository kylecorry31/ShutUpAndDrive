package com.KDB.shutupdrive;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by kyle on 8/12/14.
 */
public class Splash extends Activity implements View.OnClickListener {
    private static final String FILENAME = "firstTime";
    private boolean first;
    ImageView image;
    TextView titleText;
    SharedPreferences.Editor editor;
    ImageButton muleSticker;
    final static String url = "https://www.stickermule.com/unlock?ref_id=3567360701";
    boolean muleClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Removes actionbar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putInt(Constants.TUT_NUM_KEY, 0);
        editor.apply();
        setContentView(R.layout.splash);
        image = (ImageView) findViewById(R.id.icon);
        titleText = (TextView) findViewById(R.id.title);
        muleSticker = (ImageButton) findViewById(R.id.mule);
        if (!Constants.DEVELOPER_MODE)
            muleSticker.setOnClickListener(this);
        else
            muleSticker.setVisibility(View.GONE);
        Animation slideRight = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_right);
        image.startAnimation(slideRight);
        titleText.startAnimation(slideRight);
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!isFirst()) {
                    // Open the main class
                    Intent i = new Intent(getBaseContext(), MainActivity.class);
                    if (!muleClicked)
                        startActivity(i);
                }
            }
        }, 2000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }


    // If it is the first time the user opened the app, show tutorial
    private boolean isFirst() {
        SharedPreferences getPrefs;
        getPrefs = getSharedPreferences(FILENAME, 0);
        first = getPrefs.getBoolean("firstTime", true);
        if (first) {
            SharedPreferences.Editor editor = getPrefs.edit();
            editor.putBoolean("firstTime", false);
            editor.apply();
            Intent intent = new Intent(getApplicationContext(), Tutorial.class);
            if (!muleClicked)
                startActivity(intent);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onClick(View v) {
        Intent mule = new Intent(Intent.ACTION_VIEW);
        mule.setData(Uri.parse(url));
        muleClicked = true;
        startActivity(mule);
    }
}
