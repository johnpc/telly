package com.johncorser.telly

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import com.johncorser.telly.core.ServiceLocator
import com.johncorser.telly.core.settings.TellySettings
import com.johncorser.telly.features.onboarding.OnboardingRestore
import com.johncorser.telly.features.onboarding.RestoreRunner
import com.johncorser.telly.features.onboarding.WelcomeRestore
import com.johncorser.telly.features.playlist.M3uFetcher
import com.johncorser.telly.features.settings.AutoBackupState
import com.johncorser.telly.features.settings.ConfigAutoBackup
import com.johncorser.telly.features.settings.MediaStoreBackupDocuments
import com.johncorser.telly.features.settings.PlaylistUpdater
import com.johncorser.telly.features.settings.SettingsBackupManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.runBlocking

// MainActivity's reinstall-proof-backup glue (kept out of MainActivity.kt
// for the file-length gate): the automatic Documents/telly export engine
// and the welcome screen's restore hooks. Logic lives in the tested
// ConfigAutoBackup / RestoreRunner / WelcomeRestore classes.

private val backupScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
private var backupJob: Job? = null
private var sharedBackupState: AutoBackupState? = null

/** Process-wide auto-backup bookkeeping shared with the settings graph. */
internal fun autoBackupState(context: Context): AutoBackupState =
    sharedBackupState ?: AutoBackupState(ServiceLocator.keyValueStore(context)).also { sharedBackupState = it }

/** (Re)starts the auto-export engine over the current composition root. */
internal fun MainActivity.keepConfigBackedUp() {
    val settings = ServiceLocator.settingsRepository(this)
    val repository = ServiceLocator.playlistRepository(this)
    val manager = SettingsBackupManager(settings, repository)
    val engine =
        ConfigAutoBackup(
            enabled = { settings.get(TellySettings.AUTO_BACKUP) },
            // An empty install has nothing worth exporting — and a fresh
            // reinstall must never wipe the previous install's backup.
            buildJson = { if (repository.playlists.first().isEmpty()) null else manager.exportJson() },
            write = MediaStoreBackupDocuments(applicationContext)::write,
            state = autoBackupState(this),
            clock = ServiceLocator.clock,
            warn = { message, cause -> Log.w("telly", message, cause) },
        )
    backupJob?.cancel()
    backupJob = engine.start(backupScope, merge(repository.playlists.map { }, settings.changes.map { }))
}

/**
 * Stops the auto-export engine with the activity (onDestroy) and waits out
 * any in-flight write, so nothing exports into a torn-down composition
 * root (the next onCreate restarts it over fresh dependencies).
 */
internal fun stopConfigBackup() {
    val job = backupJob ?: return
    backupJob = null
    runBlocking {
        job.cancel()
        job.join()
    }
}

/** The welcome screen's "Restore previous setup" platform hooks. */
internal fun MainActivity.onboardingRestore(fetcher: M3uFetcher): OnboardingRestore {
    val settings = ServiceLocator.settingsRepository(this)
    val repository = ServiceLocator.playlistRepository(this)
    val store = MediaStoreBackupDocuments(applicationContext)
    val runner =
        RestoreRunner(
            importJson = SettingsBackupManager(settings, repository)::importJson,
            playlistUrls = { repository.playlists.first().map { it.sourceUrl } },
            updatePlaylists = { urls -> PlaylistUpdater(fetcher::fetch, repository).updateAll(urls) },
            channelCount = { ServiceLocator.database(this).channelDao().totalCount() },
        )
    return OnboardingRestore(
        probe = {
            WelcomeRestore.offerOf(
                json = runCatching { store.read() }.getOrNull(),
                sighted = runCatching { store.sighted() }.getOrDefault(false),
                canRequestAccess = canRequestAllFilesAccess(),
            )
        },
        restore = runner::restore,
        requestAccess = ::launchAllFilesAccess,
        land = ::restoreStartRoute,
    )
}

private fun canRequestAllFilesAccess(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()

/** Opens the system "All files access" grant screen for telly. */
private fun MainActivity.launchAllFilesAccess() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
    runCatching {
        startActivity(
            Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:$packageName")),
        )
    }.onFailure {
        runCatching { startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)) }
            .onFailure { cause -> Log.w("telly", "all-files-access screen unavailable", cause) }
    }
}
