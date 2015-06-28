package com.DKB.shutupdrive;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.w3c.dom.Text;

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
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    Animation fadeIn, fadeOut;
    Bitmap featureImage;
    BitmapFactory.Options options;
    LinearLayout progressDots;

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
    public void onClick(View v) {
        nextCount++;
        updateUI();
    }

    protected void updateUI() {
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
            editor.putInt(Constants.TUT_NUM_KEY, nextCount);
            editor.apply();
        } else {
            editor.putInt(Constants.TUT_NUM_KEY, 0);
            featureText.startAnimation(fadeOut);
            descriptionText.startAnimation(fadeOut);
            image.startAnimation(fadeOut);
            progressDots.startAnimation(fadeOut);
            Animation fabOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_out);
            fabOut.setFillAfter(true);
            fabOut.setDuration(250);
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
                    }, fabOut.getDuration());

            fab.startAnimation(fabOut);
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
