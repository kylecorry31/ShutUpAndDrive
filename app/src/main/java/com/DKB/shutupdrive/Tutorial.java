package com.DKB.shutupdrive;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
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
import android.widget.Toast;

import com.DKB.shutupdrive.tutorial.AutoReplyContent;
import com.DKB.shutupdrive.tutorial.CallerIDContent;
import com.DKB.shutupdrive.tutorial.DrivingContent;
import com.DKB.shutupdrive.tutorial.MainTutorialContent;
import com.DKB.shutupdrive.ui.DotsPageIndicator;
import com.DKB.shutupdrive.utils.UserSettings;
import com.DKB.shutupdrive.utils.Utils;

/**
 * Created by kyle on 9/6/15.
 */
public class Tutorial extends FragmentActivity {

    private static final int NUM_PAGES = 4;

    private ViewPager mPager;

//    private TextView skip, done;
//
//    private ImageButton next;

    private Button getStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_tutorial2);
        getWindow().setBackgroundDrawable(null);
        DotsPageIndicator progressDots = (DotsPageIndicator) findViewById(R.id.progressDots);
        getStarted = (Button) findViewById(R.id.getStarted);
//        skip = (TextView) findViewById(R.id.skip);
//        done = (TextView) findViewById(R.id.done);
//        next = (ImageButton) findViewById(R.id.next);
        getStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserSettings.setFirst(getApplicationContext(), false);
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                finish();
            }
        });
//        skip.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Utils.setFirst(getApplicationContext(), false);
//                System.gc();
//                Intent i = new Intent(getApplicationContext(), MainActivity.class);
//                startActivity(i);
//                finish();
//            }
//        });
//        done.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Utils.setFirst(getApplicationContext(), false);
//                System.gc();
//                Intent i = new Intent(getApplicationContext(), MainActivity.class);
//                startActivity(i);
//                finish();
//            }
//        });
        mPager = (ViewPager) findViewById(R.id.pager);
        PagerAdapter mPagerAdapter = new ContentPageAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
//        next.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mPager.arrowScroll(View.FOCUS_RIGHT);
//            }
//        });
        progressDots.setViewPager(mPager);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 2) {
                    if (UserSettings.isFirst(getApplication()))
                        enableSMS();
//                    skip.setVisibility(View.VISIBLE);
//                    done.setVisibility(View.GONE);
//                    next.setVisibility(View.VISIBLE);
                } else if (position == 3) {
                    if (UserSettings.isFirst(getApplication()))
                        enablePhone();
//                    skip.setVisibility(View.GONE);
//                    done.setVisibility(View.VISIBLE);
//                    next.setVisibility(View.INVISIBLE);
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
            super.onBackPressed();
        } else {
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }


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
                == PackageManager.PERMISSION_GRANTED) {
            UserSettings.setAutoReply(this, true);
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
                Toast.makeText(this, "SMS permission is needed to auto reply to messages.",
                        Toast.LENGTH_SHORT).show();
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, Utils.PERMISSION_REQUEST_CODE_SMS);
        }

    }

    private void enablePhone() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            UserSettings.setPhoneOption(this, Utils.PHONE_READ_CALLER);
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
                Toast.makeText(this, "Read Phone State permission is needed to detect when the phone is ringing.",
                        Toast.LENGTH_SHORT).show();
            }
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                Toast.makeText(this, "Read Contacts permission is needed to read out the name of the caller.",
                        Toast.LENGTH_SHORT).show();
            }
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS},
                    Utils.PERMISSION_REQUEST_CODE_PHONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Utils.PERMISSION_REQUEST_CODE_SMS: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    UserSettings.setAutoReply(this, true);
                } else {
                    Toast.makeText(this, "Permission was not granted, auto reply disabled.",
                            Toast.LENGTH_SHORT).show();
                    UserSettings.setAutoReply(this, false);
                }
                return;
            }
            case Utils.PERMISSION_REQUEST_CODE_PHONE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    UserSettings.setPhoneOption(getApplicationContext(), Utils.PHONE_READ_CALLER);
                } else if ((grantResults[1] != PackageManager.PERMISSION_GRANTED && permissions[1].contentEquals(Manifest.permission.READ_PHONE_STATE)) ||
                        (grantResults[0] != PackageManager.PERMISSION_GRANTED && permissions[0].contentEquals(Manifest.permission.READ_PHONE_STATE))) {
                    UserSettings.setPhoneOption(getApplicationContext(), Utils.PHONE_BLOCK_CALLS);
                } else {
                    UserSettings.setPhoneOption(getApplicationContext(), Utils.PHONE_READ_CALLER);
                }
            }
        }
    }
}