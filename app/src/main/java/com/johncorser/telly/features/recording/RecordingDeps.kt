package com.johncorser.telly.features.recording

import com.johncorser.telly.features.player.Media3PlayerEngine
import java.util.TimeZone

/**
 * Everything the Recordings library screen needs from the composition
 * root: the DVR facade plus a player-engine factory for capture playback
 * (fresh engine per playback, released when it leaves composition —
 * the MultiviewScreen engine-usage precedent).
 */
class RecordingDeps(
    val center: RecordingCenter,
    val engineFactory: () -> Media3PlayerEngine,
    val clock: () -> Long,
    val zone: TimeZone = TimeZone.getDefault(),
)
