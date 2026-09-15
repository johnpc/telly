package com.johncorser.telly.features.guide

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.johncorser.telly.features.playback.PlaybackEnv
import com.johncorser.telly.features.playback.PlaybackHooks
import com.johncorser.telly.features.playback.PlaybackTime
import com.johncorser.telly.features.player.Media3PlayerEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/** Builds the guide's controller over [engine]; closes it on dispose. */
@Composable
internal fun rememberGuideController(
    deps: GuideDeps,
    engine: Media3PlayerEngine,
    callbacks: GuideCallbacks,
): GuideController {
    // A dedicated main-thread scope instead of rememberCoroutineScope(): the
    // controller drives ExoPlayer (main-thread-affine), so its coroutines
    // must not resume on the composition's frame clock (see PlaybackScreen).
    val scope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate) }
    val controller =
        remember {
            GuideController(
                env =
                    PlaybackEnv(
                        channelDao = deps.playback.sources.channelDao,
                        epgRepository = deps.playback.sources.epgRepository,
                        engine = engine,
                        store = deps.playback.keyValueStore,
                        time = PlaybackTime(deps.playback.clock),
                        hooks = PlaybackHooks(catchup = deps.playback.catchup),
                    ),
                history = deps.playback.sources.history,
                pastDays = deps.pastDays,
                scope = scope,
                callbacks = callbacks,
            )
        }
    DisposableEffect(Unit) {
        onDispose {
            controller.close()
            scope.cancel()
        }
    }
    return controller
}
