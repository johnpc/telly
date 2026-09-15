package com.johncorser.telly.features.playback

import com.johncorser.telly.features.history.WatchHistory
import com.johncorser.telly.features.mylist.InMemoryMyListStore
import com.johncorser.telly.features.mylist.MyListHooks
import com.johncorser.telly.features.pip.PipState
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
import java.util.TimeZone

/**
 * Shared fixture for the PlaybackViewModel test classes (split so each
 * stays under the detekt LargeClass gate): three channels over fake
 * engine/store/EPG, spy counters for the cross-slice hooks, and the
 * ViewModel builder that bakes the My-list + PIP spies into the hooks.
 */
@OptIn(ExperimentalCoroutinesApi::class)
open class PlaybackVmHarness {
    protected val channels =
        listOf(
            testChannel(1, 1, "News One", group = "News"),
            testChannel(2, 2, "News Two", group = "News"),
            testChannel(3, 3, "Sports Arena", group = "Sports"),
        )
    protected val dao = FakeChannelDao(channels)
    protected val engine = FakePlayerEngine()
    protected val store = FakeKeyValueStore()
    protected val programs = FakeProgramDao()
    protected val historyDao = FakeWatchHistoryDao()

    protected var exitedToGuide = 0
    protected var openedHistory = 0
    protected var now = 1_000_000L
    protected val myListStore = InMemoryMyListStore()
    protected var manageOpens = 0
    protected val reorderGroups = mutableListOf<String>()

    protected fun TestScope.buildVm(
        clock: () -> Long = { now },
        onEnterPip: () -> Unit = {},
        pip: PipState = PipState(),
        hooks: PlaybackHooks = PlaybackHooks(),
    ): PlaybackViewModel =
        PlaybackViewModel(
            env =
                PlaybackEnv(
                    channelDao = dao,
                    epgRepository = testEpgRepository(programs),
                    engine = engine,
                    store = store,
                    time = PlaybackTime(clock, TimeZone.getTimeZone("UTC")),
                    hooks =
                        hooks.copy(
                            onEnterPip = onEnterPip,
                            pip = pip,
                            myList =
                                MyListHooks(
                                    onOpenManageFavorites = { manageOpens += 1 },
                                    onOpenReorderChannels = { reorderGroups += it },
                                    store = myListStore,
                                ),
                        ),
                ),
            history = WatchHistory(historyDao, clock),
            scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler)),
            onExitToGuide = { exitedToGuide += 1 },
            onOpenHistory = { openedHistory += 1 },
        )
}
