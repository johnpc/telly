package com.johncorser.telly.features.epg.db

import com.johncorser.telly.core.db.inMemoryDb
import com.johncorser.telly.features.epg.RoomEpgSourceStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EpgSourceDaoTest {
    private val database = inMemoryDb()
    private var nowMs = 0L
    private val store = RoomEpgSourceStore(database.epgSourceDao()) { ++nowMs }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `sources persist per playlist in added order`() =
        runTest {
            store.add("http://p/a.m3u", "http://e/2.xml")
            store.add("http://p/a.m3u", "http://e/1.xml")
            store.add("http://p/b.m3u", "http://e/3.xml")

            assertEquals(
                listOf("http://e/2.xml", "http://e/1.xml"),
                store.forPlaylist("http://p/a.m3u").map { it.url },
            )
            assertEquals(
                listOf("http://e/2.xml", "http://e/1.xml", "http://e/3.xml"),
                store.sources.first().map { it.url },
            )
        }

    @Test
    fun `re-adding the same url for a playlist does not duplicate it`() =
        runTest {
            store.add("http://p/a.m3u", "http://e/1.xml")
            store.add("http://p/a.m3u", "http://e/1.xml")
            assertEquals(1, store.sources.first().size)
        }

    @Test
    fun `setUrl rewrites one source and remove deletes it`() =
        runTest {
            store.add("http://p/a.m3u", "http://e/1.xml")
            store.add("http://p/a.m3u", "http://e/2.xml")
            val first = store.sources.first().first()

            store.setUrl(first.id, "http://e/edited.xml")
            assertEquals(
                listOf("http://e/edited.xml", "http://e/2.xml"),
                store.sources.first().map { it.url },
            )

            store.remove(first.id)
            assertEquals(listOf("http://e/2.xml"), store.sources.first().map { it.url })
        }

    @Test
    fun `source names derive from the url host`() =
        runTest {
            store.add("http://p/a.m3u", "http://guide.example:8080/epg.xml")
            assertEquals("guide.example", store.sources.first().single().name)
        }
}
