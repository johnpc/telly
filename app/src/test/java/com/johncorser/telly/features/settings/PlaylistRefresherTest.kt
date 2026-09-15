package com.johncorser.telly.features.settings

import com.johncorser.telly.features.playlist.db.PlaylistDao
import com.johncorser.telly.features.playlist.db.PlaylistEntity
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaylistRefresherTest {
    private val hourMs = 60L * 60L * 1000L
    private var nowMs = 100L * hourMs
    private val updated = mutableListOf<String>()
    private val intervals = mutableMapOf<String, Int>()
    private val onStart = mutableSetOf<String>()

    private fun playlist(
        url: String,
        ageHours: Long,
    ): PlaylistEntity = PlaylistEntity(id = 1, name = url, url = url, lastUpdatedMs = nowMs - ageHours * hourMs)

    private fun refresher(
        vararg playlists: PlaylistEntity,
        updateResult: Boolean = true,
    ): PlaylistRefresher {
        val dao = mockk<PlaylistDao>()
        coEvery { dao.all() } returns playlists.toList()
        return PlaylistRefresher(
            playlistDao = dao,
            update = { url ->
                updated += url
                updateResult
            },
            intervalHours = { intervals[it] ?: 0 },
            updateOnStart = { it in onStart },
            clock = { nowMs },
        )
    }

    @Test
    fun `the None interval never makes a playlist due`() =
        runTest {
            assertTrue(refresher(playlist("http://p/a", ageHours = 1000)).refreshDue().isEmpty())
            assertTrue(updated.isEmpty())
        }

    @Test
    fun `a playlist older than its interval is due, a fresher one is not`() =
        runTest {
            intervals["http://p/stale"] = 8
            intervals["http://p/fresh"] = 8

            val due = refresher(playlist("http://p/stale", 9), playlist("http://p/fresh", 7)).refreshDue()

            assertEquals(listOf("http://p/stale"), due)
            assertEquals(listOf("http://p/stale"), updated)
        }

    @Test
    fun `update on app start forces the fetch at launch regardless of age`() =
        runTest {
            onStart += "http://p/forced"

            val refreshed = refresher(playlist("http://p/forced", 0), playlist("http://p/other", 0)).refreshOnStart()

            assertEquals(listOf("http://p/forced"), refreshed)
        }

    @Test
    fun `refreshOnStart also covers interval-due playlists`() =
        runTest {
            intervals["http://p/stale"] = 1

            assertEquals(listOf("http://p/stale"), refresher(playlist("http://p/stale", 2)).refreshOnStart())
        }

    @Test
    fun `failed updates drop out of the refreshed list`() =
        runTest {
            onStart += "http://p/broken"

            val refreshed = refresher(playlist("http://p/broken", 0), updateResult = false).refreshOnStart()

            assertTrue(refreshed.isEmpty())
            assertEquals(listOf("http://p/broken"), updated)
        }

    @Test
    fun `run refreshes on start then once per tick`() =
        runTest {
            onStart += "http://p/forced"
            intervals["http://p/forced"] = 0

            refresher(playlist("http://p/forced", 0)).run(flowOf(Unit, Unit))

            // Start forces the fetch; the ticks find nothing interval-due.
            assertEquals(listOf("http://p/forced"), updated)
        }

    @Test
    fun `ticks re-check the interval with the injected clock`() =
        runTest {
            intervals["http://p/hourly"] = 1
            val target = playlist("http://p/hourly", 0)
            val refresher = refresher(target)

            nowMs += hourMs
            refresher.run(flowOf(Unit))

            // Due at start (one interval old by then) and due again on the
            // tick: the mocked DAO keeps returning the stale stamp.
            assertEquals(listOf("http://p/hourly", "http://p/hourly"), updated)
        }
}
