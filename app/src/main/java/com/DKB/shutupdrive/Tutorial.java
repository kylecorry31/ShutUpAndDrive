package com.DKB.shutupdrive;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by kyle on 5/31/15.
 */
public class Tutorial extends Activity implements View.OnClickListener {

    private final int[][] features = {
            {R.string.feature_driving_detection, R.string.description_driving_detection},
            {R.string.feature_auto_reply, R.string.description_auto_reply},
            {R.string.feature_caller_id_readout, R.string.description_caller_id_readout}
    };

    private final int[] images = {
            R.drawable.car_image,
            R.drawable.sms_image,
            R.drawable.phone_image
    };

    private int nextCount;
    private TextView featureText;
    private TextView descriptionText;
    private ImageView image;
    private FloatingActionButton fab;
    private Animation fadeIn;
    private Animation fadeOut;
    private Bitmap featureImage;
    private BitmapFactory.Options options;
    private LinearLayout progressDots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ALPHA_8;
        setContentView(R.layout.layout_tutorial);
        getWindow().setBackgroundDrawable(null);
        if (savedInstanceState != null && savedInstanceState.containsKey(Utils.TUTORIAL_NUM_KEY)) {
            nextCount = savedInstanceState.getInt(Utils.TUTORIAL_NUM_KEY);
        } else {
            nextCount = 0;
        }
        featureText = (TextView) findViewById(R.id.feature_name);
        descriptionText = (TextView) findViewById(R.id.description);
        progressDots = (LinearLayout) findViewById(R.id.progressDots);
        image = (ImageView) findViewById(R.id.photo);
        fab = (FloatingActionButton) findViewById(R.id.next);
        Animation fabIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_in);
        fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_fade_in);
        fadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_fade_out);
        fadeOut.setFillAfter(true);
        fabIn.setStartOffset(250);
        fabIn.setDuration(250);
        fab.startAnimation(fabIn);
        progressDots.startAnimation(fadeIn);
        featureText.startAnimation(fadeIn);
        descriptionText.startAnimation(fadeIn);
        fadeIn.setStartOffset(250);
        image.startAnimation(fadeIn);
        updateUI();
        fab.setOnClickListener(this);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(Utils.TUTORIAL_NUM_KEY, nextCount);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View v) {
        nextCount++;
        updateUI();
    }

    private void updateUI() {
        if (nextCount < features.length) {

            if (nextCount == 1) {
                if (checkSelfPermission(Manifest.permission.SEND_SMS)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.SEND_SMS}, Utils.PERMISSION_REQUEST_CODE_SMS);
                }
            } else if (nextCount == 2) {
                if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, Utils.PERMISSION_REQUEST_CODE_PHONE);
                }
                if (checkSelfPermission(Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, Utils.PERMISSION_REQUEST_CODE_CONTACTS);
                }
            }
            featureText.startAnimation(fadeOut);
            descriptionText.startAnimation(fadeOut);
            image.startAnimation(fadeOut);
            featureText.setText(getString(features[nextCount][0]));
            descriptionText.setText(getString(features[nextCount][1]));
            new LoadAndShowImages().execute(images[nextCount]);
            featureText.startAnimation(fadeIn);
            descriptionText.startAnimation(fadeIn);
            image.startAnimation(fadeIn);
            for (int i = 0; i < 3; i++) {
                if (i == nextCount)
                    ((TextView) progressDots.getChildAt(i)).setTextColor(getResources().getColor(R.color.accent));
                else
                    ((TextView) progressDots.getChildAt(i)).setTextColor(getResources().getColor(R.color.white));
            }
        } else {
            featureText.startAnimation(fadeOut);
            descriptionText.startAnimation(fadeOut);
            image.startAnimation(fadeOut);
            progressDots.startAnimation(fadeOut);
            new Handler()
                    .postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            image.setImageDrawable(null);
                            featureImage.recycle();
                            System.gc();
                            finish();
                        }
                    }, fadeOut.getDuration());

            fab.hide();
        }
    }

    private class LoadAndShowImages extends AsyncTask<Integer, Void, Void> {
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            image.setImageBitmap(featureImage);
        }

        @Override
        protected Void doInBackground(Integer... params) {
            featureImage = BitmapFactory.decodeResource(getResources(), params[0], options);
            return null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Utils.PERMISSION_REQUEST_CODE_SMS: {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Utils.setAutoReply(this, true);
                } else {
                    Utils.setAutoReply(this, false);
                }
                return;
            }
            case Utils.PERMISSION_REQUEST_CODE_PHONE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkSelfPermission(Manifest.permission.READ_CONTACTS)
                            == PackageManager.PERMISSION_GRANTED) {
                        Utils.setPhoneOption(this, Utils.PHONE_READ_CALLER);

                    } else {
                        Utils.setPhoneOption(this, Utils.PHONE_ALLOW_CALLS);
                    }
                } else {
                    Utils.setPhoneOption(this, Utils.PHONE_BLOCK_CALLS);
                }
                return;
            }
            case Utils.PERMISSION_REQUEST_CODE_CONTACTS: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
                            == PackageManager.PERMISSION_GRANTED) {
                        Utils.setPhoneOption(this, Utils.PHONE_READ_CALLER);

                    } else {
                        Utils.setPhoneOption(this, Utils.PHONE_BLOCK_CALLS);
                    }
                } else {
                    if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
                            == PackageManager.PERMISSION_GRANTED) {
                        Utils.setPhoneOption(this, Utils.PHONE_ALLOW_CALLS);
                    } else {
                        Utils.setPhoneOption(this, Utils.PHONE_BLOCK_CALLS);
                    }
                }
                return;
            }

        }
    }
}
