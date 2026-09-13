package com.johncorser.telly.core.kv

/**
 * Tiny persistent key-value seam for scalars like the last-watched channel
 * (decision: SharedPreferences-backed, see CLAUDE.md). Logic depends on this
 * interface; JVM tests inject an in-memory map implementation.
 */
interface KeyValueStore {
    fun getLong(key: String): Long?

    fun putLong(
        key: String,
        value: Long,
    )
}
