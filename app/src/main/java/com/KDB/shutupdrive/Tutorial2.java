package com.KDB.shutupdrive;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

/**
 * Created by kyle on 8/12/14.
 */
public class Tutorial2 extends Activity implements View.OnClickListener {
    private Button next;
    private CheckBox cb;
    private EditText et;
    private SharedPreferences getPrefs;
    private String msg;
    private boolean auto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutorial_autoreply);
        next = (Button) findViewById(R.id.nextBtn1);
        cb = (CheckBox) findViewById(R.id.chkbx1);
        et = (EditText) findViewById(R.id.message);
        getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        userSettings();
        next.setOnClickListener(this);
    }

    private void userSettings() {
        msg = getPrefs
                .getString("msg", "");
        if (msg.contentEquals("")) {
            msg = "";
        }
        if (!msg.isEmpty()) {
            et.setText(msg);
        }
        auto = getPrefs.getBoolean("autoReply", true);
        cb.setChecked(auto);
    }

    @Override
    public void onClick(View v) {
        SharedPreferences.Editor editor = getPrefs.edit();
        editor.putBoolean("autoReply", cb.isChecked());
        editor.putString("msg", et.getText().toString());
        editor.apply();
        Intent i = new Intent(this, Tutorial3.class);
        startActivity(i);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
