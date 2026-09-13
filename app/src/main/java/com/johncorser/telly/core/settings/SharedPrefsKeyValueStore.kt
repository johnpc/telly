package com.johncorser.telly.core.settings

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * SharedPreferences-backed [KeyValueStore]. telly is a single-process app,
 * so a snapshot StateFlow refreshed on every write is an exact mirror.
 */
class SharedPrefsKeyValueStore(
    private val prefs: SharedPreferences,
) : KeyValueStore {
    private val state = MutableStateFlow(currentMap())

    override val snapshots = state.asStateFlow()

    override fun read(key: String): String? = prefs.getString(key, null)

    override fun write(
        key: String,
        value: String?,
    ) {
        prefs.edit { if (value == null) remove(key) else putString(key, value) }
        state.value = currentMap()
    }

    override fun readAll(): Map<String, String> = currentMap()

    private fun currentMap(): Map<String, String> = prefs.all.mapValues { (_, value) -> value.toString() }
}
