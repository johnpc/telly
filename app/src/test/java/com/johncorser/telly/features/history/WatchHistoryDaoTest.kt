package com.johncorser.telly.features.history

import com.johncorser.telly.core.db.inMemoryDb
import com.johncorser.telly.features.history.db.WatchHistoryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** The real watch-history queries against an in-memory Room DB. */
@RunWith(RobolectricTestRunner::class)
class WatchHistoryDaoTest {
    private val database = inMemoryDb()
    private val dao = database.watchHistoryDao()

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `events come back newest-first and re-watching dedupes to the most recent`() =
        runTest {
            dao.upsert(WatchHistoryEntity("news-one", 1_000L))
            dao.upsert(WatchHistoryEntity("sports-arena", 2_000L))
            dao.upsert(WatchHistoryEntity("news-one", 3_000L))

            assertEquals(
                listOf(WatchHistoryEntity("news-one", 3_000L), WatchHistoryEntity("sports-arena", 2_000L)),
                dao.observeEvents().first(),
            )
        }

    @Test
    fun `trim keeps only the cap most recent watches`() =
        runTest {
            (1..5).forEach { dao.upsert(WatchHistoryEntity("channel-$it", it * 1_000L)) }

            dao.trimTo(3)

            assertEquals(
                listOf("channel-5", "channel-4", "channel-3"),
                dao.observeEvents().first().map { it.channelKey },
            )
        }

    @Test
    fun `clear empties the table`() =
        runTest {
            dao.upsert(WatchHistoryEntity("news-one", 1_000L))

            dao.clear()

            assertEquals(emptyList<WatchHistoryEntity>(), dao.observeEvents().first())
        }
}
