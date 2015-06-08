package com.DKB.shutupdrive;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;


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
        ((MyApplication) getApplication()).tracker.setScreenName("Splash Screen");
        getWindow().setBackgroundDrawable(null);
        image = (ImageView) findViewById(R.id.icon);
        titleText = (TextView) findViewById(R.id.title);
        Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_fade_in);
        fadeIn.setDuration(250);
        fadeIn.setStartOffset(250);
        image.startAnimation(fadeIn);
        titleText.startAnimation(fadeIn);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isFirst()) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }
            }
        }, 1000);
    }

    private boolean isFirst() {
        SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean first = getPrefs.getBoolean("firstTime", true);
        if (first) {
            getPrefs.edit().putBoolean("firstTime", false).apply();
            Intent intent = new Intent(getApplicationContext(), Tutorial.class);
            startActivity(intent);
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.gc();
        finish();
    }


}
