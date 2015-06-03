package com.DKB.shutupdrive;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.graphics.BitmapCompat;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by kyle on 5/31/15.
 */
public class Tutorial extends Activity implements View.OnClickListener {

    private int features[][] = {
            {R.string.feature_driving_detection, R.string.description_driving_detection},
            {R.string.feature_auto_reply, R.string.description_auto_reply},
            {R.string.feature_caller_id_readout, R.string.description_caller_id_readout}
    };

    private int images[] = {
            R.drawable.car_image,
            R.drawable.sms_image,
            R.drawable.phone_image
    };

    int nextCount;
    TextView featureText, descriptionText;
    ImageView image;
    FloatingActionButton fab;
    ProgressBar progressBar;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    Animation fadeIn, fadeOut;
    Bitmap featureImage;
    BitmapFactory.Options options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ALPHA_8;
        setContentView(R.layout.layout_tutorial);
        getWindow().setBackgroundDrawable(null);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();
        nextCount = prefs.getInt(Constants.TUT_NUM_KEY, 0);
        featureText = (TextView) findViewById(R.id.feature_name);
        descriptionText = (TextView) findViewById(R.id.description);
        image = (ImageView) findViewById(R.id.photo);
        fab = (FloatingActionButton) findViewById(R.id.next);
        Animation slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_slide_in_bottom);
        fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_fade_in);
        fadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_fade_out);

        slideUp.setStartOffset(250);
        fab.startAnimation(slideUp);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.startAnimation(fadeIn);
        featureText.startAnimation(fadeIn);
        descriptionText.startAnimation(fadeIn);
        fadeIn.setStartOffset(250);
        image.startAnimation(fadeIn);
        updateUI();
        fab.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        nextCount++;
        updateUI();
    }

    protected void updateUI() {
        if (nextCount < features.length) {
            featureText.startAnimation(fadeOut);
            descriptionText.startAnimation(fadeOut);
            image.startAnimation(fadeOut);
            featureText.setText(getString(features[nextCount][0]));
            descriptionText.setText(getString(features[nextCount][1]));
            new LoadAndShowImages().execute(images[nextCount]);
            featureText.startAnimation(fadeIn);
            descriptionText.startAnimation(fadeIn);
            image.startAnimation(fadeIn);
            progressBar.setProgress((int) ((nextCount + 1) / (double) features.length * 100));
            ProgressBarAnimation anim = new ProgressBarAnimation(progressBar, (int) (nextCount / (double) features.length * 100), (int) ((nextCount + 1) / (double) features.length * 100));
            progressBar.startAnimation(anim);
            editor.putInt(Constants.TUT_NUM_KEY, nextCount);
            editor.apply();
        } else {
            editor.putInt(Constants.TUT_NUM_KEY, 0);
            featureText.startAnimation(fadeOut);
            descriptionText.startAnimation(fadeOut);
            image.startAnimation(fadeOut);
            progressBar.startAnimation(fadeOut);
            Animation slideOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_slide_out_bottom);
            new Handler()
                    .postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            featureText.setVisibility(View.GONE);
                            fab.setVisibility(View.GONE);
                            progressBar.setVisibility(View.GONE);
                            descriptionText.setVisibility(View.GONE);
                            image.setVisibility(View.GONE);
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            image.setImageDrawable(null);
                            featureImage.recycle();
                            System.gc();
                            finish();
                        }
                    }, slideOut.getDuration());

            fab.startAnimation(slideOut);
        }
    }

    public class ProgressBarAnimation extends Animation {
        private ProgressBar progressBar;
        private float from;
        private float to;

        public ProgressBarAnimation(ProgressBar progressBar, float from, float to) {
            super();
            this.progressBar = progressBar;
            this.from = from;
            this.to = to;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            float value = from + (to - from) * interpolatedTime;
            progressBar.setProgress((int) value);
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
