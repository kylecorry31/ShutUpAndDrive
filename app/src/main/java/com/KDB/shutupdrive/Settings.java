package com.KDB.shutupdrive;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

/**
 * Created by kyle on 11/3/14.
 */
public class Settings extends ActionBarActivity {
    static Context c;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        c = getApplicationContext();
        // Show up arrow in actionbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.blank);
        // Set the view to the settings
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }
    public static class SettingsFragment extends PreferenceFragment{
        //contains the settings and checks if the gps frequency was changed
        SharedPreferences.OnSharedPreferenceChangeListener listener;
        SharedPreferences prefs;
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            prefs = PreferenceManager.getDefaultSharedPreferences(c);
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
            addPreferencesFromResource(R.xml.prefs);
        }
    }
}
