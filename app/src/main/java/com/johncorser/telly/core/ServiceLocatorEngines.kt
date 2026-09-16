package com.johncorser.telly.core

import android.content.Context
import com.johncorser.telly.features.player.Media3PlayerEngine
import com.johncorser.telly.features.player.PlayerTuning

/**
 * The one engine builder every slice's factory calls: per-playlist user
 * agent plus the Playback settings ([PlayerTuning] — Buffer size and the
 * decoder preferences) read at CREATE time. Engines are built per screen,
 * so a changed setting applies the next time a screen builds its engine.
 */
internal fun ServiceLocator.tunedEngine(
    context: Context,
    handleAudioFocus: Boolean = true,
): Media3PlayerEngine =
    Media3PlayerEngine.create(
        context.applicationContext,
        handleAudioFocus = handleAudioFocus,
        userAgentFor = streamUserAgentFor(context),
        tuning = PlayerTuning.from(settingsRepository(context)),
    )
