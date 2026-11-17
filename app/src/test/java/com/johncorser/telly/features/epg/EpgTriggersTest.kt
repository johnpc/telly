package com.johncorser.telly.features.epg

import com.johncorser.telly.features.playlist.db.PlaylistEntity
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class EpgTriggersTest {
    private fun row(
        epgUrl: String? = "http://e/1.xml",
        epgStamp: Long = 0,
        failedAt: Long = 0,
        lastUpdated: Long = 0,
        url: String = "http://p/1.m3u",
    ) = PlaylistEntity(
        id = 1,
        name = "p",
        url = url,
        epgUrl = epgUrl,
        lastUpdatedMs = lastUpdated,
        epgLastUpdatedMs = epgStamp,
        epgFailedAtMs = failedAt,
    )

    @Test
    fun `the refresher's own stamp writes are not playlist changes`() =
        runTest {
            val emissions =
                flowOf(
                    listOf(row()),
                    // markEpgUpdated
                    listOf(row(epgStamp = 10)),
                    // markEpgFailed
                    listOf(row(epgStamp = 10, failedAt = 20)),
                    // a re-import with the same EPG URL
                    listOf(row(epgStamp = 30, lastUpdated = 25)),
                ).epgRelevantChanges().toList()

            assertEquals("only the start state: no EPG-relevant change happened", 1, emissions.size)
        }

    @Test
    fun `adding a playlist or changing its EPG URL is a change`() =
        runTest {
            val emissions =
                flowOf(
                    listOf(row()),
                    listOf(row(epgUrl = "http://e/2.xml")),
                    listOf(row(epgUrl = "http://e/2.xml"), row(url = "http://p/2.m3u").copy(id = 2)),
                ).epgRelevantChanges().toList()

            assertEquals(3, emissions.size)
            assertEquals("http://p/1.m3u" to "http://e/2.xml", emissions[1].single())
        }
}
