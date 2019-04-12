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

import java.util.HashMap;
import android.app.Activity;
import android.preference.PreferenceManager;

/**
 * Various useful methods for the application
 */
public class Misc {
	
	// SIMPLE HASHMAP (to easily share data across objects)
	private static HashMap<String,Object> map = new HashMap<String,Object>();
	public static Object getAttribute(String s)	{ return map.get(s); }
	public static void setAttribute(String s, Object o)	{ map.put(s, o); }
	public static void resetAttributes() { map = new HashMap<String,Object>(); }
	
	// PREFERENCES
	public static void setPreference(Activity a, String key, boolean value) {	PreferenceManager.getDefaultSharedPreferences(a).edit().putBoolean(key, value).commit();	}
	public static void setPreference(Activity a, String key, float value) {		PreferenceManager.getDefaultSharedPreferences(a).edit().putFloat(key, value).commit();	}
	public static void setPreference(Activity a, String key, int value) {		PreferenceManager.getDefaultSharedPreferences(a).edit().putInt(key, value).commit();	}
	public static void setPreference(Activity a, String key, long value) {		PreferenceManager.getDefaultSharedPreferences(a).edit().putLong(key, value).commit();	}
	public static void setPreference(Activity a, String key, String value) {	PreferenceManager.getDefaultSharedPreferences(a).edit().putString(key, value).commit();	}
	
	public static boolean getPreference(Activity a, String key, boolean def) {	return PreferenceManager.getDefaultSharedPreferences(a).getBoolean(key, def);	}
	public static float getPreference(Activity a, String key, float def) {		return PreferenceManager.getDefaultSharedPreferences(a).getFloat(key, def);	}
	public static int getPreference(Activity a, String key, int def) {			return PreferenceManager.getDefaultSharedPreferences(a).getInt(key, def);	}
	public static long getPreference(Activity a, String key, long def) {		return PreferenceManager.getDefaultSharedPreferences(a).getLong(key, def);	}
	public static String getPreference(Activity a, String key, String def) {	return PreferenceManager.getDefaultSharedPreferences(a).getString(key, def);	}

	public static void setPreference(String key, boolean value) {	setPreference(getActivity(), key, value);	}
	public static void setPreference(String key, float value) {		setPreference(getActivity(), key, value);	}
	public static void setPreference(String key, int value) {		setPreference(getActivity(), key, value);	}
	public static void setPreference(String key, long value) {		setPreference(getActivity(), key, value);	}
	public static void setPreference(String key, String value) {	setPreference(getActivity(), key, value);	}
	
	public static boolean getPreference(String key, boolean def) {	return getPreference(getActivity(), key, def);	}
	public static float getPreference(String key, float def) {		return getPreference(getActivity(), key, def);	}
	public static int getPreference(String key, int def) {			return getPreference(getActivity(), key, def);	}
	public static long getPreference(String key, long def) {		return getPreference(getActivity(), key, def);	}
	public static String getPreference(String key, String def) {	return getPreference(getActivity(), key, def);	}
	
	public static Activity getActivity() {
		return (Activity) getAttribute("activity");
	}
}
