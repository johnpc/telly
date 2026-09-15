package com.johncorser.telly.features.vod

import com.johncorser.telly.features.player.Media3PlayerEngine
import com.johncorser.telly.features.vod.db.VodItemDao
import com.johncorser.telly.features.vod.db.VodPositionDao

/**
 * Everything the VOD slice needs from the composition root. The engine
 * comes as a factory so each playback visit gets a fresh ExoPlayer that is
 * released when the screen leaves composition; [rememberPosition] reads the
 * Settings -> Other -> VOD toggle live.
 */
class VodDeps(
    val items: VodItemDao,
    val positions: VodPositionDao,
    val engineFactory: () -> Media3PlayerEngine,
    val rememberPosition: () -> Boolean,
    val clock: () -> Long,
)
