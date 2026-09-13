package com.johncorser.telly.features.playlist

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryPlaylistRepositoryTest {
    private val playlist = M3uPlaylist(channels = listOf(M3uChannel(title = "One", streamUrl = "http://s/1.ts")))

    @Test
    fun `starts empty`() {
        assertEquals(emptyList<StoredPlaylist>(), InMemoryPlaylistRepository().playlists.value)
    }

    @Test
    fun `add appends playlists in order`() =
        runTest {
            val repository = InMemoryPlaylistRepository()

            repository.add("http://a.example/a.m3u", playlist)
            repository.add("http://b.example/b.m3u", M3uPlaylist())

            assertEquals(
                listOf("http://a.example/a.m3u", "http://b.example/b.m3u"),
                repository.playlists.value.map { it.sourceUrl },
            )
        }

    @Test
    fun `re-adding the same source url replaces the stored copy`() =
        runTest {
            val repository = InMemoryPlaylistRepository()
            repository.add("http://a.example/a.m3u", M3uPlaylist())

            repository.add("http://a.example/a.m3u", playlist)

            assertEquals(listOf(StoredPlaylist("http://a.example/a.m3u", playlist)), repository.playlists.value)
        }

    @Test
    fun `add records the caller-provided name`() =
        runTest {
            val repository = InMemoryPlaylistRepository()

            repository.add("http://a.example/a.m3u", playlist, name = "My IPTV")

            assertEquals("My IPTV", repository.playlists.value.single().name)
        }

    @Test
    fun `re-adding without a name keeps the stored name`() =
        runTest {
            val repository = InMemoryPlaylistRepository()
            repository.add("http://a.example/a.m3u", playlist, name = "My IPTV")

            repository.add("http://a.example/a.m3u", M3uPlaylist())

            assertEquals("My IPTV", repository.playlists.value.single().name)
        }

    @Test
    fun `rename changes only the matching playlist`() =
        runTest {
            val repository = InMemoryPlaylistRepository()
            repository.add("http://a.example/a.m3u", playlist, name = "Old")
            repository.add("http://b.example/b.m3u", M3uPlaylist(), name = "Other")

            repository.rename("http://a.example/a.m3u", "New")

            assertEquals(listOf("New", "Other"), repository.playlists.value.map { it.name })
        }

    @Test
    fun `delete removes the playlist by url`() =
        runTest {
            val repository = InMemoryPlaylistRepository()
            repository.add("http://a.example/a.m3u", playlist)

            repository.delete("http://a.example/a.m3u")

            assertTrue(repository.playlists.value.isEmpty())
        }

    @Test
    fun `stored playlists behave as value objects`() {
        val stored = StoredPlaylist(sourceUrl = "http://a.example/a.m3u", playlist = playlist, name = "n")

        assertEquals(stored, stored.copy())
        assertEquals(stored.hashCode(), stored.copy().hashCode())
        assertNotEquals(stored, stored.copy(sourceUrl = "http://b.example/b.m3u"))
        assertEquals("http://a.example/a.m3u", stored.component1())
        assertEquals(playlist, stored.component2())
        assertEquals("n", stored.component3())
        assertTrue(stored.toString().contains("a.m3u"))
    }
}
