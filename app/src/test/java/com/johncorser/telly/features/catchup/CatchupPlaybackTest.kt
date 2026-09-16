package com.johncorser.telly.features.catchup

import com.johncorser.telly.features.history.WatchHistory
import com.johncorser.telly.features.playback.ClockStyle
import com.johncorser.telly.features.playback.PlaybackEnv
import com.johncorser.telly.features.playback.PlaybackHooks
import com.johncorser.telly.features.playback.PlaybackKey
import com.johncorser.telly.features.playback.PlaybackOverlay
import com.johncorser.telly.features.playback.PlaybackTime
import com.johncorser.telly.features.playback.TuneController
import com.johncorser.telly.features.player.PlayerState
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

@OptIn(ExperimentalCoroutinesApi::class)
class CatchupPlaybackTest {
    private val hourMs = 3_600_000L
    private val now = 100 * hourMs
    private val catchupChannel =
        testChannel(1, 1, "News One").withCatchup(source = "http://s/arc?utc={utc}&d={duration}", days = 7)
    private val plainChannel = testChannel(2, 2, "Plain Two")
    private val dao = FakeChannelDao(listOf(catchupChannel, plainChannel))
    private val engine = FakePlayerEngine()
    private val store = FakeKeyValueStore()
    private val programs =
        FakeProgramDao(listOf(testProgram("tvg-1", now - hourMs, now + hourMs, "Airing Now")))
    private val session = CatchupSession()
    private var transports = 0
    private var pins = 0
    private var exits = 0

    private val request =
        CatchupRequest(
            channel = catchupChannel,
            url = "http://s/arc?utc=1&d=2",
            title = "Yesterday's Show",
            startMs = now - 5 * hourMs,
            endMs = now - 4 * hourMs,
        )

    private fun TestScope.build(toggles: CatchupToggles = CatchupToggles()): Pair<CatchupPlayback, TuneController> {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val env =
            PlaybackEnv(
                channelDao = dao,
                epgRepository = testEpgRepository(programs),
                engine = engine,
                store = store,
                time = PlaybackTime({ now }, ClockStyle(TimeZone.getTimeZone("UTC"))),
                hooks = PlaybackHooks(catchup = CatchupDeps(session = session, toggles = toggles)),
            )
        val tuner = TuneController(engine, store, scope, dao, WatchHistory(FakeWatchHistoryDao()) { now })
        val playback = CatchupPlayback(env, tuner, { transports += 1 }, { pins += 1 }, scope) { exits += 1 }
        return playback to tuner
    }

    @Test
    fun `a pending guide request tunes the catch-up URL and shows the transport`() =
        runTest {
            session.set(request)
            val (catchup, tuner) = build()

            assertTrue(catchup.resumePending())

            assertEquals(listOf(request.url), engine.loaded)
            assertEquals(catchupChannel.id, tuner.current.value?.id)
            assertEquals(1, transports)
            assertEquals(request, catchup.state.value?.request)
            assertEquals(catchupChannel.id, store.values[TuneController.LAST_CHANNEL_KEY])
        }

    @Test
    fun `without a pending request the live tune stays in charge`() =
        runTest {
            val (catchup, _) = build()

            assertFalse(catchup.resumePending())
            assertTrue(engine.loaded.isEmpty())
        }

    @Test
    fun `seeks clamp to the programme bounds`() =
        runTest {
            session.set(request)
            val (catchup, _) = build()
            catchup.resumePending()

            engine.position = 30 * 60_000L
            catchup.seekBy(CatchupSkip.DEFAULT_FORWARD_MS)
            assertEquals(30 * 60_000L + CatchupSkip.DEFAULT_FORWARD_MS, catchup.position.value)

            engine.position = 5_000L
            catchup.seekBy(-CatchupSkip.DEFAULT_BACK_MS)
            assertEquals(0L, catchup.position.value)

            engine.position = request.durationMs - 1_000L
            catchup.seekBy(CatchupSkip.DEFAULT_FORWARD_MS)
            assertEquals(request.durationMs, catchup.position.value)
        }

    @Test
    fun `the overlay's ticks refresh the position from the engine`() =
        runTest {
            session.set(request)
            val (catchup, _) = build()
            catchup.resumePending()

            engine.position = 42_000L
            catchup.refreshPosition()

            assertEquals(42_000L, catchup.position.value)
        }

