package com.johncorser.telly.features.settings

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File

/**
 * [BackupDocumentsStore] over Documents/telly on shared storage. Writes try
 * the raw file path first ([BackupDirectFiles]), then fall back to a
 * MediaStore insert-or-update (API 29+, no permission needed for
 * app-created Documents entries; a name collision with a previous
 * install's orphan auto-renames to "telly-backup (1).json", which reads
 * back as the newest match). A MediaStore write then prunes every other
 * deletable backup entry so the app never stacks its own duplicates.
 */
class MediaStoreBackupDocuments(
    private val context: Context,
) : BackupDocumentsStore {
    private val resolver get() = context.contentResolver

    private val direct =
        BackupDirectFiles(
            File(
                @Suppress("DEPRECATION")
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                AutoBackupLocation.DIRECTORY.substringAfter('/'),
            ),
        )

    override fun write(json: String) {
        if (direct.write(json)) return
        check(hasMediaStorePaths()) { "no writable backup location before API 29" }
        val uri = newestEntry() ?: insertEntry()
        resolver.openOutputStream(uri, "wt")?.use { it.write(json.toByteArray()) }
            ?: error("cannot open $uri for writing")
        pruneOthers(uri)
    }

    /** Drops every other backup entry; other-owner orphans may refuse and stay. */
    private fun pruneOthers(kept: Uri) {
        allEntries().filter { it != kept }.forEach { runCatching { resolver.delete(it, null, null) } }
    }

    override fun read(): String? = direct.read() ?: readViaMediaStore()

    override fun sighted(): Boolean = direct.sighted() || runCatching { newestEntry() != null }.getOrDefault(false)

    override fun delete() {
        direct.deleteAll()
        runCatching { allEntries().forEach { resolver.delete(it, null, null) } }
    }

    private fun hasMediaStorePaths() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    private fun readViaMediaStore(): String? =
        runCatching {
            newestEntry()?.let { uri ->
                resolver.openInputStream(uri)?.use { it.readBytes().decodeToString() }
            }
        }.getOrNull()

    /** The newest visible MediaStore entry for the backup file, if any. */
    private fun newestEntry(): Uri? = allEntries().firstOrNull()

    /** Every visible MediaStore entry for the backup file, newest first. */
    private fun allEntries(): List<Uri> {
        if (!hasMediaStorePaths()) return emptyList()
        val files = MediaStore.Files.getContentUri(VOLUME)
        val selection = "${MediaStore.MediaColumns.RELATIVE_PATH} = ?"
        val order = "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
        val projection = arrayOf(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DISPLAY_NAME)
        return resolver
            .query(files, projection, selection, arrayOf(RELATIVE_PATH), order)
            ?.use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        if (AutoBackupLocation.isBackupName(cursor.getString(1))) {
                            add(Uri.withAppendedPath(files, cursor.getLong(0).toString()))
                        }
                    }
                }
            }.orEmpty()
    }

    private fun insertEntry(): Uri {
        val values =
            ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, AutoBackupLocation.FILE_NAME)
                put(MediaStore.MediaColumns.MIME_TYPE, SettingsBackupManager.BACKUP_MIME_TYPE)
                put(MediaStore.MediaColumns.RELATIVE_PATH, RELATIVE_PATH)
            }
        return checkNotNull(resolver.insert(MediaStore.Files.getContentUri(VOLUME), values)) {
            "MediaStore refused the backup entry"
        }
    }

    private companion object {
        const val VOLUME = "external"
        const val RELATIVE_PATH = "${AutoBackupLocation.DIRECTORY}/"
    }
}
