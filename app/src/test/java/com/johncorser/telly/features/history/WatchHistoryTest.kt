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

            val events = history.events.first()
            assertEquals(WatchHistory.CAP, events.size)
            assertEquals("tvg-${WatchHistory.CAP + 1}", events.first().channelKey)
        }

    @Test
    fun `events come back newest-first`() =
        runTest {
            now = 1_000L
            history.record(testChannel(1, 1, "News One"))
            now = 2_000L
            history.record(testChannel(2, 2, "Sports Arena"))

            assertEquals(listOf("tvg-2", "tvg-1"), history.events.first().map { it.channelKey })
        }

    @Test
    fun `clear empties the one table behind both history surfaces`() =
        runTest {
            now = 1_000L
            history.record(testChannel(1, 1, "News One"))

            history.clear()

            assertEquals(emptyList<Any>(), history.events.first())
        }
}