    @Test
    fun `position refreshes are inert outside catch-up mode`() =
        runTest {
            val (catchup, tuner) = build()
            tuner.tune(catchupChannel)
            engine.position = 42_000L

            catchup.refreshPosition()

            assertEquals(0L, catchup.position.value)
        }

    @Test
    fun `back from a guide entry exits to the guide`() =
        runTest {
            session.set(request)
            val (catchup, _) = build()
            catchup.resumePending()

            assertTrue(catchup.keys.onKey(PlaybackOverlay.None, PlaybackKey.BACK))

            assertNull(catchup.state.value)
            assertEquals(1, exits)
        }

    @Test
    fun `rewind-live enters catch-up of the airing programme near the live edge`() =
        runTest {
            val toggles = CatchupToggles { setting -> setting.key == "remote_rw_rewinds_live" || setting.default }
            val (catchup, tuner) = build(toggles)
            tuner.tune(catchupChannel)

            assertTrue(catchup.keys.onKey(PlaybackOverlay.None, PlaybackKey.REWIND))

            val state = catchup.state.value
            assertTrue(state?.fromLive == true)
            assertEquals(now - hourMs, state?.request?.startMs)
            // Position = live edge − skip step, relative to the programme start.
            assertEquals(hourMs - CatchupSkip.DEFAULT_BACK_MS, catchup.position.value)
        }

    @Test
    fun `back from a rewind-live entry returns to the live stream`() =
        runTest {
            val toggles = CatchupToggles { setting -> setting.key == "remote_rw_rewinds_live" || setting.default }
            val (catchup, tuner) = build(toggles)
            tuner.tune(catchupChannel)
            catchup.keys.onKey(PlaybackOverlay.None, PlaybackKey.REWIND)

            assertTrue(catchup.keys.onKey(PlaybackOverlay.None, PlaybackKey.BACK))

            assertNull(catchup.state.value)
            assertEquals(0, exits)
            assertEquals(catchupChannel.source.streamUrl, engine.loaded.last())
        }

    @Test
    fun `seek keys fall through on a channel without catch-up`() =
        runTest {
            val (catchup, tuner) = build()
            tuner.tune(plainChannel)

            assertFalse(catchup.keys.onKey(PlaybackOverlay.None, PlaybackKey.REWIND))
        }

    @Test
    fun `a live tune leaves catch-up mode`() =
        runTest {
            session.set(request)
            val (catchup, _) = build()
            catchup.resumePending()

            catchup.onLiveTune()

            assertNull(catchup.state.value)
        }

    @Test
    fun `pause pins the transport and resume re-arms its auto-hide`() =
        runTest {
            session.set(request)
            val (catchup, _) = build()
            catchup.resumePending()
            val shownAtEntry = transports

            catchup.pause.toggle()
            assertTrue(catchup.pause.paused.value)
            assertEquals(1, pins)
            assertEquals(shownAtEntry, transports)

            catchup.pause.toggle()
            assertFalse(catchup.pause.paused.value)
            assertEquals(1, pins)
            assertEquals(shownAtEntry + 1, transports)
        }

    @Test
    fun `pause is inert during live playback`() =
        runTest {
            val (catchup, tuner) = build()
            tuner.tune(catchupChannel)

            catchup.pause.toggle()

            assertFalse(catchup.pause.paused.value)
            assertEquals(0, pins)
        }

    @Test
    fun `a finished archive returns to live playback of the same channel`() =
        runTest {
            session.set(request)
            val (catchup, tuner) = build()
            catchup.resumePending()

            engine.state.value = PlayerState.Ended

            assertNull(catchup.state.value)
            assertEquals(catchupChannel.source.streamUrl, engine.loaded.last())
            assertEquals(catchupChannel.id, tuner.current.value?.id)
            assertEquals(0, exits)
        }

    @Test
    fun `a finished live stream is ignored (no catch-up mode)`() =
        runTest {
            val (catchup, tuner) = build()
            tuner.tune(catchupChannel)
            val loadsBefore = engine.loaded.size

            engine.state.value = PlayerState.Ended

            assertNull(catchup.state.value)
            assertEquals(loadsBefore, engine.loaded.size)
        }

    @Test
    fun `a paused archive resumes cleanly when a hop retunes`() =
        runTest {
            session.set(request)
            val (catchup, _) = build()
            catchup.resumePending()
            catchup.pause.toggle()

            engine.state.value = PlayerState.Ended

            assertFalse(catchup.pause.paused.value)
        }
}
