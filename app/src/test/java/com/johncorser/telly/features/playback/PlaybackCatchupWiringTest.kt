package com.johncorser.telly.features.playback

import com.johncorser.telly.features.catchup.CatchupDeps
import com.johncorser.telly.features.catchup.CatchupRequest
import com.johncorser.telly.features.catchup.CatchupSession
import com.johncorser.telly.features.history.WatchHistory
import com.johncorser.telly.testutil.FakeChannelDao
import com.johncorser.telly.testutil.FakeKeyValueStore
import com.johncorser.telly.testutil.FakePlayerEngine
import com.johncorser.telly.testutil.FakeProgramDao
import com.johncorser.telly.testutil.FakeWatchHistoryDao
import com.johncorser.telly.testutil.testChannel
import com.johncorser.telly.testutil.testEpgRepository
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

/** The playback screen consuming a guide catch-up request (ux-spec §2.10). */
@OptIn(ExperimentalCoroutinesApi::class)
class PlaybackCatchupWiringTest {
    private val hourMs = 3_600_000L
    private val now = 100 * hourMs
    private val channel =
        testChannel(1, 1, "News One").withCatchup(source = "http://s/arc?utc={utc}&d={duration}")
    private val dao = FakeChannelDao(listOf(channel, testChannel(2, 2, "News Two")))
    private val engine = FakePlayerEngine()
    private val store = FakeKeyValueStore()
    private val session = CatchupSession()
    private var exitedToGuide = 0

    private val request =
        CatchupRequest(
            channel = channel,
            url = "http://s/arc?utc=42&d=3600",
            title = "Morning Report",
            startMs = now - 5 * hourMs,
            endMs = now - 4 * hourMs,
        )

    private fun TestScope.buildVm(): PlaybackViewModel =
        PlaybackViewModel(
            env =
                PlaybackEnv(
                    channelDao = dao,
                    epgRepository = testEpgRepository(FakeProgramDao()),
                    engine = engine,
                    store = store,
                    time = PlaybackTime({ now }, TimeZone.getTimeZone("UTC")),
                    hooks = PlaybackHooks(catchup = CatchupDeps(session = session)),
                ),
            history = WatchHistory(FakeWatchHistoryDao()) { now },
            scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler)),
            exitToGuide = { exitedToGuide += 1 },
        )

    @Test
    fun `a pending request plays the archive with the seek transport shown`() =
        runTest {
            session.set(request)

            val vm = buildVm()

            assertEquals(listOf(request.url), engine.loaded)
            assertEquals(PlaybackOverlay.InfoTransport, vm.overlay.value)
            assertEquals(channel.id, vm.current.value?.id)
        }

    @Test
    fun `the info overlay shows the archived programme with a position readout`() =
        runTest {
            session.set(request)

            val vm = buildVm()

            assertEquals("Morning Report", vm.info.value?.title)
            assertEquals("00:00", vm.info.value?.elapsed)
            assertEquals("1:00:00", vm.info.value?.duration)
            assertNull(vm.info.value?.remaining)
        }

    @Test
    fun `back returns to the guide from catch-up playback`() =
        runTest {
            session.set(request)
            val vm = buildVm()
            vm.onKey(PlaybackKey.BACK) // dismiss the transport overlay

            assertTrue(vm.onKey(PlaybackKey.BACK))

            assertEquals(1, exitedToGuide)
            assertNull(vm.catchup.state.value)
        }

    @Test
    fun `zapping away returns to live and leaves catch-up mode`() =
        runTest {
            session.set(request)
            val vm = buildVm()
            vm.onKey(PlaybackKey.BACK)

            vm.onKey(PlaybackKey.CHANNEL_UP)

            assertNull(vm.catchup.state.value)
            assertEquals("http://s/2.ts", engine.loaded.last())
        }

    @Test
    fun `rw seeks during catch-up with the default toggle`() =
        runTest {
            session.set(request)
            val vm = buildVm()
            engine.position = 60_000L

            assertTrue(vm.onKey(PlaybackKey.REWIND))

            assertEquals(listOf(50_000L), engine.seeks)
            assertEquals(PlaybackOverlay.InfoTransport, vm.overlay.value)
        }

    @Test
    fun `without a pending request the cold start tunes live as before`() =
        runTest {
            val vm = buildVm()

            assertEquals(listOf(channel.source.streamUrl), engine.loaded)
            assertNull(vm.catchup.state.value)
            assertEquals(PlaybackOverlay.None, vm.overlay.value)
        }
}
