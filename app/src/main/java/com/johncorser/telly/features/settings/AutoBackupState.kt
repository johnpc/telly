package com.johncorser.telly.features.settings

import com.johncorser.telly.core.kv.KeyValueStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Where the automatic backup lands on shared storage. */
object AutoBackupLocation {
    const val DIRECTORY = "Documents/telly"
    const val FILE_NAME = "telly-backup.json"

    /** Canonical name or a MediaStore rename ("telly-backup (1).json"). */
    fun isBackupName(name: String?): Boolean =
        name != null &&
            name.startsWith(FILE_NAME.substringBeforeLast('.')) &&
            name.endsWith("." + FILE_NAME.substringAfterLast('.'))
}

/** What the General pane's "Automatic backup" row shows. */
data class AutoBackupStatus(
    val lastExportMs: Long? = null,
) {
    fun summary(): String =
        lastExportMs
            ?.let { "Last backup ${FORMAT.format(Date(it))} · ${AutoBackupLocation.DIRECTORY}" }
            ?: "Backs up to ${AutoBackupLocation.DIRECTORY} on every change"

    private companion object {
        val FORMAT = SimpleDateFormat("MMM d, HH:mm", Locale.US)
    }
}

/**
 * Auto-backup bookkeeping (last export time + payload hash). Lives in the
 * scalar KeyValueStore, NOT the settings store: recording an export there
 * would itself change the settings snapshot and re-trigger the export loop.
 */
class AutoBackupState(
    private val kv: KeyValueStore,
) {
    private val mutable = MutableStateFlow(AutoBackupStatus(kv.getLong(KEY_LAST_MS)))

    /** Live status for the settings row summary. */
    val status: StateFlow<AutoBackupStatus> = mutable.asStateFlow()

    /** The hash of the last exported payload; unchanged payloads skip IO. */
    val lastHash: Long? get() = kv.getLong(KEY_LAST_HASH)

    fun recordExport(
        atMs: Long,
        hash: Long,
    ) {
        kv.putLong(KEY_LAST_MS, atMs)
        kv.putLong(KEY_LAST_HASH, hash)
        mutable.value = AutoBackupStatus(atMs)
    }

    companion object {
        const val KEY_LAST_MS = "autoBackupLastMs"
        const val KEY_LAST_HASH = "autoBackupLastHash"
    }
}
