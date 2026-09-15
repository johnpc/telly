package com.johncorser.telly.features.playback

import com.johncorser.telly.features.recording.RecordingCenter
import com.johncorser.telly.features.recording.RecordingIndicator
import com.johncorser.telly.features.recording.RecordingMenu
import com.johncorser.telly.features.recording.RecordingPrompt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * The playback host's DVR surface: the context sheet's Record rows act
 * through [menu], the transport record dot reads [active] (red while the
 * tuned channel records) and [toggle]s instant record — the exact action
 * behind the sheet's Record row, mirroring TiviMate's transport button.
 */
class PlaybackRecord(
    center: RecordingCenter?,
    private val tuner: TuneController,
    scope: CoroutineScope,
    clock: () -> Long,
    show: (RecordingPrompt) -> Unit,
    private val comingSoon: (String) -> Unit,
) {
    /** The sheet's Record rows act through this (null while no DVR wired). */
    val menu: RecordingMenu? = center?.let { RecordingMenu(it, scope, clock, show) }

    /** True while the tuned channel has an in-progress recording. */
    val active: StateFlow<Boolean> = RecordingIndicator.activeFlow(center?.recordings, tuner.current, scope)

    /** The record dot: start instant record, or offer Stop while recording. */
    fun toggle() {
        val channel = tuner.current.value ?: return
        val recording = menu ?: return comingSoon(PlayerMenuItem.RECORD.label)
        recording.onRecord(channel)
    }
}
