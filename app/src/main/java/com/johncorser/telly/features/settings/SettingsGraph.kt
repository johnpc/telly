package com.johncorser.telly.features.settings

import com.johncorser.telly.core.settings.ParentalControls
import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.features.epg.EpgSourceStore
import com.johncorser.telly.features.playlist.PlaylistRepository
import kotlinx.coroutines.CoroutineScope

/** The imperative actions the settings tree can trigger. */
class SettingsActions(
    val updater: PlaylistUpdater,
    val updateEpgNow: suspend () -> Unit,
    val backup: SettingsBackupManager,
    /** Playlist URL edit (PlaylistUrlChanger::change); false keeps the old URL. */
    val changePlaylistUrl: suspend (oldUrl: String, newUrl: String) -> Boolean = { _, _ -> false },
)

/**
 * The settings slice's dependencies, assembled once (MainActivity via
 * ServiceLocator) and turned into a view model per shell entry.
 */
class SettingsGraph(
    val settings: SettingsRepository,
    val playlists: PlaylistRepository,
    val parental: ParentalControls,
    val actions: SettingsActions,
    val versionName: String,
    val epgSources: EpgSourceStore,
) {
    fun viewModel(
        scope: CoroutineScope,
        callbacks: SettingsCallbacks,
    ): SettingsViewModel = SettingsViewModel(scope = scope, graph = this, callbacks = callbacks)
}
