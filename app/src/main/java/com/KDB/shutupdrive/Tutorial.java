package com.KDB.shutupdrive;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
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
    Button nextBtn;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_tutorial);
        nextCount = 0;
        featureText = (TextView) findViewById(R.id.feature_name);
        descriptionText = (TextView) findViewById(R.id.description);
        image = (ImageView) findViewById(R.id.photo);
        nextBtn = (Button) findViewById(R.id.next);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        updateUI();
        nextBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        nextCount++;
        updateUI();
    }

    protected void updateUI() {
        if (nextCount < features.length) {
            featureText.setText(getString(features[nextCount][0]));
            descriptionText.setText(getString(features[nextCount][1]));
            image.setImageResource(images[nextCount]);
            progressBar.setProgress((int) ((nextCount + 1) / (double) features.length * 100));
            ProgressBarAnimation anim = new ProgressBarAnimation(progressBar, (int) (nextCount / (double) features.length * 100), (int) ((nextCount + 1) / (double) features.length * 100));
            anim.setDuration(500);
            progressBar.startAnimation(anim);
        } else {
            startActivity(new Intent(this, MainActivity.class));
        }
        if ((nextCount + 1) == features.length) {
            nextBtn.setText("Get started");
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
}
