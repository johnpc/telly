package com.johncorser.telly.core

import android.content.Context
import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.core.settings.TellySettings
import com.johncorser.telly.features.player.Media3PlayerEngine
import com.johncorser.telly.features.player.PlayerAudioPrefs
import com.johncorser.telly.features.player.PlayerTuning

/** Surround-by-default + passthrough, read from the store per engine. */
private fun audioPrefs(settings: SettingsRepository): PlayerAudioPrefs =
    PlayerAudioPrefs(
        surroundByDefault = { settings.get(TellySettings.SURROUND_BY_DEFAULT) },
        passthrough = { settings.get(TellySettings.AUDIO_PASSTHROUGH) },
    )

/**
 * The one engine builder every slice's factory calls: per-playlist user
 * agent, the audio settings ([PlayerAudioPrefs] — passthrough + surround
 * by default) and the Playback settings ([PlayerTuning] — Buffer size and
 * the decoder preferences), all read at CREATE time. Engines are built per
 * lease cycle (guide + fullscreen share one; VOD, recordings and multiview
 * build their own), so a changed setting applies on the next build.
 */
internal fun ServiceLocator.tunedEngine(
    context: Context,
    handleAudioFocus: Boolean = true,
): Media3PlayerEngine =
    Media3PlayerEngine.create(
        context.applicationContext,
        handleAudioFocus = handleAudioFocus,
        userAgentFor = streamUserAgentFor(context),
        audio = audioPrefs(settingsRepository(context)),
        tuning = PlayerTuning.from(settingsRepository(context)),
    )
