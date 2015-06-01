package com.DKB.shutupdrive;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by kyle on 8/12/14.
 */
public class Splash extends Activity {
    private static final String FILENAME = "firstTime";
    ImageView image;
    TextView titleText;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putInt(Constants.TUT_NUM_KEY, 0);
        editor.apply();
        setContentView(R.layout.splash);
        getWindow().setBackgroundDrawable(null);
        image = (ImageView) findViewById(R.id.icon);
        titleText = (TextView) findViewById(R.id.title);
        Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_fade_in);
        fadeIn.setDuration(750);
        fadeIn.setStartOffset(250);
        image.startAnimation(fadeIn);
        titleText.startAnimation(fadeIn);
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!isFirst()) {
                    // Open the main class

                    Intent i = new Intent(getBaseContext(), MainActivity.class);
                    startActivity(i);
                }
            }
        }, 2000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.gc();
        finish();
    }


    // If it is the first time the user opened the app, show tutorial
    private boolean isFirst() {
        SharedPreferences getPrefs;
        getPrefs = getSharedPreferences(FILENAME, 0);
        boolean first = getPrefs.getBoolean("firstTime", true);
        if (first) {
            SharedPreferences.Editor editor = getPrefs.edit();
            editor.putBoolean("firstTime", false);
            editor.apply();
            Intent intent = new Intent(getApplicationContext(), Tutorial.class);
            startActivity(intent);
            return true;
        } else {
            return false;
        }
    }

}
