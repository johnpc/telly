package com.johncorser.telly.features.player

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer

/**
 * Settings-driven audio behavior injected into [Media3PlayerEngine.create]:
 * "Select surround audio track by default" (read live per tracks change)
 * and "Audio passthrough" (read once at player creation — each visit to a
 * playback surface builds a fresh engine, so a change applies on the next
 * tune-in). Both captured defaults are off = today's behavior.
 */
class PlayerAudioPrefs(
    val surroundByDefault: () -> Boolean = { false },
    val passthrough: () -> Boolean = { false },
)

/**
 * Built here so devices get audio focus and TV-ready defaults.
 * Multiview panes pass [handleAudioFocus] = false: N players each
 * grabbing focus would pause one another, so the pool's engines
 * share the app's focus and the focused pane owns audio by mute
 * state instead. Single fullscreen playback keeps the default.
 */
fun Media3PlayerEngine.Companion.create(
    context: Context,
    handleAudioFocus: Boolean = true,
    audio: PlayerAudioPrefs = PlayerAudioPrefs(),
): Media3PlayerEngine {
    val audioAttributes =
        AudioAttributes
            .Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()
    // Passthrough OFF keeps the stock builder (Media3's automatic capability
    // detection — today's behavior); ON forces the sink to advertise the
    // surround encodings (PassthroughRenderersFactory).
    val builder =
        if (audio.passthrough()) {
            ExoPlayer.Builder(context, PassthroughRenderersFactory(context))
        } else {
            ExoPlayer.Builder(context)
        }
    val player =
        builder
            .setMediaSourceFactory(streamMediaSourceFactory(context))
            .setAudioAttributes(audioAttributes, handleAudioFocus)
            .build()
    return Media3PlayerEngine(player, audio.surroundByDefault)
}
