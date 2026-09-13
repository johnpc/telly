package com.johncorser.telly.features.playback

import com.johncorser.telly.features.player.VideoDetails
import kotlin.math.roundToInt

/**
 * Info-overlay badge derivation from the Media3 stream formats. Capture 34
 * shows "HD", "25 FPS", "MONO" for the 1280x720 25fps mono fixture; unknown
 * dimensions simply drop their badge.
 */
object PlaybackBadges {
    private const val UHD_HEIGHT = 2160
    private const val FHD_HEIGHT = 1080
    private const val HD_HEIGHT = 720
    private const val STEREO_CHANNELS = 2

    fun badges(video: VideoDetails?): List<String> =
        video?.let {
            listOfNotNull(resolution(it.height), fps(it.frameRate), audio(it.audioChannels))
        } ?: emptyList()

    private fun resolution(height: Int): String? =
        when {
            height <= 0 -> null
            height >= UHD_HEIGHT -> "UHD"
            height >= FHD_HEIGHT -> "FHD"
            height >= HD_HEIGHT -> "HD"
            else -> "SD"
        }

    private fun fps(frameRate: Float): String? = if (frameRate > 0f) "${frameRate.roundToInt()} FPS" else null

    private fun audio(channels: Int): String? =
        when {
            channels <= 0 -> null
            channels == 1 -> "MONO"
            channels == STEREO_CHANNELS -> "STEREO"
            else -> "SURROUND"
        }
}
