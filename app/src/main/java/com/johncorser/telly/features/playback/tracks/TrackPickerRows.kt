package com.johncorser.telly.features.playback.tracks

import com.johncorser.telly.features.player.tracks.TrackFacade
import com.johncorser.telly.features.player.tracks.TrackSnapshot

/** One row of a picker dialog; [checked] marks the active option. */
data class TrackPickerRow(
    val id: String,
    val label: String,
    val checked: Boolean = false,
)

/**
 * The rows each picker shows and what selecting one does. Video = "Auto" +
 * every rendition; audio = the stream's audio tracks; CC = "Off" + text
 * tracks; sync = a stepper (±25/50 ms within ±1000 ms) that keeps the
 * dialog open while the others close on pick, like the reference dialogs.
 */
object TrackPickerRows {
    const val AUTO = "auto"
    const val OFF = "off"
    const val SYNC_LIMIT_MS = 1_000L
    private const val RESET = "sync:reset"
    private const val SYNC_PREFIX = "sync:"
    private val SYNC_STEPS_MS = listOf(-50L, -25L, 25L, 50L)

    fun of(
        kind: TrackPickerKind,
        snapshot: TrackSnapshot,
    ): List<TrackPickerRow> =
        when (kind) {
            TrackPickerKind.VIDEO -> videoRows(snapshot)
            TrackPickerKind.AUDIO -> audioRows(snapshot)
            TrackPickerKind.SYNC -> syncRows()
            TrackPickerKind.SUBTITLES -> textRows(snapshot)
        }

    /** Applies [rowId] to [tracks]; true = the picker closes. */
    fun apply(
        kind: TrackPickerKind,
        rowId: String,
        tracks: TrackFacade,
    ): Boolean {
        when (kind) {
            TrackPickerKind.VIDEO -> tracks.selectVideo(rowId.takeUnless { it == AUTO })
            TrackPickerKind.AUDIO -> tracks.selectAudio(rowId)
            TrackPickerKind.SUBTITLES -> tracks.selectText(rowId.takeUnless { it == OFF })
            TrackPickerKind.SYNC -> tracks.setAudioOffsetMs(stepped(rowId, tracks.audioOffsetMs.value))
        }
        return kind != TrackPickerKind.SYNC
    }

    private fun stepped(
        rowId: String,
        current: Long,
    ): Long {
        if (rowId == RESET) return 0L
        val delta = rowId.removePrefix(SYNC_PREFIX).toLongOrNull() ?: return current
        return (current + delta).coerceIn(-SYNC_LIMIT_MS, SYNC_LIMIT_MS)
    }

    private fun videoRows(snapshot: TrackSnapshot): List<TrackPickerRow> =
        listOf(TrackPickerRow(AUTO, "Auto", snapshot.videoOverrideId == null)) +
            snapshot.videos.map { TrackPickerRow(it.id, TrackLabels.video(it), it.id == snapshot.videoOverrideId) }

    private fun audioRows(snapshot: TrackSnapshot): List<TrackPickerRow> =
        snapshot.audios.mapIndexed { index, track ->
            TrackPickerRow(track.id, TrackLabels.audio(track, index), track.id == snapshot.selectedAudioId)
        }

    private fun textRows(snapshot: TrackSnapshot): List<TrackPickerRow> =
        listOf(TrackPickerRow(OFF, "Off", snapshot.selectedTextId == null)) +
            snapshot.texts.mapIndexed { index, track ->
                TrackPickerRow(track.id, TrackLabels.text(track, index), track.id == snapshot.selectedTextId)
            }

    private fun syncRows(): List<TrackPickerRow> =
        SYNC_STEPS_MS.map { TrackPickerRow("$SYNC_PREFIX$it", TrackLabels.sync(it)) } +
            TrackPickerRow(RESET, "Reset")
}
