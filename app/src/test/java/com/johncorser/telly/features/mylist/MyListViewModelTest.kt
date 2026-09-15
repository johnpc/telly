package com.johncorser.telly.features.mylist

import com.johncorser.telly.features.mylist.db.MyListEntity
import com.johncorser.telly.features.playback.TuneController
import com.johncorser.telly.testutil.FakeChannelDao
import com.johncorser.telly.testutil.FakeKeyValueStore
import com.johncorser.telly.testutil.testChannel
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
class MyListViewModelTest {
    private val channels = listOf(testChannel(1, 1, "News One"), testChannel(2, 2, "Sports Arena"))
    private val channelDao = FakeChannelDao(channels)
    private val store = InMemoryMyListStore()
    private val lastChannel = FakeKeyValueStore()
    private var now = 2_000L

    private fun TestScope.buildVm(): MyListViewModel =
        MyListViewModel(
            store = store,
            channelDao = channelDao,
            lastChannel = lastChannel,
            clock = { now },
            zone = TimeZone.getTimeZone("UTC"),
            scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler)),
        )

    private suspend fun save(
        key: String,
        startMs: Long,
        endMs: Long,
        addedAtMs: Long,
    ) = store.save(
        MyListEntity(
            channelKey = key,
            startMs = startMs,
            endMs = endMs,
            title = "P$startMs",
            description = null,
            addedAtMs = addedAtMs,
        ),
    )

    @Test
    fun `rows list saved programmes newest-first, ended ones hidden`() =
        runTest {
            save("tvg-1", 500L, 1_500L, addedAtMs = 1L)
            save("tvg-1", 1_000L, 9_000L, addedAtMs = 2L)
            save("tvg-2", 5_000L, 9_000L, addedAtMs = 3L)

            val vm = buildVm()

            assertEquals(listOf(2L, 1L), vm.rows.value.map { it.channel.id })
        }

    @Test
    fun `tuning a row persists the channel for the playback screen to restore`() =
        runTest {
            save("tvg-2", 1_000L, 9_000L, addedAtMs = 1L)
            val vm = buildVm()

            vm.tune(vm.rows.value.single())

            assertEquals(2L, lastChannel.getLong(TuneController.LAST_CHANNEL_KEY))
        }

    @Test
    fun `remove deletes the entry and the rows follow`() =
        runTest {
            save("tvg-1", 1_000L, 9_000L, addedAtMs = 1L)
            val vm = buildVm()

            vm.remove(vm.rows.value.single())

            assertEquals(emptyList<MyListRow>(), vm.rows.value)
        }
}
