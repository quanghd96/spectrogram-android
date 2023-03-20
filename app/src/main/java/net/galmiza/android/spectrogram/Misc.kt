/**
 * Spectrogram Android application
 * Copyright (c) 2013 Guillaume Adam  http://www.galmiza.net/
 *
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from the use of this software.
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it freely,
 * subject to the following restrictions:
 *
 * 1. The origin of this software must not be misrepresented; you must not claim that you wrote the original software. If you use this software in a product, an acknowledgment in the product documentation would be appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */
package net.galmiza.android.spectrogram

import android.app.Activity
import android.content.Context
import androidx.preference.PreferenceManager
import java.util.HashMap

/**
 * Various useful methods for the application
 */
object Misc {
    // SIMPLE HASHMAP (to easily share data across objects)
    private val map = HashMap<String, Any>()
    @JvmStatic
	fun getAttribute(s: String): Any? {
        return map[s]
    }



    // PREFERENCES

    @JvmStatic
    fun getPreference(context: Context, key: String?, def: Boolean): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, def)
    }

    @JvmStatic
    fun getPreference(context: Context, key: String?, def: String?): String? {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, def)
    }
}