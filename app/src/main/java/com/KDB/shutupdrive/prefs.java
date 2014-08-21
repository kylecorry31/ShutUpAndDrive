package com.KDB.shutupdrive;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.widget.Toast;

public class prefs extends PreferenceActivity {

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        Toast.makeText(
                this,
                getResources().getString(R.string.pref_update),
                Toast.LENGTH_SHORT).show();
    }
}
