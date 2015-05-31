package com.KDB.shutupdrive;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

/**
 * Created by kyle on 8/12/14.
 */
public class Tutorial3 extends AppCompatActivity implements View.OnClickListener {
    private Button next;
    private CheckBox cb;
    private boolean phone;
    private SharedPreferences getPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutorial_phone);
        next = (Button) findViewById(R.id.nextBtn1);
        cb = (CheckBox) findViewById(R.id.cb);
        getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        userSettings();
        next.setOnClickListener(this);
    }

    private void userSettings() {

        phone = getPrefs.getBoolean("phone", false);
        cb.setChecked(phone);
    }

    @Override
    public void onClick(View v) {
        SharedPreferences.Editor editor = getPrefs.edit();
        editor.putBoolean("phone", cb.isChecked());
        editor.commit();
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
