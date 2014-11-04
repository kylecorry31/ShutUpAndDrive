package com.KDB.shutupdrive;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.widget.Toast;

public class prefs extends PreferenceActivity {
    SharedPreferences.OnSharedPreferenceChangeListener listener;
    SharedPreferences prefs;
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                Log.d("Preference Changed", key);
                if(key.equals("gps")){
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("gpsChange", true);
                    editor.apply();
                }
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(listener);
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);

    }
}
