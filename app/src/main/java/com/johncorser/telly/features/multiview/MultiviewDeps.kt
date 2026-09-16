package com.johncorser.telly.features.multiview

import com.johncorser.telly.core.kv.KeyValueStore
import com.johncorser.telly.features.epg.EpgRepository
import com.johncorser.telly.features.playback.BlockGate
import com.johncorser.telly.features.playback.PlaybackTime
import com.johncorser.telly.features.player.PlayerEngineFactory
import com.johncorser.telly.features.playlist.db.ChannelDao

/**
 * The tune seam every pane tune passes: the engine factory yields one
 * independent player per pane (no per-engine audio focus — see
 * Media3PlayerEngine.create), [resolveUrl] applies the UDP-proxy rewrite
 * and [gate] is the blocked-channel PIN gate, sharing the app-wide
 * BlockSession so multiview honors the same relock semantics as the
 * playback and guide tuners.
 */
class MultiviewTune(
    val engines: PlayerEngineFactory,
    /** Stream-URL resolution (UDP-proxy rewrite) applied at pane tunes. */
    val resolveUrl: (String) -> String = { it },
    /** The blocked-channel PIN gate every multiview tune path passes. */
    val gate: BlockGate = BlockGate(),
)

/**
 * Everything the multiview screen needs from the composition root. The
 * pane engine pool built over [MultiviewTune.engines] is owned by the
 * screen so every pane's codec is released on exit.
 */
class MultiviewDeps(
    val channelDao: ChannelDao,
    val epgRepository: EpgRepository,
    val store: KeyValueStore,
    /** Injected clock + zone + the persisted 12/24-hour choice (picker times). */
    val time: PlaybackTime,
    val tune: MultiviewTune,
) {
    val engines: PlayerEngineFactory get() = tune.engines
    val resolveUrl: (String) -> String get() = tune.resolveUrl
    val gate: BlockGate get() = tune.gate
}
