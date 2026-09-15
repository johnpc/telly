package com.johncorser.telly.features.multiview

import com.johncorser.telly.core.kv.KeyValueStore
import com.johncorser.telly.features.epg.EpgRepository
import com.johncorser.telly.features.playback.BlockGate
import com.johncorser.telly.features.playback.PlaybackTime
import com.johncorser.telly.features.player.PlayerEngineFactory
import com.johncorser.telly.features.playlist.db.ChannelDao
import java.util.TimeZone

/**
 * Everything the multiview screen needs from the composition root. The
 * engine factory yields one independent player per pane (no per-engine
 * audio focus — see Media3PlayerEngine.create); the pool built over it is
 * owned by the screen so every pane's codec is released on exit. The gate
 * shares the app-wide BlockSession so multiview honors the same relock
 * semantics as the playback and guide tuners.
 */
class MultiviewDeps(
    val channelDao: ChannelDao,
    val epgRepository: EpgRepository,
    val engines: PlayerEngineFactory,
    val store: KeyValueStore,
    /** Injected clock + zone, the same bundle the playback slice carries. */
    val time: PlaybackTime,
    /** The blocked-channel PIN gate every multiview tune path passes. */
    val gate: BlockGate = BlockGate(),
) {
    val clock: () -> Long get() = time.clock
    val zone: TimeZone get() = time.zone
}
