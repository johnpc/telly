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

/**
 * Search-screen state machine (catalogue §4): live results per keystroke,
 * the landing history list, the focused programme's detail card and the
 * programme dropdown. Plain class, everything injected, JVM-tested.
 */
class SearchViewModel(
    private val deps: SearchDeps,
    scope: CoroutineScope,
) {
    private val searchHistory = SearchHistory(deps.historyStore)

    private val mutableQuery = MutableStateFlow("")
    private val mutableHistory = MutableStateFlow(searchHistory.list())
    private val mutableOverlay = MutableStateFlow<SearchOverlay>(SearchOverlay.None)
    private val mutableFocusedProgram = MutableStateFlow<SearchProgramHit?>(null)

    val query: StateFlow<String> = mutableQuery.asStateFlow()
    val history: StateFlow<List<String>> = mutableHistory.asStateFlow()
    val overlay: StateFlow<SearchOverlay> = mutableOverlay.asStateFlow()

    /** Feeds the right-side detail card (captures 50/51). */
    val focusedProgram: StateFlow<SearchProgramHit?> = mutableFocusedProgram.asStateFlow()

    /**
     * Results recompute on every keystroke; TiviMate searches as you type.
     * Each batch preselects its first programme so the detail card is
     * already visible while the IME is still up (ref 50, live tm-03) —
     * state only, D-pad focus stays wherever it is (the query field).
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val results: StateFlow<SearchResults> =
        mutableQuery
            .mapLatest { deps.repository.search(it, deps.clock(), deps.zone) }
            .onEach { mutableFocusedProgram.value = it.programs.firstOrNull() }
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

    /** OK on a programme row opens the guide-cell dropdown (capture 27). */
    fun onProgramResult(hit: SearchProgramHit) {
        commit(mutableQuery.value)
        mutableOverlay.value = SearchOverlay.ProgramMenu(hit)
    }

    /** Dropdown rows + the gear's premium search settings open the paywall. */
    fun showPaywall() {
        mutableOverlay.value = SearchOverlay.Paywall
    }

    /** Voice search lands on the placeholder until that slice ships. */
    fun showComingSoon(feature: String) {
        mutableOverlay.value = SearchOverlay.ComingSoon(feature)
    }

    /** BACK with an overlay up closes just the overlay. */
    fun dismissOverlay(): Boolean {
        if (mutableOverlay.value == SearchOverlay.None) return false
        mutableOverlay.value = SearchOverlay.None
        return true
    }
}
