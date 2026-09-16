package com.johncorser.telly.features.playback

import com.johncorser.telly.core.kv.KeyValueStore
import com.johncorser.telly.core.settings.ParentalControls
import com.johncorser.telly.features.epg.EpgRepository
import com.johncorser.telly.features.history.WatchHistory
import com.johncorser.telly.features.player.Media3PlayerEngine
import com.johncorser.telly.features.playlist.db.ChannelDao
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.TimeZone

/** The injected wall clock + clock style (no wall-clock reads in logic). */
class PlaybackTime(
    val clock: () -> Long,
    val style: ClockStyle = ClockStyle(),
    /** Fires just past each minute boundary of [clock]; tests inject their own. */
    val minuteTicks: Flow<Unit> = minuteBoundaryTicks(clock),
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
)

/**
 * The settings-driven playback behaviors, read live from the store:
 * the UDP-proxy stream rewrite and the 12/24-hour clock format. Defaults
 * (identity, 12-hour) preserve the previous behavior exactly.
 */
class PlaybackTuning(
    val resolveUrl: (String) -> String = { it },
    val is24h: () -> Boolean = { false },
) {
    /** The clock style the playback/guide/history clocks render with. */
    fun clockStyle(): ClockStyle = ClockStyle(h24 = is24h)
}

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
    val tuning: PlaybackTuning = PlaybackTuning(),
)
