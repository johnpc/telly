package com.johncorser.telly.core

import android.content.Context
import com.johncorser.telly.core.settings.ParentalControls
import com.johncorser.telly.core.settings.TellySettings
import com.johncorser.telly.features.multiview.MultiviewDeps
import com.johncorser.telly.features.multiview.MultiviewTune
import com.johncorser.telly.features.playback.BlockGate
import com.johncorser.telly.features.playback.PlaybackTime
import com.johncorser.telly.features.vod.VodDeps

/**
 * Multiview slice: one independent engine per pane from the factory. The
 * pane engines do NOT handle audio focus (N players grabbing focus pause
 * one another); the focused pane owns audio via mute state instead. The
 * gate shares [sharedBlockSession] so multiview honors the same
 * "Until app restart" relock as the playback and guide tuners.
 */
fun ServiceLocator.multiviewDeps(context: Context): MultiviewDeps =
    MultiviewDeps(
        channelDao = visibleChannelDao(context),
        epgRepository = epgRepository(context),
        store = keyValueStore(context),
        time = PlaybackTime(clock = clock, style = clockStyleOf(settingsRepository(context))),
        tune =
            MultiviewTune(
                engines = { tunedEngine(context, handleAudioFocus = false) },
                resolveUrl = proxyResolve(settingsRepository(context)),
                gate = BlockGate(ParentalControls(settingsRepository(context)), sharedBlockSession),
            ),
    )

/** VOD slice: the Movies browser + seekable playback with resume. */
fun ServiceLocator.vodDeps(context: Context): VodDeps =
    VodDeps(
        items = database(context).vodItemDao(),
        positions = database(context).vodPositionDao(),
        engineFactory = { tunedEngine(context) },
        rememberPosition = { settingsRepository(context).get(TellySettings.VOD_REMEMBER_POSITION) },
        clock = clock,
    )
