package com.johncorser.telly.features.settings

import com.johncorser.telly.core.settings.ParentalControls
import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.features.epg.EpgSourceStore
import com.johncorser.telly.features.playlist.PlaylistRepository
import com.johncorser.telly.features.recording.RecordingSettingsHook
import com.johncorser.telly.features.reminders.ReminderSettingsFeed
import com.johncorser.telly.features.search.SearchHistory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** The imperative actions the settings tree can trigger. */
class SettingsActions(
    val updater: PlaylistUpdater,
    val updateEpgNow: suspend () -> Unit,
    val backup: SettingsBackupManager,
    /** Playlist URL edit (PlaylistUrlChanger::change); false keeps the old URL. */
    val changePlaylistUrl: suspend (oldUrl: String, newUrl: String) -> Boolean = { _, _ -> false },
    /** Other -> VOD -> Clear playback positions (empties vod_positions). */
    val clearVodPositions: suspend () -> Unit = {},
    /** Other -> Recording pane (recording slice); null in JVM tests. */
    val recordings: RecordingSettingsHook? = null,
)

/** The feature stores the settings tree reads/edits beyond the prefs map. */
class SettingsStores(
    val epgSources: EpgSourceStore,
    /** Parental "Blocked channels" pane; null renders it empty. */
    val blocked: BlockedChannels? = null,
    /** Other -> Search -> Clear search history; null makes clear a no-op. */
    val searchHistory: SearchHistory? = null,
    /** General -> "Automatic backup" row summary (last export time). */
    val autoBackup: Flow<AutoBackupStatus> = flowOf(AutoBackupStatus()),
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
    val stores: SettingsStores,
) {
    /** The reminders feed (Other -> Reminders); attached by the composition root. */
    var reminders: ReminderSettingsFeed? = null

    fun viewModel(
        scope: CoroutineScope,
        callbacks: SettingsCallbacks,
    ): SettingsViewModel = SettingsViewModel(scope = scope, graph = this, callbacks = callbacks)
}
