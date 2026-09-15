package com.johncorser.telly.core.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** Typed read/write/observe access to every telly setting. */
class SettingsRepository(
    private val store: KeyValueStore,
) {
    fun <T> get(setting: Setting<T>): T = store.read(setting.key)?.let(setting.decode) ?: setting.default

    fun <T> set(
        setting: Setting<T>,
        value: T,
    ) {
        store.write(setting.key, setting.encode(value))
    }

    fun <T> flow(setting: Setting<T>): Flow<T> =
        store.snapshots
            .map { it[setting.key]?.let(setting.decode) ?: setting.default }
            .distinctUntilChanged()

    /** Emits on every settings change; drives the settings rows UI. */
    val changes: Flow<Map<String, String>> get() = store.snapshots

    /** Raw write by key, used by pickers and backup restore. */
    fun writeRaw(
        key: String,
        raw: String,
    ) {
        store.write(key, raw)
    }

    /** Removes a raw key (playlist URL re-key drops the old-URL copies). */
    fun removeRaw(key: String) {
        store.write(key, null)
    }

    /** The raw persisted map, as exported by backup. */
    fun snapshot(): Map<String, String> = store.readAll()

    /** Replaces persisted values with [values] (backup restore). */
    fun restore(values: Map<String, String>) {
        store.readAll().keys.filterNot(values::containsKey).forEach { store.write(it, null) }
        values.forEach { (key, value) -> store.write(key, value) }
    }
}
