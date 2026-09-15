package com.johncorser.telly.features.multiview

import com.johncorser.telly.features.playback.ChannelZapper
import com.johncorser.telly.features.playback.TuneController
import com.johncorser.telly.features.player.PlayerEnginePool
import com.johncorser.telly.features.playlist.db.ChannelEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Multiview state machine (multiview-round/multiview-spec.md): entry = one
 * centered pane on the current channel, OK on a pane opens its menu, every
 * menu row opens the channel picker, BACK walks picker/menu -> panes ->
 * fullscreen playback of the focused pane's channel. Plain class, JVM-tested.
 */
class MultiviewViewModel(
    private val deps: MultiviewDeps,
    private val scope: CoroutineScope,
    private val onExit: () -> Unit = {},
) {
    val panes = MultiviewPanes(PlayerEnginePool(deps.engines))
    val picker = MultiviewPicker(deps.channelDao, deps.epgRepository, deps.clock, scope, deps.zone)

    private val mutableLayer = MutableStateFlow<MultiviewLayer>(MultiviewLayer.Panes)
    val layer: StateFlow<MultiviewLayer> get() = mutableLayer

    private val channels: StateFlow<List<ChannelEntity>> =
        deps.channelDao.observeVisible().stateIn(scope, SharingStarted.Eagerly, emptyList())

    /** Entry teaser: one pane playing the current (last-watched) channel. */
    fun start() {
        scope.launch {
            val list = channels.first { it.isNotEmpty() }
            if (panes.panes.value.isEmpty()) {
                ChannelZapper.restore(list, deps.store.getLong(TuneController.LAST_CHANNEL_KEY))?.let(panes::add)
            }
        }
    }

    /** OK on a pane focuses it and opens its menu. */
    fun onPaneOk(paneId: Int) {
        panes.focus(paneId)
        mutableLayer.value = MultiviewLayer.Menu
    }

    fun menuRows(): List<MultiviewMenuAction> = MultiviewMenu.rows(panes.panes.value.size)

    /**
     * Search and add opens the same picker as Add screen: the reference free
     * build does exactly that (multiview-round 10) and telly's search
     * affordance is a full route with an IME, not embeddable here cheaply.
     */
    fun onMenuAction(action: MultiviewMenuAction) {
        when (action) {
            MultiviewMenuAction.ADD_SCREEN, MultiviewMenuAction.SEARCH_AND_ADD -> openPicker(MultiviewPickerMode.ADD)
            MultiviewMenuAction.CHANGE_CHANNEL -> openPicker(MultiviewPickerMode.CHANGE)
            MultiviewMenuAction.REMOVE_SCREEN -> {
                panes.removeFocused()
                mutableLayer.value = MultiviewLayer.Panes
            }
        }
    }

    /** Picker OK: Add fills the next pane, Change retunes the focused one. */
    fun onPick(channel: ChannelEntity) {
        when ((mutableLayer.value as? MultiviewLayer.Picker)?.mode) {
            MultiviewPickerMode.ADD -> panes.add(channel)
            MultiviewPickerMode.CHANGE -> panes.change(channel)
            null -> return
        }
        mutableLayer.value = MultiviewLayer.Panes
    }

    /** BACK: picker/menu -> panes; panes -> fullscreen on the focused channel. */
    fun onBack() {
        if (mutableLayer.value == MultiviewLayer.Panes) {
            exitToFullscreen()
        } else {
            mutableLayer.value = MultiviewLayer.Panes
        }
    }

    /** CH+/- zap the focused pane in place (panes layer only). */
    fun onChannelKey(delta: Int) {
        if (mutableLayer.value == MultiviewLayer.Panes) panes.zap(channels.value, delta)
    }

    fun close() = panes.releaseAll()

    /** The focused pane's channel becomes the fullscreen channel (no confirm). */
    private fun exitToFullscreen() {
        panes.focused?.channel?.id?.let { deps.store.putLong(TuneController.LAST_CHANNEL_KEY, it) }
        onExit()
    }

    private fun openPicker(mode: MultiviewPickerMode) {
        picker.open(panes.focused?.channel?.id)
        mutableLayer.value = MultiviewLayer.Picker(mode)
    }
}
