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
    private static Preference messagePreference;
    private static Preference phonePreference;
    private static Context context;
    private static SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.blank);
        context = this;
        getWindow().setBackgroundDrawable(null);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs);
            sharedPreferences = getPreferenceManager().getSharedPreferences();
            messagePreference = getPreferenceScreen().findPreference(getString(R.string.key_auto_reply_message));
            messagePreference.setSummary(Utils.getAutoReplyMessage(context));
            messagePreference.setEnabled(Utils.isAutoReply(context));

            phonePreference = getPreferenceScreen().findPreference(getString(R.string.key_phone_option));
            phonePreference.setSummary(getPhoneOption());


            sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.contentEquals(getString(R.string.key_auto_reply_message))) {
                String userMessage = Utils.getAutoReplyMessage(context);
                messagePreference.setSummary(userMessage);
            } else if (key.contentEquals(getString(R.string.key_auto_reply))) {
                messagePreference.setEnabled(Utils.isAutoReply(context));
                // on -> permission sms
            } else if (key.contentEquals(getString(R.string.key_phone_option))) {
                phonePreference.setSummary(getPhoneOption());
                // on -> permission phone
            }
        }


        public String getPhoneOption() {
            int phoneOption = Utils.getPhoneOption(context);
            switch (phoneOption) {
                case Utils.PHONE_BLOCK_CALLS:
                    return getString(R.string.phone_option_block);
                case Utils.PHONE_READ_CALLER:
                    return getString(R.string.phone_option_read);
                case Utils.PHONE_ALLOW_CALLS:
                    return getString(R.string.phone_option_allow);
            }
            return getString(R.string.phone_option_read);
        }
    }
}
