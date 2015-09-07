package com.DKB.shutupdrive;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by kyle on 9/6/15.
 */
public class Tutorial extends FragmentActivity {

    private static final int NUM_PAGES = 4;

    private ViewPager mPager;

    private LinearLayout progressDots;

    private PagerAdapter mPagerAdapter;

    private Button start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_tutorial);
        getWindow().setBackgroundDrawable(null);
        progressDots = (LinearLayout) findViewById(R.id.progressDots);
        start = (Button) findViewById(R.id.getStarted);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.gc();
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                finish();
            }
        });
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ContentPageAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 2) {
                    enableSMS();
                } else if (position == 3) {
                    enablePhone();
                }
                for (int i = 0; i < NUM_PAGES; i++) {
                    if (i == position)
                        ((TextView) progressDots.getChildAt(i)).setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
                    else
                        ((TextView) progressDots.getChildAt(i)).setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ContentPageAdapter extends FragmentStatePagerAdapter {
        public ContentPageAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new MainTutorialContent();
                case 1:
                    return new DrivingContent();
                case 2:
                    return new AutoReplyContent();
                case 3:
                    return new CallerIDContent();
            }
            return null;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    private void enableSMS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.SEND_SMS}, Utils.PERMISSION_REQUEST_CODE_SMS);
        }
    }

    private void enablePhone() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS}, Utils.PERMISSION_REQUEST_CODE_PHONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Utils.PERMISSION_REQUEST_CODE_SMS: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Utils.setAutoReply(this, true);
                } else {
                    Utils.setAutoReply(this, false);
                }
                return;
            }
            case Utils.PERMISSION_REQUEST_CODE_PHONE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Utils.setPhoneOption(getApplicationContext(), Utils.PHONE_READ_CALLER);
                } else if ((grantResults[1] != PackageManager.PERMISSION_GRANTED && permissions[1].contentEquals(Manifest.permission.READ_PHONE_STATE)) ||
                        (grantResults[0] != PackageManager.PERMISSION_GRANTED && permissions[0].contentEquals(Manifest.permission.READ_PHONE_STATE))) {
                    Utils.setPhoneOption(getApplicationContext(), Utils.PHONE_BLOCK_CALLS);
                } else {
                    Utils.setPhoneOption(getApplicationContext(), Utils.PHONE_ALLOW_CALLS);
                }
            }
        }
    }
}