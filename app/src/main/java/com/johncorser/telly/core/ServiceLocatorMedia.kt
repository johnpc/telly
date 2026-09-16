package com.johncorser.telly.core

import android.content.Context
import com.johncorser.telly.core.settings.TellySettings
import com.johncorser.telly.features.multiview.MultiviewDeps
import com.johncorser.telly.features.playback.PlaybackTime
import com.johncorser.telly.features.player.Media3PlayerEngine
import com.johncorser.telly.features.vod.VodDeps

/**
 * Multiview slice: one independent engine per pane from the factory. The
 * pane engines do NOT handle audio focus (N players grabbing focus pause
 * one another); the focused pane owns audio via mute state instead.
 */
fun ServiceLocator.multiviewDeps(context: Context): MultiviewDeps =
    MultiviewDeps(
        channelDao = visibleChannelDao(context),
        epgRepository = epgRepository(context),
        engines = {
            Media3PlayerEngine.create(
                context.applicationContext,
                handleAudioFocus = false,
                userAgentFor = streamUserAgentFor(context),
            )
        },
        store = keyValueStore(context),
        time = PlaybackTime(clock = clock, style = clockStyleOf(settingsRepository(context))),
        resolveUrl = proxyResolve(settingsRepository(context)),
    )

/** VOD slice: the Movies browser + seekable playback with resume. */
fun ServiceLocator.vodDeps(context: Context): VodDeps =
    VodDeps(
        items = database(context).vodItemDao(),
        positions = database(context).vodPositionDao(),
        engineFactory = {
            Media3PlayerEngine.create(context.applicationContext, userAgentFor = streamUserAgentFor(context))
        },
        rememberPosition = { settingsRepository(context).get(TellySettings.VOD_REMEMBER_POSITION) },
        clock = clock,
    )
