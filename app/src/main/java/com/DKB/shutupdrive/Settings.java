package com.DKB.shutupdrive;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by kyle on 11/3/14.
 */
public class Settings extends AppCompatActivity {
    private static Preference messagePreference;
    private static ListPreference phonePreference;
    private static SwitchPreference gpsPreference, autoPreference;
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
            gpsPreference = (SwitchPreference) getPreferenceScreen().findPreference(getString(R.string.key_gps));
            autoPreference = (SwitchPreference) getPreferenceScreen().findPreference(getString(R.string.key_auto_reply));
            messagePreference.setSummary(Utils.getAutoReplyMessage(context));
            messagePreference.setEnabled(Utils.isAutoReply(context));

            phonePreference = (ListPreference) getPreferenceScreen().findPreference(getString(R.string.key_phone_option));
            phonePreference.setSummary(getPhoneOption());


            sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onDestroy() {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
            super.onDestroy();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(getString(R.string.key_auto_reply_message))) {
                String userMessage = Utils.getAutoReplyMessage(context);
                messagePreference.setSummary(userMessage);
            } else if (key.equals(getString(R.string.key_auto_reply))) {
                if (Utils.isAutoReply(context)) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                            == PackageManager.PERMISSION_GRANTED) {
                        messagePreference.setEnabled(true);
                    } else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.SEND_SMS)) {
                            Toast.makeText(context, "SMS permission is needed to auto reply to messages.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.SEND_SMS}, Utils.PERMISSION_REQUEST_CODE_SMS);
                    }
                } else {
                    messagePreference.setEnabled(false);
                }
            } else if (key.contentEquals(getString(R.string.key_phone_option))) {
                if (Utils.getPhoneOption(context) == Utils.PHONE_ALLOW_CALLS) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                            == PackageManager.PERMISSION_GRANTED) {
                        phonePreference.setSummary(getPhoneOption());
                    } else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_PHONE_STATE)) {
                            Toast.makeText(context, "Read Phone State permission is needed to detect when the phone is ringing.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_PHONE_STATE}, Utils.PERMISSION_REQUEST_CODE_PHONE);
                    }
                } else if (Utils.getPhoneOption(context) == Utils.PHONE_READ_CALLER) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_PHONE_STATE}, Utils.PERMISSION_REQUEST_CODE_PHONE);
                    } else {
                        phonePreference.setSummary(getPhoneOption());
                    }
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS}, Utils.PERMISSION_REQUEST_CODE_CONTACTS);
                    } else {
                        phonePreference.setSummary(getPhoneOption());
                    }
                } else {
                    phonePreference.setSummary(getPhoneOption());
                }
            } else if (key.contentEquals(getString(R.string.key_gps))) {
                if (Utils.getGPS(context) && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Utils.PERMISSION_REQUEST_CODE_LOCATION);
                }
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            // SMS Permission
            case Utils.PERMISSION_REQUEST_CODE_SMS: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    messagePreference.setEnabled(true);
                } else {
                    Toast.makeText(context, "Permission was not granted",
                            Toast.LENGTH_SHORT).show();
                    autoPreference.setChecked(false);
                    Utils.setAutoReply(context, false);
                }
                return;
            }
            // Location Permission
            case Utils.PERMISSION_REQUEST_CODE_LOCATION: {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(context, "Permission was not granted",
                            Toast.LENGTH_SHORT).show();
                    Utils.setGPS(context, false);
                    gpsPreference.setChecked(false);
                }
                return;
            }
            // Phone Permission
            case Utils.PERMISSION_REQUEST_CODE_PHONE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    int option = Utils.getPhoneOption(context);
                    if (option == Utils.PHONE_READ_CALLER && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
                            == PackageManager.PERMISSION_GRANTED) {
                        Utils.setPhoneOption(context, Utils.PHONE_READ_CALLER);
                    } else {
                        Utils.setPhoneOption(context, Utils.PHONE_ALLOW_CALLS);
                    }
                } else {
                    Toast.makeText(context, "Permission was not granted",
                            Toast.LENGTH_SHORT).show();
                    Utils.setPhoneOption(context, Utils.PHONE_BLOCK_CALLS);
                }
                phonePreference.setValue(String.valueOf(Utils.getPhoneOption(context)));
                phonePreference.setSummary(getPhoneOption());
                return;
            }
            // Contacts Permission
            case Utils.PERMISSION_REQUEST_CODE_CONTACTS: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                            == PackageManager.PERMISSION_GRANTED) {
                        Utils.setPhoneOption(context, Utils.PHONE_READ_CALLER);

                    } else {
                        Toast.makeText(context, "Permission was not granted",
                                Toast.LENGTH_SHORT).show();
                        Utils.setPhoneOption(context, Utils.PHONE_BLOCK_CALLS);
                    }
                } else {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                            == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(context, "Permission was not granted",
                                Toast.LENGTH_SHORT).show();
                        Utils.setPhoneOption(context, Utils.PHONE_ALLOW_CALLS);
                    } else {
                        Toast.makeText(context, "Permission was not granted",
                                Toast.LENGTH_SHORT).show();
                        Utils.setPhoneOption(context, Utils.PHONE_BLOCK_CALLS);
                    }
                }
                phonePreference.setValue(String.valueOf(Utils.getPhoneOption(context)));
                phonePreference.setSummary(getPhoneOption());
            }

        }
    }


}
