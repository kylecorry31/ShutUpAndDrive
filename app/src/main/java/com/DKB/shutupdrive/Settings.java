package com.DKB.shutupdrive;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by kyle on 11/3/14.
 */
public class Settings extends AppCompatActivity {
    static Preference messagePreference;
    static Preference phonePreference;
    static SharedPreferences sharedPreferences;
    static Tracker tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Show up arrow in actionbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.blank);
        tracker = ((MyApplication) getApplication()).tracker;
        tracker.setScreenName("Settings");
        // Set the view to the settings
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs);
            sharedPreferences = getPreferenceManager().getSharedPreferences();
            String userMessage = sharedPreferences.getString("msg", Constants.DEFAULT_MSG);
            if (userMessage.isEmpty()) {
                userMessage = Constants.DEFAULT_MSG;
            }
            messagePreference = getPreferenceScreen().findPreference("msg");
            messagePreference.setSummary(userMessage);
            messagePreference.setEnabled(sharedPreferences.getBoolean("autoReply", true));

            phonePreference = getPreferenceScreen().findPreference("phoneOpt");
            phonePreference.setSummary(getPhoneOption());


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
            } else if (key.contentEquals("phoneOpt")){
                phonePreference.setSummary(getPhoneOption());
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Preferences")
                        .setAction("Phone Options")
                        .setLabel(getPhoneOption())
                        .build());
            } else if(key.contentEquals("autoStart")){
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Preferences")
                        .setAction("Auto Start")
                        .setLabel(String.valueOf(sharedPreferences.getBoolean("autoStart", false)))
                        .build());
            }
        }

        public String getPhoneOption(){
            int phoneOption = Integer.valueOf(sharedPreferences.getString("phoneOpt", "2"));
            switch (phoneOption){
                case 1:
                    return "Blocking calls";
                case 2:
                    return "Reading caller ID";
                case 3:
                    return "Allowing calls";
            }
            return "Reading caller ID";
        }
    }
}
