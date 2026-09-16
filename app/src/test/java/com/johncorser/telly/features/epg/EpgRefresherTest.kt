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
        fetch = EpgFetch(refresh),
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
    fun `custom sources are fetched after the auto-detected url-tvg`() =
        runTest {
            coEvery { playlistDao.all() } returns listOf(playlist(1, "http://e/1.xml"))
            val refresher =
                EpgRefresher(
                    playlistDao = playlistDao,
                    scheduler = RefreshScheduler(),
                    clock = { nowMs },
                    fetch =
                        EpgFetch(
                            refresh = {
                                refreshedUrls += it
                                1
                            },
                        ),
                    customSources = { playlistUrl ->
                        assertEquals("http://p/1.m3u", playlistUrl)
                        listOf("http://e/custom-a.xml", "http://e/custom-b.xml")
                    },
                )

            assertEquals(listOf(1L), refresher.refreshDue())

            // Auto first, then custom in added order: the per-channel replace
            // semantics of EpgRepository.refresh make the LAST source win, so
            // custom sources take precedence per channel.
            assertEquals(
                listOf("http://e/1.xml", "http://e/custom-a.xml", "http://e/custom-b.xml"),
                refreshedUrls,
            )
        }

    @Test
    fun `a playlist without url-tvg still refreshes its custom sources`() =
        runTest {
            coEvery { playlistDao.all() } returns listOf(playlist(1, epgUrl = null))
            val refresher =
                EpgRefresher(
                    playlistDao = playlistDao,
                    scheduler = RefreshScheduler(),
                    clock = { nowMs },
                    fetch =
                        EpgFetch(
                            refresh = {
                                refreshedUrls += it
                                1
                            },
                        ),
                    customSources = { listOf("http://e/custom.xml") },
                )

            assertEquals(listOf(1L), refresher.refreshDue())
            assertEquals(listOf("http://e/custom.xml"), refreshedUrls)
            coVerify(exactly = 1) { playlistDao.markEpgUpdated(1, nowMs) }
        }

    @Test
    fun `a failing custom source does not fail the playlist when another succeeds`() =
        runTest {
            coEvery { playlistDao.all() } returns listOf(playlist(1, "http://e/good.xml"))
            val refresher =
                EpgRefresher(
                    playlistDao = playlistDao,
                    scheduler = RefreshScheduler(),
                    clock = { nowMs },
                    fetch = EpgFetch(refresh = { url -> if (url.contains("bad")) throw IOException("boom") else 1 }),
                    customSources = { listOf("http://e/bad.xml") },
                )

            assertEquals(listOf(1L), refresher.refreshDue())
            coVerify(exactly = 1) { playlistDao.markEpgUpdated(1, nowMs) }
        }

    @Test
    fun `a playlist is not stamped when every source fails`() =
        runTest {
            coEvery { playlistDao.all() } returns listOf(playlist(1, "http://e/bad.xml"))
            val refresher =
                EpgRefresher(
                    playlistDao = playlistDao,
                    scheduler = RefreshScheduler(),
                    clock = { nowMs },
                    fetch = EpgFetch(refresh = { throw IOException("boom") }),
                    customSources = { listOf("http://e/bad2.xml") },
                )

            assertEquals(emptyList<Long>(), refresher.refreshDue())
            coVerify(exactly = 0) { playlistDao.markEpgUpdated(any(), any()) }
        }

    @Test
    fun `a transient failure stays due and the next run stamps on success`() =
        runTest {
            coEvery { playlistDao.all() } returns listOf(playlist(1, "http://e/1.xml"))
            var failNext = true
            val refresher =
                refresher {
                    if (failNext) throw IOException("transient") else refreshedUrls += it
                    1
                }

            // First run fails: no stamp, so epgLastUpdatedMs stays 0 and the
            // scheduler keeps the source due for the next minute tick.
            assertEquals(emptyList<Long>(), refresher.refreshDue())
            coVerify(exactly = 0) { playlistDao.markEpgUpdated(any(), any()) }

            failNext = false
            assertEquals(listOf(1L), refresher.refreshDue())
            assertEquals(listOf("http://e/1.xml"), refreshedUrls)
            coVerify(exactly = 1) { playlistDao.markEpgUpdated(1, nowMs) }
        }

    @Test
    fun `a failing source is reported through the warn seam`() =
        runTest {
            coEvery { playlistDao.all() } returns listOf(playlist(1, "http://e/bad.xml"))
            val warnings = mutableListOf<Pair<String, Throwable>>()
            val cause = IOException("boom")
            val refresher =
                EpgRefresher(
                    playlistDao = playlistDao,
                    scheduler = RefreshScheduler(),
                    clock = { nowMs },
                    fetch =
                        EpgFetch(
                            refresh = { throw cause },
                            warn = { message, error -> warnings += message to error },
                        ),
                )

            refresher.refreshDue()

            assertEquals(listOf("EPG refresh failed for http://e/bad.xml" to cause), warnings)
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
    fun `a playlists change with the update toggle ON forces a full refresh`() =
        runTest {
            coEvery { playlistDao.all() } returns
                listOf(playlist(1, "http://e/fresh.xml", epgLastUpdatedMs = nowMs - 1))

            assertEquals(listOf(1L), refresher().onPlaylistsChanged(updateOnChange = true))
            assertEquals(listOf("http://e/fresh.xml"), refreshedUrls)
        }

    @Test
    fun `a playlists change with the toggle OFF keeps the due-only policy`() =
        runTest {
            coEvery { playlistDao.all() } returns
                listOf(
                    playlist(1, "http://e/fresh.xml", epgLastUpdatedMs = nowMs - 1),
                    playlist(2, "http://e/never.xml"),
                )

            assertEquals(listOf(2L), refresher().onPlaylistsChanged(updateOnChange = false))
            assertEquals(listOf("http://e/never.xml"), refreshedUrls)
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
                    fetch = EpgFetch(refresh = { 1 }),
                    retention =
                        EpgRetention(
                            keepPastMs = { EpgRefresher.daysToMs(2) },
                            trim = { cutoffs += it },
                        ),
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
