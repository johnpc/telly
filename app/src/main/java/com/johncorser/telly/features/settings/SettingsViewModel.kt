package com.johncorser.telly.features.settings

import com.johncorser.telly.core.settings.ParentalControls
import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.features.epg.EpgSource
import com.johncorser.telly.features.epg.EpgSourceStore
import com.johncorser.telly.features.playlist.PlaylistRepository
import com.johncorser.telly.features.playlist.db.ChannelEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * Plain JVM-testable state holder for the right-sheet settings stack. Rows
 * are rebuilt from the store + playlists on every change; row activations
 * are dispatched generically (toggle table, picker table) with the
 * leftovers in SettingsViewModelActions.kt.
 */
class SettingsViewModel(
    internal val scope: CoroutineScope,
    graph: SettingsGraph,
    internal val callbacks: SettingsCallbacks,
) {
    internal val settings: SettingsRepository = graph.settings
    internal val playlistRepository: PlaylistRepository = graph.playlists
    internal val epgSources: EpgSourceStore = graph.stores.epgSources
    internal val parental: ParentalControls = graph.parental
    internal val blocked: BlockedChannels? = graph.stores.blocked
    internal val searchHistory = graph.stores.searchHistory
    internal val updater: PlaylistUpdater = graph.actions.updater
    internal val updateEpgNow: suspend () -> Unit = graph.actions.updateEpgNow
    internal val backup: SettingsBackupManager = graph.actions.backup
    private val versionName: String = graph.versionName

    internal val mutableState = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = mutableState.asStateFlow()

    val playlistItems: StateFlow<List<PlaylistItem>> =
        playlistRepository.playlists
            .map { stored ->
                stored.map {
                    PlaylistItem(
                        url = it.sourceUrl,
                        name = it.name ?: it.sourceUrl,
                        channelCount = it.playlist.channels.size,
                        epgUrl = it.playlist.epgUrl,
                    )
                }
            }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    /** Custom EPG sources, in added order (Settings -> EPG -> EPG sources). */
    val epgSourceItems: StateFlow<List<EpgSource>> =
        epgSources.sources.stateIn(scope, SharingStarted.Eagerly, emptyList())

    /** Blocked channels (Parental controls -> Blocked channels). */
    val blockedItems: StateFlow<List<ChannelEntity>> =
        (blocked?.channels ?: flowOf(emptyList())).stateIn(scope, SharingStarted.Eagerly, emptyList())

    /** The active sheet's rows (root section list when nothing is pushed). */
    val rows: StateFlow<List<SettingsRow>> =
        combine(mutableState, playlistItems, epgSourceItems, settings.changes, blockedItems) {
                uiState, playlists, sources, _, blockedChannels ->
            rowsFor(uiState.activePane, settings, playlists, versionName, SettingsRowSources(sources, blockedChannels))
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    /** OK on a section row pushes its sheet over the root list. */
    fun selectSection(section: SettingsSection) {
        mutableState.update { it.copy(panes = listOf(SettingsPane.Section(section))) }
    }

    /** OK on a row. Locked rows are unfocusable and never reach this. */
    fun activate(rowId: String) {
        val toggle = SettingsToggles.byRowId[rowId]
        val picker = SettingsPickers.byRowId[rowId]
        when {
            rowId.startsWith(RowIds.SECTION_PREFIX) ->
                selectSection(SettingsSection.valueOf(rowId.removePrefix(RowIds.SECTION_PREFIX)))
            rowId == RowIds.PARENTAL_MASTER -> toggleParentalMaster()
            toggle != null -> flip(toggle)
            picker != null ->
                showOverlay(
                    SettingsOverlay.Picker(picker, SettingsPickers.currentRaw(picker, settings.snapshot())),
                )
            rowId.startsWith(
                RowIds.PLAYLIST_PREFIX,
            ) -> push(SettingsPane.PlaylistDetail(rowId.removePrefix(RowIds.PLAYLIST_PREFIX)))
            rowId.startsWith(RowIds.EPG_CUSTOM_SOURCE_PREFIX) ->
                rowId.removePrefix(RowIds.EPG_CUSTOM_SOURCE_PREFIX).toLongOrNull()?.let {
                    push(SettingsPane.EpgSourceDetail(it))
                }
            rowId.startsWith(RowIds.BLOCKED_CHANNEL_PREFIX) -> unblockChannel(rowId)
            else -> runAction(rowId)
        }
    }

    /** BACK inside settings: overlay first, then one sheet. False = leave. */
    fun back(): Boolean {
        val current = mutableState.value
        return when {
            current.overlay != null -> {
                dismissOverlay()
                true
            }
            current.panes.isNotEmpty() -> {
                mutableState.update { it.copy(panes = it.panes.dropLast(1)) }
                true
            }
            else -> false
        }
    }

    fun dismissOverlay() {
        mutableState.update { it.copy(overlay = null) }
    }
}
