package com.johncorser.telly.features.settings

import com.johncorser.telly.features.playlist.InMemoryPlaylistRepository
import com.johncorser.telly.features.playlist.M3uPlaylist
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class PlaylistUpdaterTest {
    private val repository = InMemoryPlaylistRepository()
    private val body =
        """
        #EXTM3U url-tvg="http://e/epg.xml"
        #EXTINF:-1 tvg-id="one" group-title="News",News One
        http://s/1.ts
        """.trimIndent()

    @Test
    fun `update re-fetches, re-parses and re-imports the stored url`() =
        runTest {
            val updater = PlaylistUpdater(fetchPlaylist = { body }, repository = repository)

            assertTrue(updater.update("http://p/x.m3u"))

            val stored = repository.playlists.value.single()
            assertEquals("http://p/x.m3u", stored.sourceUrl)
            assertEquals(listOf("News One"), stored.playlist.channels.map { it.title })
            assertEquals("http://e/epg.xml", stored.playlist.epgUrl)
        }

    @Test
    fun `a failed fetch leaves the stored playlist untouched`() =
        runTest {
            repository.add("http://p/x.m3u", M3uPlaylist(), name = "Keep")
            val updater = PlaylistUpdater(fetchPlaylist = { throw IOException("down") }, repository = repository)

            assertFalse(updater.update("http://p/x.m3u"))

            assertEquals("Keep", repository.playlists.value.single().name)
        }

    @Test
    fun `updateAll reports only the successful urls`() =
        runTest {
            val updater =
                PlaylistUpdater(
                    fetchPlaylist = { url -> if (url.contains("bad")) throw IOException("down") else body },
                    repository = repository,
                )

            assertEquals(
                listOf("http://p/good.m3u"),
                updater.updateAll(listOf("http://p/bad.m3u", "http://p/good.m3u")),
            )
        }
}
