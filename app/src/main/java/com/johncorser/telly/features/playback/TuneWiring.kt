package com.johncorser.telly.features.playback

import com.johncorser.telly.features.catchup.CatchupInfo
import com.johncorser.telly.features.catchup.CatchupPlayback
import com.johncorser.telly.features.history.WatchHistory
import com.johncorser.telly.features.panel.PanelViewModel
import com.johncorser.telly.features.player.external.ExternalPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

// Tuner assembly shared by the playback and guide hosts: both build the
// same PIN-gated TuneController over their PlaybackEnv's hooks.

/** A tuner whose blocked-PIN gate + external handoff come from [env]'s hooks. */
fun gatedTuner(
    env: PlaybackEnv,
    history: WatchHistory,
    scope: CoroutineScope,
    external: ExternalPlayer,
): TuneController =
    TuneController(
        engine = env.engine,
        store = env.store,
        scope = scope,
        channelDao = env.channelDao,
        history = history,
        policies =
            TunePolicies(
                external = external,
                gate = BlockGate(env.hooks.parental, env.hooks.blockSession),
                resolveUrl = env.hooks.resolveUrl,
            ),
    )

/** Catch-up mode over the shared tuner; transport show/pin route as commands. */
internal fun catchupPlayback(
    env: PlaybackEnv,
    tuner: TuneController,
    execute: (PlaybackCommand) -> Unit,
    scope: CoroutineScope,
    exitToGuide: () -> Unit,
): CatchupPlayback =
    CatchupPlayback(
        env = env,
        tuner = tuner,
        showTransport = { execute(PlaybackCommand.ShowTransport) },
        pinTransport = { execute(PlaybackCommand.PinTransport) },
        scope = scope,
        exitToGuide = exitToGuide,
    )

/** The command executor's seams over the ViewModel's clock/nav/catch-up. */
internal fun commandSeams(
    env: PlaybackEnv,
    refreshInstant: () -> Unit,
    exitToGuide: () -> Unit,
    catchup: CatchupPlayback,
): PlaybackCommandSeams =
    PlaybackCommandSeams(
        refreshInstant = refreshInstant,
        exitToGuide = exitToGuide,
        timeouts = env.time.panelTimeouts,
        onLiveTune = catchup::onLiveTune,
    )

/** The live info feed, swapped to the archived programme during catch-up. */
internal fun catchupInfoFeed(
    env: PlaybackEnv,
    tuner: TuneController,
    instant: StateFlow<Long>,
    catchup: CatchupPlayback,
    scope: CoroutineScope,
): StateFlow<PlaybackInfoData?> {
    val live = PlaybackInfoFeed(tuner.current, instant, env.engine.video, env.epgRepository, env.time.style, scope).info
    return CatchupInfo.merged(live, catchup.state, catchup.position, env.time.style, scope)
}

/**
 * The playback host's tune-gate prompt: a verified PIN tunes and shows the
 * zap overlay; cancelling stays put and hands focus back to the panel when
 * it is open.
 */
internal fun panelBlockPrompt(
    tuner: TuneController,
    overlays: OverlayState,
    panel: PanelViewModel,
    showZap: () -> Unit,
): TuneBlockPrompt =
    TuneBlockPrompt(
        tuner = tuner,
        onUnlocked = showZap,
        onDismissed = {
            if (overlays.value == PlaybackOverlay.Panel) panel.openFocusedOn(tuner.current.value?.id)
        },
    )
