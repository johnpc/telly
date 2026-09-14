package com.johncorser.telly.features.playback

import com.johncorser.telly.features.player.VideoDetails

/** One icon of the playback quick-bar with its live label. */
data class QuickBarItem(
    val action: QuickBarAction,
    val label: String,
)

/** The nine quick-bar slots, left to right (round3-ref 07 uidump). */
enum class QuickBarAction(
    val feature: String,
) {
    SEARCH("Search"),
    CHANNELS_LIST("Channels list"),
    RECORDINGS("Recordings"),
    MULTIVIEW("Multiview"),
    PICTURE_IN_PICTURE("Picture-in-picture"),
    RESOLUTION("Video track"),
    AUDIO("Audio track"),
    LATENCY("Latency"),
    SUBTITLES("Subtitles"),
}

/**
 * Long-OK / MENU at fullscreen opens this bottom icon bar (round3-ref
 * 07/08): Search · Channels list · Recordings · Multiview ·
 * Picture-in-picture · 1280 × 720 · Mono · 0 ms · Off(CC). The three
 * stream slots read their labels live from the tuned stream.
 */
object QuickBar {
    private const val STEREO_CHANNELS = 2
    private const val UNKNOWN = "—"

    fun items(video: VideoDetails?): List<QuickBarItem> =
        QuickBarAction.entries.map { action ->
            QuickBarItem(
                action = action,
                label =
                    when (action) {
                        QuickBarAction.RESOLUTION -> resolution(video)
                        QuickBarAction.AUDIO -> audio(video)
                        QuickBarAction.LATENCY -> "0 ms"
                        QuickBarAction.SUBTITLES -> "Off"
                        else -> action.feature
                    },
            )
        }

    private fun resolution(video: VideoDetails?): String =
        video?.takeIf { it.width > 0 && it.height > 0 }?.let { "${it.width} × ${it.height}" } ?: UNKNOWN

    private fun audio(video: VideoDetails?): String =
        when {
            video == null || video.audioChannels <= 0 -> UNKNOWN
            video.audioChannels == 1 -> "Mono"
            video.audioChannels == STEREO_CHANNELS -> "Stereo"
            else -> "Surround"
        }
}
