package com.johncorser.telly.features.history

import com.johncorser.telly.features.history.db.WatchHistoryEntity
import com.johncorser.telly.features.playback.PlaybackSources
import com.johncorser.telly.features.playback.TuneController
import com.johncorser.telly.testutil.FakeChannelDao
import com.johncorser.telly.testutil.FakeKeyValueStore
import com.johncorser.telly.testutil.FakeProgramDao
import com.johncorser.telly.testutil.FakeWatchHistoryDao
import com.johncorser.telly.testutil.testChannel
import com.johncorser.telly.testutil.testEpgRepository
import com.johncorser.telly.testutil.testProgram
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.TimeZone

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {
    private val channels =
        listOf(
            testChannel(1, 1, "News One"),
            testChannel(2, 2, "Sports Arena"),
        )
    private val channelDao = FakeChannelDao(channels)
    private val programs =
        FakeProgramDao(
            listOf(
                testProgram("tvg-1", 1_000L, 2_000L, "Business Hour", episode = "S1 E7"),
            ),
        )
    private val historyDao = FakeWatchHistoryDao()
    private val store = FakeKeyValueStore()

    private fun TestScope.buildVm(): HistoryViewModel =
        HistoryViewModel(
            sources =
                PlaybackSources(
                    channelDao = channelDao,
                    epgRepository = testEpgRepository(programs),
                    history = WatchHistory(historyDao) { 0L },
                ),
            store = store,
            zone = TimeZone.getTimeZone("UTC"),
            scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler)),
        )

    @Test
    fun `rows list the full history newest-first with the watched programme`() =
        runTest {
            historyDao.upsert(WatchHistoryEntity("tvg-1", 1_500L))
            historyDao.upsert(WatchHistoryEntity("tvg-2", 5_000L))

            val vm = buildVm()

            assertEquals(listOf(2L, 1L), vm.rows.value.map { it.channel.id })
            assertEquals(listOf(null, "Business Hour. S1 E7"), vm.rows.value.map { it.programmeTitle })
        }

    @Test
    fun `clear-all acts immediately and empties the rows`() =
        runTest {
            historyDao.upsert(WatchHistoryEntity("tvg-1", 1_500L))
            val vm = buildVm()
            assertEquals(1, vm.rows.value.size)

            vm.clearAll()

            assertEquals(emptyList<HistoryRow>(), vm.rows.value)
        }

    @Test
    fun `tuning a row persists the channel for the playback screen to restore`() =
        runTest {
            val vm = buildVm()

            vm.tune(HistoryRow(channels[1], watchedText = "now", programmeTitle = null))

            assertEquals(2L, store.getLong(TuneController.LAST_CHANNEL_KEY))
        }
}
