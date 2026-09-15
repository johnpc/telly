package com.johncorser.telly.features.playback.tracks

import com.johncorser.telly.features.playback.OverlayState
import com.johncorser.telly.features.playback.PlaybackOverlay
import com.johncorser.telly.features.playback.QuickBarAction
import com.johncorser.telly.features.player.tracks.TrackFacade

/**
 * State machine of the quick-bar track pickers: opening a slot swaps the
 * quick-bar for the sticky picker overlay (no auto-hide — BACK or a pick
 * closes it), rows come from the engine's live track snapshot, and the
 * quick-bar's stream slots read their labels back through it.
 */
class TrackPickerController(
    val tracks: TrackFacade,
    private val overlays: OverlayState,
) {
    fun open(action: QuickBarAction) = overlays.set(PlaybackOverlay.TrackPicker(TrackPickerKind.of(action)))

    fun rows(kind: TrackPickerKind): List<TrackPickerRow> = TrackPickerRows.of(kind, tracks.snapshot.value)

    fun onRow(
        kind: TrackPickerKind,
        rowId: String,
    ) {
        if (TrackPickerRows.apply(kind, rowId, tracks)) overlays.set(PlaybackOverlay.None)
    }

    /** The quick-bar's audio-sync slot label ("0 ms", "+150 ms"). */
    fun syncLabel(): String = TrackLabels.sync(tracks.audioOffsetMs.value)

    /** The quick-bar's CC slot label ("Off" or the enabled track). */
    fun subtitleLabel(): String = TrackLabels.subtitleSlot(tracks.snapshot.value)
}
