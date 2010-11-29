package org.peterstrand.activities;

import org.peterstrand.R;
import org.peterstrand.service.SensorService;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.widget.Toast;

public class Preferences extends PreferenceActivity {
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		// Get the custom preference
		
		Preference runWatcherPref = (Preference) findPreference("runWatcherPref");
		runWatcherPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Log.i(SensorService.LOGID,"setting runWatcherPref to "+newValue);
				if (newValue == Boolean.TRUE)
					SensorService.scheduleWakeup(100L, Preferences.this,true);
				return true;
			}});
		
	}
	
}