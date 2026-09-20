package com.johncorser.telly.features.guide

import com.johncorser.telly.core.kv.KeyValueStore
import com.johncorser.telly.features.panel.PanelViewModel
import com.johncorser.telly.features.playlist.db.ChannelEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Remembers the guide's selected group across the fullscreen→back controller
 * rebuild (persisted in the scalar KV store, restored on the next build).
 */
class GuideGroupMemory(
    private val store: KeyValueStore,
) {
    private val selected = MutableStateFlow(store.getString(GROUP_KEY) ?: PanelViewModel.ALL_CHANNELS)
    val group: StateFlow<String> = selected.asStateFlow()
    val value: String get() = selected.value

    /** True when the group actually changed (the caller resets focus/scroll). */
    fun select(next: String): Boolean {
        if (next == selected.value) return false
        selected.value = next
        store.putString(GROUP_KEY, next)
        return true
    }

    /**
     * Drops a restored group the current playlist no longer has (renamed,
     * deleted, or a different playlist) back to All channels — otherwise the
     * grid filters to nothing and the guide opens empty with no way out.
     */
    fun arm(
        scope: CoroutineScope,
        channels: StateFlow<List<ChannelEntity>>,
        groups: StateFlow<List<String>>,
    ) {
        if (selected.value == PanelViewModel.ALL_CHANNELS) return
        scope.launch {
            channels.first { it.isNotEmpty() }
            if (selected.value !in groups.value) select(PanelViewModel.ALL_CHANNELS)
        }
    }

    companion object {
        const val GROUP_KEY = "guideGroup"
    }
}
