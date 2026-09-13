package com.johncorser.telly.features.settings

import com.johncorser.telly.core.settings.BackupCodec
import com.johncorser.telly.core.settings.BackupPayload
import com.johncorser.telly.core.settings.BackupPlaylist
import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.features.playlist.M3uPlaylist
import com.johncorser.telly.features.playlist.PlaylistRepository
import kotlinx.coroutines.flow.first
import java.io.File

/**
 * Back up / restore of settings + playlist identities as a JSON file
 * (Settings -> General -> "Back up data" / "Restore data"). A restored
 * playlist starts with zero channels; "Update playlist" re-fetches them.
 */
class SettingsBackupManager(
    private val settings: SettingsRepository,
    private val playlists: PlaylistRepository,
    private val localDir: File? = null,
) {
    /** Keeps a copy in the app files dir alongside the SAF export. */
    suspend fun exportLocal(): File? = localDir?.let { exportToDir(it) }

    suspend fun exportJson(): String =
        BackupCodec.encode(
            BackupPayload(
                settings = settings.snapshot(),
                playlists =
                    playlists.playlists.first().map { stored ->
                        BackupPlaylist(
                            name = stored.name ?: stored.sourceUrl,
                            url = stored.sourceUrl,
                            epgUrl = stored.playlist.epgUrl,
                        )
                    },
            ),
        )

    /** Applies a backup; returns false when [json] is not a telly backup. */
    suspend fun importJson(json: String): Boolean {
        val payload = BackupCodec.decode(json) ?: return false
        settings.restore(payload.settings)
        payload.playlists.forEach { entry ->
            playlists.add(entry.url, M3uPlaylist(epgUrl = entry.epgUrl, channels = emptyList()), entry.name)
        }
        return true
    }

    /** Writes the backup into [dir] (the app files dir) and returns the file. */
    suspend fun exportToDir(dir: File): File =
        File(dir, BACKUP_FILE_NAME).apply {
            parentFile?.mkdirs()
            writeText(exportJson())
        }

    suspend fun importFromFile(file: File): Boolean = file.exists() && importJson(file.readText())

    companion object {
        const val BACKUP_FILE_NAME = "telly-backup.json"
        const val BACKUP_MIME_TYPE = "application/json"
    }
}
