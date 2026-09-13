package com.johncorser.telly.core.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Dependency-light persistence boundary for user settings (see CLAUDE.md
 * decisions: SharedPreferences behind an interface instead of DataStore).
 * Values are stored as strings; [SettingsRepository] adds the typing.
 */
interface KeyValueStore {
    /** Emits the full key/value map now and after every write. */
    val snapshots: Flow<Map<String, String>>

    fun read(key: String): String?

    /** Writes [value]; null removes the key. */
    fun write(
        key: String,
        value: String?,
    )

    fun readAll(): Map<String, String>
}

/** Process-lifetime store for JVM tests and previews. */
class InMemoryKeyValueStore(
    initial: Map<String, String> = emptyMap(),
) : KeyValueStore {
    private val state = MutableStateFlow(initial)

    override val snapshots = state.asStateFlow()

    override fun read(key: String): String? = state.value[key]

    override fun write(
        key: String,
        value: String?,
    ) {
        state.update { if (value == null) it - key else it + (key to value) }
    }

    override fun readAll(): Map<String, String> = state.value
}
