package com.johncorser.telly.features.playback

import com.johncorser.telly.features.player.VideoDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * AFR feed: pushes every known (non-zero) content frame rate from the
 * engine's video details into [PlaybackHooks.onFrameRateChanged], where
 * MainActivity switches the display mode. Unknown rates (0, before the
 * TS render estimate lands) never reach the hook.
 */
internal fun feedFrameRates(
    video: StateFlow<VideoDetails?>,
    scope: CoroutineScope,
    onFrameRate: (Float) -> Unit,
) {
    scope.launch {
        video.collect { details -> details?.frameRate?.takeIf { it > 0f }?.let(onFrameRate) }
    }
}
