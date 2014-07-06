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
				"Preferences updated, you may need to restart the app for your updates to take effect",
				Toast.LENGTH_SHORT).show();
	}
}
