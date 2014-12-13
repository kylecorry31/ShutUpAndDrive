package com.KDB.shutupdrive;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;

/**
 * Created by kyle on 8/12/14.
 */
public class Splash extends Activity {
    private static final String FILENAME = "firstTime";
    private boolean first;
    ImageButton muleSticker;
    final static String url = "https://www.stickermule.com/unlock?ref_id=3567360701";
    boolean muleClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Removes actionbar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash);
        muleSticker = (ImageButton) findViewById(R.id.stickerMule);
        if (ActivityUtils.DEVELOPER_EDITION)
            muleSticker.setVisibility(View.GONE);
        muleSticker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mule = new Intent(Intent.ACTION_VIEW);
                mule.setData(Uri.parse(url));
                muleClicked = true;
                startActivity(mule);
            }
        });
        // Splash screen stays on for 3 seconds
        Thread timer = new Thread() {
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    if (!isFirst()) {
                        // Open the main class
                        Intent i = new Intent(getBaseContext(), TestMain.class);
                        if (!muleClicked)
                            startActivity(i);
                    }
                }
            }
        };
        timer.start();

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
            Intent intent = new Intent(getApplicationContext(), Tutorial1.class);
            startActivity(intent);
            return true;
        } else {
            return false;
        }
    }
}
