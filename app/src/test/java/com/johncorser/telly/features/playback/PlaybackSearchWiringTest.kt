package com.johncorser.telly.features.playback

import com.johncorser.telly.features.history.WatchHistory
import com.johncorser.telly.testutil.FakeChannelDao
import com.johncorser.telly.testutil.FakeKeyValueStore
import com.johncorser.telly.testutil.FakePlayerEngine
import com.johncorser.telly.testutil.FakeProgramDao
import com.johncorser.telly.testutil.FakeWatchHistoryDao
import com.johncorser.telly.testutil.testChannel
import com.johncorser.telly.testutil.testEpgRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The quick-bar Search slot navigates to the search route (catalogue §4). */
@OptIn(ExperimentalCoroutinesApi::class)
class PlaybackSearchWiringTest {
    private fun TestScope.buildVm(openSearch: () -> Unit): PlaybackViewModel =
        PlaybackViewModel(
            env =
                PlaybackEnv(
                    channelDao = FakeChannelDao(listOf(testChannel(1, 1, "News One"))),
                    epgRepository = testEpgRepository(FakeProgramDao()),
                    engine = FakePlayerEngine(),
                    store = FakeKeyValueStore(),
                    time = PlaybackTime({ 1_000_000L }),
                ),
            history = WatchHistory(FakeWatchHistoryDao()) { 1_000_000L },
            scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler)),
            openSearch = openSearch,
        )

    @Test
    fun `the quick-bar Search slot opens search instead of coming-soon`() =
        runTest {
            var opened = false
            val vm = buildVm { opened = true }

            vm.onQuickBarItem(QuickBarAction.SEARCH)

            assertTrue(opened)
            assertEquals(PlaybackOverlay.None, vm.overlay.value)
        }

    @Test
    fun `other unbuilt quick-bar slots still show the placeholder`() =
        runTest {
            val vm = buildVm { }

            vm.onQuickBarItem(QuickBarAction.RECORDINGS)

            assertEquals(PlaybackOverlay.ComingSoon("Recordings"), vm.overlay.value)
        }
}
