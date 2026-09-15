package com.johncorser.telly.features.playback.tracks

import com.johncorser.telly.features.playback.QuickBarAction

/**
 * The four quick-bar stream slots that open a picker dialog over playback
 * (ux-spec §3.14: video track, audio track, audio-sync offset, CC).
 */
enum class TrackPickerKind(
    val action: QuickBarAction,
    val title: String,
) {
    VIDEO(QuickBarAction.RESOLUTION, "Video track"),
    AUDIO(QuickBarAction.AUDIO, "Audio track"),
    SYNC(QuickBarAction.LATENCY, "Audio sync"),
    SUBTITLES(QuickBarAction.SUBTITLES, "Closed captions"),
    ;

    companion object {
        /** The quick-bar actions routed to a picker instead of a placeholder. */
        val actions: Set<QuickBarAction> = entries.map { it.action }.toSet()

        fun of(action: QuickBarAction): TrackPickerKind = entries.first { it.action == action }
    }
}
