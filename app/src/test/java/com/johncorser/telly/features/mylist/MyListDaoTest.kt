package com.johncorser.telly.features.mylist

import com.johncorser.telly.core.db.inMemoryDb
import com.johncorser.telly.features.mylist.db.MyListEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** The real my_list queries against an in-memory Room DB. */
@RunWith(RobolectricTestRunner::class)
class MyListDaoTest {
    private val database = inMemoryDb()
    private val dao = database.myListDao()

    @After
    fun tearDown() {
        database.close()
    }

    private fun entry(
        key: String,
        startMs: Long,
        addedAtMs: Long,
        title: String = "Programme",
    ) = MyListEntity(
        channelKey = key,
        startMs = startMs,
        endMs = startMs + 1_000L,
        title = title,
        description = "About $title",
        addedAtMs = addedAtMs,
    )

    @Test
    fun `entries come back most recently added first`() =
        runTest {
            dao.upsert(entry("news-one", 1_000L, addedAtMs = 10L))
            dao.upsert(entry("sports-arena", 2_000L, addedAtMs = 30L))
            dao.upsert(entry("movie-house", 3_000L, addedAtMs = 20L))

            assertEquals(
                listOf("sports-arena", "movie-house", "news-one"),
                dao.observeAll().first().map { it.channelKey },
            )
        }

    @Test
    fun `re-saving the same airing replaces the row instead of duplicating`() =
        runTest {
            dao.upsert(entry("news-one", 1_000L, addedAtMs = 10L, title = "Old"))
            dao.upsert(entry("news-one", 1_000L, addedAtMs = 20L, title = "New"))

            assertEquals(listOf("New"), dao.observeAll().first().map { it.title })
        }

    @Test
    fun `delete removes exactly the keyed airing`() =
        runTest {
            dao.upsert(entry("news-one", 1_000L, addedAtMs = 10L))
            dao.upsert(entry("news-one", 2_000L, addedAtMs = 20L))

            dao.delete("news-one", 1_000L)

            assertEquals(listOf(2_000L), dao.observeAll().first().map { it.startMs })
        }
}
