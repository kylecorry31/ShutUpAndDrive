package com.DKB.shutupdrive;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
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
            /*
                nextCount = 0 -> permission activity recognition
                    else: fab ask
                nextCount = 1 -> permission sms
                    else: autoreply -> false
                nextCount = 2 -> permission phone
                    else: phone -> block
             */
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

}
