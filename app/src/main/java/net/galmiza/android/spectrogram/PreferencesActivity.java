/**
 * Spectrogram Android application
 * Copyright (c) 2013 Guillaume Adam  http://www.galmiza.net/

 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from the use of this software.
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it freely,
 * subject to the following restrictions:

 * 1. The origin of this software must not be misrepresented; you must not claim that you wrote the original software. If you use this software in a product, an acknowledgment in the product documentation would be appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */

package net.galmiza.android.spectrogram;


import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * Renders preferences as defined in xml/preferences.xml
 * Formats text using string patterns defined in values-xx/string.xml
 * Note that activity is registered as activity in the manifest file AndroidManifest.xml
 */
public class PreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		// Init values
	    
	    // Run update
	    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		UpdateConfiguration(R.array.preferences_configuration);
	}
	
	/**
	 * UpdateConfiguration
	 * Map items of ressourceId to several arrays string[3] = type, key, pattern defined in string.xml
	 * Replaces summary of preference 'key' by 'pattern' taking current value as input 
	 */
	@SuppressWarnings("deprecation")
	private void UpdateConfiguration(int ressourceId) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		String[] cf = getResources().getStringArray(ressourceId);
		
		for (int i=0; i<cf.length/4; i++) {
			String type = cf[4*i+0];
			String object = cf[4*i+1];
			String key = cf[4*i+2];
			String pattern = cf[4*i+3];
			
			Preference preference = (Preference) findPreference(key);
			if (preference == null) continue;
			
			String text = "";
			if (type.equals("string")) text = String.format(pattern, sharedPreferences.getString(key, ""));
			if (type.equals("integer")) text = String.format(pattern, sharedPreferences.getInt(key, 100));
			
			if (object.equals("summary")) preference.setSummary(text);
			if (object.equals("title")) preference.setTitle(text);
		}
	}
	public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
		// Whatever the change is, update all!
		UpdateConfiguration(R.array.preferences_configuration);
	}
}
