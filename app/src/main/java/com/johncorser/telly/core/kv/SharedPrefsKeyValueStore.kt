package com.johncorser.telly.core.kv

import android.content.SharedPreferences

/** SharedPreferences adapter; the only Android type behind [KeyValueStore]. */
class SharedPrefsKeyValueStore(
    private val prefs: SharedPreferences,
) : KeyValueStore {
    override fun getLong(key: String): Long? = if (prefs.contains(key)) prefs.getLong(key, 0L) else null

    override fun putLong(
        key: String,
        value: Long,
    ) {
        prefs.edit().putLong(key, value).apply()
    }

    override fun getString(key: String): String? = prefs.getString(key, null)

    override fun putString(
        key: String,
        value: String,
    ) {
        prefs.edit().putString(key, value).apply()
    }
}
