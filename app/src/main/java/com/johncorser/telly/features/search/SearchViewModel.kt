package com.johncorser.telly.features.search

import com.johncorser.telly.features.playback.TuneController
import com.johncorser.telly.features.playlist.db.ChannelEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Search-screen state machine (catalogue §4): live results per keystroke,
 * the landing history list, the focused programme's detail card and the
 * programme dropdown. Plain class, everything injected, JVM-tested.
 */
class SearchViewModel(
    private val deps: SearchDeps,
    scope: CoroutineScope,
) {
    private val searchHistory = deps.history

    private val mutableQuery = MutableStateFlow("")
    private val mutableHistory = MutableStateFlow(searchHistory.list())
    private val mutableFocusedProgram = MutableStateFlow<SearchProgramHit?>(null)
    private val mutableSelectedChannel = MutableStateFlow<SearchProgramChannel?>(null)

    val query: StateFlow<String> = mutableQuery.asStateFlow()
    val history: StateFlow<List<String>> = mutableHistory.asStateFlow()

    /** The dropdown / coming-soon layer over the screen. */
    val overlays = SearchOverlays()

    /** The dropdown's live actions (reminders / DVR / My list, guide parity). */
    val programMenu = SearchProgramMenu(deps.hooks, overlays, deps.clock, scope)

    /** Last results-area node D-pad focus visited (round7 §C4 focus memory). */
    val focusMemory = SearchFocusMemory()

    /** The mic orb's seam: the top bar launches it, the query follows it. */
    val voice: VoiceSearch = deps.hooks.voice

    init {
        // A recognized voice transcript becomes the typed query (TiviMate
        // hands Android voice-search results straight into the bar).
        scope.launch {
            voice.transcripts.collect { text ->
                if (text != null) onQueryChange(voice.consume() ?: text)
            }
        }
    }

    /** Feeds the right-side detail card (captures 50/51). */
    val focusedProgram: StateFlow<SearchProgramHit?> = mutableFocusedProgram.asStateFlow()

    /** The Programs master-lane channel whose airings the rows pane shows (ref-round6 §D). */
    val selectedChannel: StateFlow<SearchProgramChannel?> = mutableSelectedChannel.asStateFlow()

    /**
     * Results recompute on every keystroke; TiviMate searches as you type.
     * Each batch selects its first master-lane channel and preselects that
     * channel's first airing so the rows pane and detail card are already
     * visible while the IME is still up (ref 50, live tm-03, round6 06/07)
     * — state only, D-pad focus stays wherever it is (the query field).
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val results: StateFlow<SearchResults> =
        mutableQuery
            .mapLatest { deps.repository.search(it, deps.clock(), deps.style) }
            .onEach {
                // A new batch also forgets the DOWN-from-bar focus memory:
                // what the reference does when the results change under it
                // is uncaptured, so telly returns to the fresh landing.
                focusMemory.clear()
                select(it.programs.firstOrNull())
            }
            .stateIn(scope, SharingStarted.Eagerly, SearchResults())

    fun onQueryChange(text: String) {
        mutableQuery.value = text
    }

    /** IME search action / result OK: commit the query into the history. */
    fun commit(raw: String) {
        searchHistory.record(raw)
        mutableHistory.value = searchHistory.list()
    }

    fun onHistoryEntry(entry: String) {
        mutableQuery.value = entry
        commit(entry)
    }

    fun clearHistory() {
        searchHistory.clear()
        mutableHistory.value = emptyList()
    }

    /** OK on a channel card: persist the target so playback tunes to it. */
    fun onChannelResult(channel: ChannelEntity) {
        deps.lastChannelStore.putLong(TuneController.LAST_CHANNEL_KEY, channel.id)
        commit(mutableQuery.value)
    }

    fun onProgramFocused(hit: SearchProgramHit) {
        mutableFocusedProgram.value = hit
    }

    /**
     * Focusing a master-lane channel card swaps the rows pane to that
     * channel's airings and preselects its first airing into the detail
     * card (ref-round6 08-programs-card-newsone-plus1-selected).
     */
    fun onProgramChannelFocused(group: SearchProgramChannel) = select(group)

    /**
     * OK on a master-lane card TUNES its channel, exactly like a Channels
     * card (the free reference gated this behind Unlock Premium, which
     * telly — fully open source — has removed).
     */
    fun onProgramChannelResult(channel: ChannelEntity) = onChannelResult(channel)

    private fun select(group: SearchProgramChannel?) {
        mutableSelectedChannel.value = group
        mutableFocusedProgram.value = group?.airings?.firstOrNull()
    }

    /** OK on a programme row opens the guide-cell dropdown (capture 27). */
    fun onProgramResult(hit: SearchProgramHit) {
        commit(mutableQuery.value)
        overlays.show(SearchOverlay.ProgramMenu(hit))
    }
}
