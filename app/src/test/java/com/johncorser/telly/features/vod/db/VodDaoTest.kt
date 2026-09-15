package com.johncorser.telly.features.vod.db

import com.johncorser.telly.core.db.inMemoryDb
import com.johncorser.telly.features.playlist.db.PlaylistEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** The real VOD queries against an in-memory Room DB. */
@RunWith(RobolectricTestRunner::class)
class VodDaoTest {
    private val database = inMemoryDb()
    private val itemDao = database.vodItemDao()
    private val positionDao = database.vodPositionDao()

    @After
    fun tearDown() {
        database.close()
    }

    private suspend fun playlistId(url: String = "http://p/a.m3u"): Long =
        database.playlistDao().upsert(PlaylistEntity(name = "a", url = url))

    private fun item(
        playlistId: Long,
        name: String,
        sortIndex: Int,
    ) = VodItemEntity(
        playlistId = playlistId,
        sortIndex = sortIndex,
        itemKey = "http://s/$name.mp4|$name",
        name = name,
        groupTitle = "Cinema",
        streamUrl = "http://s/$name.mp4",
    )

    @Test
    fun `items come back in playlist order and resolve by key`() =
        runTest {
            val id = playlistId()
            itemDao.insertAll(listOf(item(id, "B", 1), item(id, "A", 0)))

            assertEquals(listOf("A", "B"), itemDao.observeAll().first().map { it.name })
            assertEquals(2, itemDao.totalCount())
            assertEquals("B", itemDao.byKey("http://s/B.mp4|B")?.name)
            assertNull(itemDao.byKey("missing"))
        }

    @Test
    fun `deleteForPlaylist only clears that playlist's items`() =
        runTest {
            val a = playlistId("http://p/a.m3u")
            val b = playlistId("http://p/b.m3u")
            itemDao.insertAll(listOf(item(a, "A", 0), item(b, "B", 0)))

            itemDao.deleteForPlaylist(a)

            assertEquals(listOf("B"), itemDao.observeAll().first().map { it.name })
        }

    @Test
    fun `deleting the playlist cascades to its items`() =
        runTest {
            val id = playlistId()
            itemDao.insertAll(listOf(item(id, "A", 0)))

            database.playlistDao().delete(id)

            assertEquals(0, itemDao.totalCount())
        }

    @Test
    fun `positions upsert by key, delete and clear`() =
        runTest {
            positionDao.upsert(VodPositionEntity("k1", 5_000, 60_000, 1))
            positionDao.upsert(VodPositionEntity("k2", 9_000, 60_000, 2))
            positionDao.upsert(VodPositionEntity("k1", 7_000, 60_000, 3))

            assertEquals(7_000L, positionDao.byKey("k1")?.positionMs)
            assertEquals(2, positionDao.observeAll().first().size)

            positionDao.delete("k1")
            assertNull(positionDao.byKey("k1"))

            positionDao.clearAll()
            assertTrue(positionDao.observeAll().first().isEmpty())
        }
}
