package com.DKB.shutupdrive;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;


/**
 * Created by kyle on 8/12/14.
 */
public class Splash extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash);
        getWindow().setBackgroundDrawable(null);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i;
                if (Utils.isFirst(getApplicationContext())) {
                    Utils.setFirst(getApplicationContext(), false);
                    i = new Intent(getApplicationContext(), Tutorial.class);
                } else {
                    i = new Intent(getApplicationContext(), MainActivity.class);
                }
                startActivity(i);
            }
        }, 1000);
    }


    @Override
    protected void onPause() {
        super.onPause();
        System.gc();
        finish();
    }


}
