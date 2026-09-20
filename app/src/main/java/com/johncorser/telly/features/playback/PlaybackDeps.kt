package com.johncorser.telly.features.playback

import com.johncorser.telly.core.kv.KeyValueStore
import com.johncorser.telly.core.settings.ParentalControls
import com.johncorser.telly.features.epg.EpgRepository
import com.johncorser.telly.features.history.WatchHistory
import com.johncorser.telly.features.mylist.InMemoryMyListStore
import com.johncorser.telly.features.mylist.MyListStore
import com.johncorser.telly.features.player.Media3PlayerEngine
import com.johncorser.telly.features.player.SharedPlayerEngine
import com.johncorser.telly.features.playlist.db.ChannelDao
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.TimeZone

/** The injected wall clock + clock style + overlay timing (no wall-clock reads in logic). */
class PlaybackTime(
    val clock: () -> Long,
    val style: ClockStyle = ClockStyle(),
    /** Fires just past each minute boundary of [clock]; tests inject their own. */
    val minuteTicks: Flow<Unit> = minuteBoundaryTicks(clock),
    /** Appearance -> Player -> Panels timeout, sec (5 = today's constants). */
    val panelTimeouts: () -> PanelTimeouts = { PanelTimeouts.DEFAULT },
) {
    val zone: TimeZone get() = style.zone

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
    /** Saved My-list programmes (Room on device, in-memory in tests). */
    val myList: MyListStore = InMemoryMyListStore(),
)

/**
 * Everything the playback screen needs from the composition root. The engine
 * comes as a factory, but the guide and fullscreen playback lease one shared
 * build through [engines] so the stream survives the transition between them;
 * the ExoPlayer is released when the last of the two leaves composition.
 */
class PlaybackDeps(
    val sources: PlaybackSources,
    val keyValueStore: KeyValueStore,
    val engineFactory: () -> Media3PlayerEngine,
    /** Clock + zone + panel-timeout provider (Appearance -> Player). */
    val time: PlaybackTime,
    val parental: ParentalControls? = null,
    /** MainActivity's platform hooks (AFR + external player); screens copy nav lambdas in. */
    val hooks: PlaybackHooks = PlaybackHooks(),
) {
    val clock: () -> Long get() = time.clock

    /** The one engine the guide preview and fullscreen playback lease. */
    val engines: SharedPlayerEngine<Media3PlayerEngine> = SharedPlayerEngine(engineFactory)

    /** Saved My-list programmes; shared with the guide via [sources]. */
    val myList: MyListStore get() = sources.myList
}
