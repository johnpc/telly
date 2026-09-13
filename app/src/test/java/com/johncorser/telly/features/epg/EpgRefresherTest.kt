package com.johncorser.telly.features.epg

import com.johncorser.telly.features.playlist.db.PlaylistDao
import com.johncorser.telly.features.playlist.db.PlaylistEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class EpgRefresherTest {
    private val dayMs = RefreshScheduler.DEFAULT_INTERVAL_MS
    private val nowMs = 10 * dayMs
    private val playlistDao = mockk<PlaylistDao>(relaxed = true)
    private val refreshedUrls = mutableListOf<String>()

    private fun refresher(
        refresh: suspend (String) -> Int = {
            refreshedUrls += it
            1
        },
    ) = EpgRefresher(
        playlistDao = playlistDao,
        scheduler = RefreshScheduler(),
        clock = { nowMs },
        refresh = refresh,
    )

    private fun playlist(
        id: Long,
        epgUrl: String?,
        epgLastUpdatedMs: Long = 0,
    ) = PlaylistEntity(
        id = id,
        name = "p$id",
        url = "http://p/$id.m3u",
        epgUrl = epgUrl,
        epgLastUpdatedMs = epgLastUpdatedMs,
    )

    @Test
    fun `refreshes only playlists with a due epg url and stamps them`() =
        runTest {
            coEvery { playlistDao.all() } returns
                listOf(
                    playlist(1, "http://e/1.xml"),
                    playlist(2, epgUrl = null),
                    playlist(3, "http://e/3.xml", epgLastUpdatedMs = nowMs - 1),
                    playlist(4, "http://e/4.xml", epgLastUpdatedMs = nowMs - dayMs),
                )

            val refreshed = refresher().refreshDue()

            assertEquals(listOf(1L, 4L), refreshed)
            assertEquals(listOf("http://e/1.xml", "http://e/4.xml"), refreshedUrls)
            coVerify(exactly = 1) { playlistDao.markEpgUpdated(1, nowMs) }
            coVerify(exactly = 1) { playlistDao.markEpgUpdated(4, nowMs) }
            coVerify(exactly = 0) { playlistDao.markEpgUpdated(3, any()) }
        }

    @Test
    fun `one failing epg source does not starve the others`() =
        runTest {
            coEvery { playlistDao.all() } returns
                listOf(playlist(1, "http://e/bad.xml"), playlist(2, "http://e/good.xml"))
            val refresher =
                refresher { url ->
                    if (url.contains("bad")) throw IOException("boom") else 1
                }

            assertEquals(listOf(2L), refresher.refreshDue())
            coVerify(exactly = 0) { playlistDao.markEpgUpdated(1, any()) }
            coVerify(exactly = 1) { playlistDao.markEpgUpdated(2, nowMs) }
        }

    @Test
    fun `no playlists means nothing to refresh`() =
        runTest {
            coEvery { playlistDao.all() } returns emptyList()
            assertEquals(emptyList<Long>(), refresher().refreshDue())
        }

    @Test
    fun `refreshAllNow ignores freshness for the Update EPG action`() =
        runTest {
            coEvery { playlistDao.all() } returns
                listOf(playlist(1, "http://e/1.xml", epgLastUpdatedMs = nowMs - 1), playlist(2, epgUrl = null))

            assertEquals(listOf(1L), refresher().refreshAllNow())
            assertEquals(listOf("http://e/1.xml"), refreshedUrls)
        }

    @Test
    fun `every run trims programmes past the keep horizon`() =
        runTest {
            coEvery { playlistDao.all() } returns emptyList()
            val cutoffs = mutableListOf<Long>()
            val refresher =
                EpgRefresher(
                    playlistDao = playlistDao,
                    scheduler = RefreshScheduler(),
                    clock = { nowMs },
                    refresh = { 1 },
                    keepPastMs = { EpgRefresher.daysToMs(2) },
                    trim = { cutoffs += it },
                )

            refresher.refreshDue()

            assertEquals(listOf(nowMs - 2 * dayMs), cutoffs)
        }

    @Test
    fun `daysToMs clamps negatives to zero`() {
        assertEquals(0L, EpgRefresher.daysToMs(-3))
        assertEquals(7 * dayMs, EpgRefresher.DEFAULT_KEEP_PAST_MS)
    }
}
