package com.johncorser.telly.features.catchup

import com.johncorser.telly.features.history.WatchHistory
import com.johncorser.telly.features.playback.ClockStyle
import com.johncorser.telly.features.playback.PlaybackEnv
import com.johncorser.telly.features.playback.PlaybackHooks
import com.johncorser.telly.features.playback.PlaybackKey
import com.johncorser.telly.features.playback.PlaybackOverlay
import com.johncorser.telly.features.playback.PlaybackTime
import com.johncorser.telly.features.playback.TuneController
import com.johncorser.telly.testutil.FakeChannelDao
import com.johncorser.telly.testutil.FakeKeyValueStore
import com.johncorser.telly.testutil.FakePlayerEngine
import com.johncorser.telly.testutil.FakeProgramDao
import com.johncorser.telly.testutil.FakeWatchHistoryDao
import com.johncorser.telly.testutil.testChannel
import com.johncorser.telly.testutil.testEpgRepository
import com.johncorser.telly.testutil.testProgram
import com.johncorser.telly.testutil.withCatchup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

/** Transport ⏮/⏭ programme hops wired through the catch-up mode. */
@OptIn(ExperimentalCoroutinesApi::class)
class CatchupHopWiringTest {
    private val hourMs = 3_600_000L
    private val now = 100 * hourMs
    private val channel =
        testChannel(1, 1, "News One").withCatchup(source = "http://s/arc?utc={utc}&d={duration}", days = 7)
    private val dao = FakeChannelDao(listOf(channel))
    private val engine = FakePlayerEngine()
    private val store = FakeKeyValueStore()
    private val programs =
        FakeProgramDao(
            listOf(
                testProgram("tvg-1", now - 6 * hourMs, now - 5 * hourMs, "Prev Show"),
                testProgram("tvg-1", now - 5 * hourMs, now - 4 * hourMs, "Current Archive"),
                testProgram("tvg-1", now - 4 * hourMs, now - 3 * hourMs, "Next Show"),
                testProgram("tvg-1", now - hourMs, now + hourMs, "Airing Now"),
            ),
        )
    private val session = CatchupSession()
    private var transports = 0
    private var exits = 0

    private val request =
        CatchupRequest(channel, "http://s/arc?utc=1&d=2", "Current Archive", now - 5 * hourMs, now - 4 * hourMs)

    private fun TestScope.build(): Pair<CatchupPlayback, TuneController> {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val env =
            PlaybackEnv(
                channelDao = dao,
                epgRepository = testEpgRepository(programs),
                engine = engine,
                store = store,
                time = PlaybackTime({ now }, ClockStyle(TimeZone.getTimeZone("UTC"))),
                hooks = PlaybackHooks(catchup = CatchupDeps(session = session)),
            )
        val tuner = TuneController(engine, store, scope, dao, WatchHistory(FakeWatchHistoryDao()) { now })
        val playback = CatchupPlayback(env, tuner, { transports += 1 }, {}, scope) { exits += 1 }
        return playback to tuner
    }

    @Test
    fun `previous hops to the earlier programme's archive`() =
        runTest {
            session.set(request)
            val (catchup, _) = build()
            catchup.resumePending()

            catchup.hop.previous()

            val state = catchup.state.value
            assertEquals("Prev Show", state?.request?.title)
            assertEquals(now - 6 * hourMs, state?.request?.startMs)
            assertTrue(engine.loaded.last().startsWith("http://s/arc?utc=${(now - 6 * hourMs) / 1000}"))
            // The hop resets the transport's position readout to the new start.
            assertEquals(0L, catchup.position.value)
        }

    @Test
    fun `next hops to the later programme's archive and keeps the entry origin`() =
        runTest {
            session.set(request)
            val (catchup, _) = build()
            catchup.resumePending()

            catchup.hop.next()

            val state = catchup.state.value
            assertEquals("Next Show", state?.request?.title)
            assertEquals(false, state?.fromLive)

            // BACK still returns where catch-up was entered from (the guide).
            assertTrue(catchup.keys.onKey(PlaybackOverlay.None, PlaybackKey.BACK))
            assertEquals(1, exits)
        }

    @Test
    fun `next past the newest archive returns to live`() =
        runTest {
            session.set(CatchupRequest(channel, "http://s/arc?utc=1", "Newest", now - 2 * hourMs, now - hourMs))
            programs.programs.value =
                listOf(
                    testProgram("tvg-1", now - 2 * hourMs, now - hourMs, "Newest Archive"),
                    testProgram("tvg-1", now - hourMs, now + hourMs, "Airing Now"),
                )
            val (catchup, tuner) = build()
            catchup.resumePending()

            catchup.hop.next()

            assertNull(catchup.state.value)
            assertEquals(channel.source.streamUrl, engine.loaded.last())
            assertEquals(channel.id, tuner.current.value?.id)
            assertEquals(0, exits)
        }

    @Test
    fun `previous at the horizon's edge stays on the current archive`() =
        runTest {
            programs.programs.value = listOf(testProgram("tvg-1", now - 5 * hourMs, now - 4 * hourMs, "Only One"))
            session.set(request)
            val (catchup, _) = build()
            catchup.resumePending()
            val loads = engine.loaded.size

            catchup.hop.previous()

            assertEquals(request, catchup.state.value?.request)
            assertEquals(loads, engine.loaded.size)
        }

    @Test
    fun `hops are inert during live playback`() =
        runTest {
            val (catchup, tuner) = build()
            tuner.tune(channel)
            val loads = engine.loaded.size

            catchup.hop.previous()
            catchup.hop.next()

            assertNull(catchup.state.value)
            assertEquals(loads, engine.loaded.size)
        }
}
