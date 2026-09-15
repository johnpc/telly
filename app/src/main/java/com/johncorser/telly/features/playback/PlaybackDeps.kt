package com.johncorser.telly.features.playback

import com.johncorser.telly.core.kv.KeyValueStore
import com.johncorser.telly.core.settings.ParentalControls
import com.johncorser.telly.features.catchup.CatchupDeps
import com.johncorser.telly.features.epg.EpgRepository
import com.johncorser.telly.features.history.WatchHistory
import com.johncorser.telly.features.panel.PanelLock
import com.johncorser.telly.features.player.Media3PlayerEngine
import com.johncorser.telly.features.player.PlayerEngine
import com.johncorser.telly.features.playlist.db.ChannelDao
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.TimeZone

/** The injected wall clock + zone (no wall-clock reads in logic). */
class PlaybackTime(
    val clock: () -> Long,
    val zone: TimeZone = TimeZone.getDefault(),
    /** Fires just past each minute boundary of [clock]; tests inject their own. */
    val minuteTicks: Flow<Unit> = minuteBoundaryTicks(clock),
) {
    companion object {
        const val MINUTE_MS = 60_000L

        /** The production tick source for minute-resolution clocks. */
        fun minuteBoundaryTicks(clock: () -> Long): Flow<Unit> =
            flow {
                while (true) {
                    delay(MINUTE_MS - clock() % MINUTE_MS)
                    emit(Unit)
                }
            }
    }
}

/** The Room-backed feeds the playback and guide slices read. */
class PlaybackSources(
    val channelDao: ChannelDao,
    val epgRepository: EpgRepository,
    val history: WatchHistory,
)

/** Cross-slice hooks the playback surface plugs into (nav + parental + catch-up). */
class PlaybackHooks(
    val panelLock: PanelLock = PanelLock(),
    val onOpenSettings: () -> Unit = {},
    val onOpenMultiview: () -> Unit = {},
    val catchup: CatchupDeps = CatchupDeps(),
)

/** Everything `PlaybackViewModel` needs injected, bundled for readability. */
class PlaybackEnv(
    val channelDao: ChannelDao,
    val epgRepository: EpgRepository,
    val engine: PlayerEngine,
    val store: KeyValueStore,
    val time: PlaybackTime,
    val hooks: PlaybackHooks = PlaybackHooks(),
)

/**
 * Everything the playback screen needs from the composition root. The engine
 * comes as a factory so each visit to the playback route gets a fresh
 * ExoPlayer that is released when the screen leaves composition.
 */
class PlaybackDeps(
    val sources: PlaybackSources,
    val keyValueStore: KeyValueStore,
    val engineFactory: () -> Media3PlayerEngine,
    val clock: () -> Long,
    val parental: ParentalControls? = null,
    val catchup: CatchupDeps = CatchupDeps(),
)
