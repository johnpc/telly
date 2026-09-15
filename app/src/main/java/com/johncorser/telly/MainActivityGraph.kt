package com.johncorser.telly

import com.johncorser.telly.core.ServiceLocator
import com.johncorser.telly.core.settings.ParentalControls
import com.johncorser.telly.features.playlist.M3uFetcher
import com.johncorser.telly.features.playlist.PlaylistUrlChanger
import com.johncorser.telly.features.reminders.remindersHub
import com.johncorser.telly.features.settings.PlaylistKeyMigration
import com.johncorser.telly.features.settings.PlaylistUpdater
import com.johncorser.telly.features.settings.SettingsActions
import com.johncorser.telly.features.settings.SettingsBackupManager
import com.johncorser.telly.features.settings.SettingsGraph

// MainActivity's settings-slice assembly (kept out of MainActivity.kt for
// the file-length gate; all logic lives in the tested settings classes).

/** Assembles the settings slice over the shared composition root. */
internal fun MainActivity.settingsGraph(fetcher: M3uFetcher): SettingsGraph {
    val settings = ServiceLocator.settingsRepository(this)
    val repository = ServiceLocator.playlistRepository(this)
    return SettingsGraph(
        settings = settings,
        playlists = repository,
        parental = ParentalControls(settings),
        actions =
            SettingsActions(
                updater = PlaylistUpdater(fetcher::fetch, repository),
                updateEpgNow = { ServiceLocator.epgRefresher(this).refreshAllNow() },
                backup = SettingsBackupManager(settings, repository, filesDir),
                changePlaylistUrl =
                    PlaylistUrlChanger(
                        fetchPlaylist = fetcher::fetch,
                        repository = repository,
                        rekeySettings = { old, new -> PlaylistKeyMigration.apply(settings, old, new) },
                        rekeyEpgSources = ServiceLocator.epgSourceStore(this)::rekeyPlaylist,
                    )::change,
            ),
        versionName = appVersionName(),
        epgSources = ServiceLocator.epgSourceStore(this),
    ).apply { reminders = ServiceLocator.remindersHub(this@settingsGraph).settingsFeed }
}

private fun MainActivity.appVersionName(): String =
    runCatching { packageManager.getPackageInfo(packageName, 0).versionName }
        .getOrNull() ?: "unknown"
