package com.johncorser.telly.features.history

import com.johncorser.telly.testutil.FakeWatchHistoryDao
import com.johncorser.telly.testutil.testChannel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class WatchHistoryTest {
    private val dao = FakeWatchHistoryDao()
    private var now = 0L
    private val history = WatchHistory(dao) { now }

    @Test
    fun `record stamps the injected clock onto the refresh-stable identity`() =
        runTest {
            now = 5_000L
            history.record(testChannel(1, 1, "News One"))
            now = 6_000L
            history.record(testChannel(4, 4, "Silent FM", tvgId = null))

            assertEquals(
                mapOf("tvg-1" to 5_000L, "http://s/4.ts|Silent FM" to 6_000L),
                dao.events.value,
            )
        }

    @Test
    fun `recording past the cap trims the oldest watches`() =
        runTest {
            repeat(WatchHistory.CAP + 2) { index ->
                now = index.toLong()
                history.record(testChannel(index.toLong(), index + 1, "Channel $index"))
            }

            val keys = history.keys.first()
            assertEquals(WatchHistory.CAP, keys.size)
            assertEquals("tvg-${WatchHistory.CAP + 1}", keys.first())
        }

    @Test
    fun `the synthetic group orders channels by recency and drops unknown keys`() {
        val channels = listOf(testChannel(1, 1, "News One"), testChannel(2, 2, "Sports Arena"))

        val ordered = HistoryGroup.channelsIn(listOf("tvg-2", "tvg-gone", "tvg-1"), channels)

        assertEquals(listOf(2L, 1L), ordered.map { it.id })
    }

    @Test
    fun `the groups column gains History only while it is selected`() {
        val names = listOf("Favorites", "All channels", "News")

        assertEquals(listOf("History") + names, HistoryGroup.columnFor(HistoryGroup.NAME, names))
        assertEquals(names, HistoryGroup.columnFor("All channels", names))
    }
}
