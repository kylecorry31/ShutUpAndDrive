package com.DKB.shutupdrive;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by kyle on 11/3/14.
 */
public class Settings extends AppCompatActivity {
    static Preference messagePreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Show up arrow in actionbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.blank);
        // Set the view to the settings
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs);
            SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
            String userMessage = sharedPreferences.getString("msg", Constants.DEFAULT_MSG);
            if (userMessage.isEmpty()) {
                userMessage = Constants.DEFAULT_MSG;
            }
            messagePreference = getPreferenceScreen().findPreference("msg");
            messagePreference.setSummary(userMessage);
            messagePreference.setEnabled(sharedPreferences.getBoolean("autoReply", true));

            sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.contentEquals("msg")) {
                String userMessage = sharedPreferences.getString("msg", Constants.DEFAULT_MSG);
                if (userMessage.isEmpty()) {
                    userMessage = Constants.DEFAULT_MSG;
                }
                messagePreference.setSummary(userMessage);
            } else if (key.contentEquals("autoReply")) {
                messagePreference.setEnabled(sharedPreferences.getBoolean("autoReply", true));
            }
        }
    }
}
