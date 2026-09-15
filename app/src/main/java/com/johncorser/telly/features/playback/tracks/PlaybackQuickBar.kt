package com.johncorser.telly.features.playback.tracks

import com.johncorser.telly.features.playback.OverlayState
import com.johncorser.telly.features.playback.QuickBar
import com.johncorser.telly.features.playback.QuickBarAction
import com.johncorser.telly.features.playback.QuickBarItem
import com.johncorser.telly.features.player.PlayerEngine

/**
 * Quick-bar slot dispatch + live slot labels, split out of the ViewModel:
 * Search, Channels list and Multiview route to their surfaces; the four
 * stream slots open their track pickers (ux-spec §3.14); everything else is
 * a later slice's branded placeholder.
 */
class PlaybackQuickBar(
    private val engine: PlayerEngine,
    overlays: OverlayState,
    private val comingSoon: (String) -> Unit,
    private val openPanel: () -> Unit,
    private val openSearch: () -> Unit,
    private val openMultiview: () -> Unit,
) {
    /** Video / audio / audio-sync / CC picker state machine. */
    val pickers = TrackPickerController(engine.tracks, overlays)

    fun onItem(action: QuickBarAction) {
        when (action) {
            QuickBarAction.CHANNELS_LIST -> openPanel()
            QuickBarAction.SEARCH -> openSearch()
            QuickBarAction.MULTIVIEW -> openMultiview()
            in TrackPickerKind.actions -> pickers.open(action)
            else -> comingSoon(action.feature)
        }
    }

    /** The nine quick-bar slots with live stream labels (round3-ref 07). */
    fun items(): List<QuickBarItem> = QuickBar.items(engine.video.value, pickers.syncLabel(), pickers.subtitleLabel())
}
