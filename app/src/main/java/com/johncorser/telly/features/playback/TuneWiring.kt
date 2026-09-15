package com.johncorser.telly.features.playback

import com.johncorser.telly.features.history.WatchHistory
import com.johncorser.telly.features.panel.PanelViewModel
import com.johncorser.telly.features.player.external.ExternalPlayer
import kotlinx.coroutines.CoroutineScope

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
            ),
    )

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
