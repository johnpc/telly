package com.johncorser.telly.features.playback.tracks

import com.johncorser.telly.features.player.tracks.AudioTrack
import com.johncorser.telly.features.player.tracks.TextTrack
import com.johncorser.telly.features.player.tracks.TrackSnapshot
import com.johncorser.telly.features.player.tracks.VideoTrack
import java.util.Locale

/** Pure label derivation for the picker rows and the quick-bar slots. */
object TrackLabels {
    private const val STEREO_CHANNELS = 2
    private const val BITS_PER_MEGABIT = 1_000_000.0

    /** "1920×1080, 5.2 Mbps"; parts the container omits are dropped. */
    fun video(track: VideoTrack): String {
        if (track.width <= 0 || track.height <= 0) return "Video"
        val size = "${track.width}×${track.height}"
        if (track.bitrate <= 0) return size
        return size + ", " + String.format(Locale.US, "%.1f Mbps", track.bitrate / BITS_PER_MEGABIT)
    }

    /** "English · Stereo"; undeclared languages fall back to "Audio N". */
    fun audio(
        track: AudioTrack,
        index: Int,
    ): String = language(track.language, "Audio ${index + 1}") + channelsSuffix(track.channels)

    /** "English"; undeclared languages fall back to "Subtitles N". */
    fun text(
        track: TextTrack,
        index: Int,
    ): String = language(track.language, "Subtitles ${index + 1}")

    /** The audio-sync slot/stepper label: "0 ms", "+150 ms", "-150 ms". */
    fun sync(offsetMs: Long): String = if (offsetMs > 0) "+$offsetMs ms" else "$offsetMs ms"

    /** The CC quick-bar slot: "Off" until a text track is enabled. */
    fun subtitleSlot(snapshot: TrackSnapshot): String {
        val index = snapshot.texts.indexOfFirst { it.id == snapshot.selectedTextId }
        return if (index >= 0) text(snapshot.texts[index], index) else "Off"
    }

    private fun channelsSuffix(count: Int): String =
        when {
            count <= 0 -> ""
            count == 1 -> " · Mono"
            count == STEREO_CHANNELS -> " · Stereo"
            else -> " · Surround"
        }

    private fun language(
        code: String?,
        fallback: String,
    ): String {
        if (code.isNullOrBlank() || code == "und") return fallback
        val display = Locale.forLanguageTag(code).displayLanguage
        val name = if (display.isBlank()) code else display
        return name.replaceFirstChar { it.uppercase(Locale.US) }
    }
}
