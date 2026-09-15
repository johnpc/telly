package com.johncorser.telly.features.playlist

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException

class PlaylistUrlChangerTest {
    private val old = "http://p/old.m3u"
    private val new = "http://p/new.m3u"
    private val repository = InMemoryPlaylistRepository()
    private val settingsRekeys = mutableListOf<Pair<String, String>>()
    private val epgRekeys = mutableListOf<Pair<String, String>>()

    private val body =
        """
        #EXTM3U url-tvg="http://e/epg.xml"
        #EXTINF:-1 tvg-id="one" group-title="News",News One
        http://s/1.ts
        """.trimIndent()

    private fun changer(fetch: suspend (String) -> String = { body }): PlaylistUrlChanger =
        PlaylistUrlChanger(
            fetchPlaylist = fetch,
            repository = repository,
            rekeySettings = { o, n -> settingsRekeys += o to n },
            rekeyEpgSources = { o, n -> epgRekeys += o to n },
        )

    private suspend fun seed() {
        repository.add(old, M3uPlaylist(), name = "Living room")
    }

    @Test
    fun `fetches the new url then re-keys the row, settings and epg sources`() =
        runTest {
            seed()
            val fetched = mutableListOf<String>()

            assertTrue(
                changer(fetch = { url ->
                    fetched += url
                    body
                }).change(old, new),
            )

            assertEquals(listOf(new), fetched)
            val stored = repository.playlists.value.single()
            assertEquals(new, stored.sourceUrl)
            assertEquals("Living room", stored.name)
            assertEquals(listOf("News One"), stored.playlist.channels.map { it.title })
            assertEquals(listOf(old to new), settingsRekeys)
            assertEquals(listOf(old to new), epgRekeys)
        }

    @Test
    fun `a failed fetch keeps the old url and re-keys nothing`() =
        runTest {
            seed()

            assertFalse(changer(fetch = { throw IOException("down") }).change(old, new))

            assertEquals(old, repository.playlists.value.single().sourceUrl)
            assertTrue(settingsRekeys.isEmpty())
            assertTrue(epgRekeys.isEmpty())
        }

    @Test
    fun `a new url already taken by another playlist is rejected`() =
        runTest {
            seed()
            repository.add(new, M3uPlaylist(), name = "Bedroom")

            assertFalse(changer().change(old, new))

            assertEquals(listOf(old, new), repository.playlists.value.map { it.sourceUrl })
            assertTrue(settingsRekeys.isEmpty())
        }

    @Test
    fun `cancellation is rethrown, never swallowed`() =
        runTest {
            seed()
            try {
                changer(fetch = { throw CancellationException("cancelled") }).change(old, new)
                fail("Expected the CancellationException to propagate")
            } catch (expected: CancellationException) {
                assertEquals("cancelled", expected.message)
            }
        }
}
