package com.johncorser.telly.features.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember

/**
 * Leases the shared live-TV engine for this composition, releasing the
 * lease on dispose. The guide preview and fullscreen playback both lease
 * the same build, and their route crossfade overlaps the two screens, so
 * the stream survives the hand-over instead of re-tuning.
 */
@Composable
fun rememberLeasedEngine(engines: SharedPlayerEngine<Media3PlayerEngine>): Media3PlayerEngine {
    val engine = remember { engines.acquire() }
    DisposableEffect(Unit) { onDispose { engines.release() } }
    return engine
}
