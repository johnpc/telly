package com.johncorser.telly.features.settings

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Automatic uninstall-proof export: re-writes the backup JSON (the same
 * payload the manual "Back up data" row exports) to shared storage whenever
 * backed-up state changes. Triggers are debounced, identical payloads are
 * skipped by hash, and every failure is logged and swallowed — a broken
 * export must never take the app down.
 */
class ConfigAutoBackup(
    private val enabled: () -> Boolean,
    /** The backup JSON, or null when there is nothing worth backing up. */
    private val buildJson: suspend () -> String?,
    private val write: (String) -> Unit,
    private val state: AutoBackupState,
    private val clock: () -> Long,
    private val warn: (String, Throwable) -> Unit = { _, _ -> },
) {
    /** Debounces [triggers] and exports after each settled burst. */
    fun start(
        scope: CoroutineScope,
        triggers: Flow<*>,
        debounceMs: Long = DEBOUNCE_MS,
    ): Job =
        scope.launch {
            triggers
                .catch { warn("config auto-backup triggers died", it) }
                .collectLatest {
                    delay(debounceMs)
                    // A fresh trigger restarts the wait; an export already
                    // underway runs to completion (no half-written files).
                    withContext(NonCancellable) { exportNow() }
                }
        }

    /** One export attempt: build, dedupe by hash, write, record. */
    suspend fun exportNow() {
        if (!enabled()) return
        runCatching {
            val json = buildJson() ?: return
            val hash = json.hashCode().toLong()
            if (hash == state.lastHash) return
            write(json)
            state.recordExport(clock(), hash)
        }.getOrElse { warn("config auto-backup export failed", it) }
    }

    companion object {
        const val DEBOUNCE_MS = 5_000L
    }
}
