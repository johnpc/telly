package com.johncorser.telly.features.guide

import com.johncorser.telly.features.catchup.CatchupDeps
import com.johncorser.telly.features.catchup.CatchupSession
import com.johncorser.telly.features.guide.GuideTestData.at
import com.johncorser.telly.features.guide.GuideTestData.nowMs
import com.johncorser.telly.features.guide.GuideTestData.utc
import com.johncorser.telly.features.history.WatchHistory
import com.johncorser.telly.features.playback.ClockStyle
import com.johncorser.telly.features.playback.PlaybackEnv
import com.johncorser.telly.features.playback.PlaybackHooks
import com.johncorser.telly.features.playback.PlaybackTime
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** OK on a playable past cell (after a day jump) plays catch-up (ux-spec §3.17). */
@OptIn(ExperimentalCoroutinesApi::class)
class GuideCatchupWiringTest {
    private val channels =
        listOf(
            testChannel(1, 1, "News One").withCatchup(source = "http://s/arc?utc={utc}&d={duration}", days = 7),
            testChannel(2, 2, "Plain Two"),
        )
    private val dao = FakeChannelDao(channels)
    private val engine = FakePlayerEngine()
    private val store = FakeKeyValueStore()
    private val programs =
        FakeProgramDao(
            listOf(
                testProgram("tvg-1", at(14, 0, dayOffset = -1), at(15, 0, dayOffset = -1), "Yesterday Report"),
                testProgram("tvg-2", at(14, 0, dayOffset = -1), at(15, 0, dayOffset = -1), "Plain Yesterday"),
            ),
        )
    private val session = CatchupSession()
    private var fullscreens = 0

    private fun TestScope.buildController(): GuideController =
        GuideController(
            env =
                PlaybackEnv(
                    channelDao = dao,
                    epgRepository = testEpgRepository(programs),
                    engine = engine,
                    store = store,
                    time = PlaybackTime({ nowMs }, ClockStyle(utc), MutableSharedFlow()),
                    hooks = PlaybackHooks(catchup = CatchupDeps(session = session)),
                ),
            history = WatchHistory(FakeWatchHistoryDao()) { nowMs },
            pastDays = { 7 },
            scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler)),
            callbacks = GuideCallbacks(onFullscreen = { fullscreens += 1 }, onOpenSearch = {}, onOpenSettings = {}),
        )

    @Test
    fun `ok on yesterday's programme of a catch-up channel goes fullscreen with a request`() =
        runTest {
            val controller = buildController()
            controller.onKey(GuideKey.LONG_LEFT)
            assertEquals("Yesterday Report", controller.focus.value?.cell?.program?.details?.title)

            assertTrue(controller.onKey(GuideKey.OK))

            assertEquals(1, fullscreens)
            val request = session.consume()
            assertNotNull(request)
            assertEquals(at(14, 0, dayOffset = -1), request?.startMs)
            assertTrue(request?.url?.startsWith("http://s/arc?utc=") == true)
            // The guide preview did NOT tune the archive; playback consumes it.
            assertTrue(engine.loaded.isEmpty() || engine.loaded.last() != request?.url)
        }

    @Test
    fun `ok on the same past cell of a plain channel plays it live`() =
        runTest {
            val controller = buildController()
            controller.onKey(GuideKey.LONG_LEFT)
            controller.onKey(GuideKey.DOWN)
            assertEquals("Plain Yesterday", controller.focus.value?.cell?.program?.details?.title)

            assertTrue(controller.onKey(GuideKey.OK))

            // No archive for a plain channel: OK tunes it live + fullscreens.
            assertEquals(2L, controller.preview.value?.id)
            assertEquals(1, fullscreens)
            assertNull(session.consume())
        }

    @Test
    fun `a past cell beyond the catchup-days horizon plays live`() =
        runTest {
            dao.channels.value =
                listOf(testChannel(1, 1, "News One").withCatchup(source = "http://s/arc?utc={utc}", days = 0))
            val controller = buildController()
            controller.onKey(GuideKey.LONG_LEFT)

            assertTrue(controller.onKey(GuideKey.OK))

            // Past the horizon there is no archive: OK falls back to live tune.
            assertEquals(1L, controller.preview.value?.id)
            assertEquals(1, fullscreens)
            assertNull(session.consume())
        }
}
