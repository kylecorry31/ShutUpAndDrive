package com.KDB.shutupdrive;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by kyle on 8/12/14.
 */
public class Tutorial4 extends ActionBarActivity implements View.OnClickListener {
    private Button next;
    private EditText et;
    private SharedPreferences getPrefs;
    private String number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutorial_deactivate);
        next = (Button) findViewById(R.id.nextBtn1);
        et = (EditText) findViewById(R.id.et);
        getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        userSettings();
        next.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        SharedPreferences.Editor editor = getPrefs.edit();
        editor.putString("number", et.getText().toString());
        editor.apply();
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    private void userSettings() {

        number = getPrefs.getString("number", "");
        if (number.contentEquals("")) {
            number = "";
        }
        if (!number.isEmpty()) {
            et.setText(number);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
