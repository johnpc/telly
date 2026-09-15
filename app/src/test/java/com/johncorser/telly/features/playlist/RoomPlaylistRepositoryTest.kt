package com.johncorser.telly.features.playlist

import com.johncorser.telly.core.db.inMemoryDb
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RoomPlaylistRepositoryTest {
    private val database = inMemoryDb()
    private var nowMs = 1_000L
    private val repository = RoomPlaylistRepository(database) { nowMs }

    private val playlist =
        M3uPlaylist(
            epgUrl = "http://e/epg.xml",
            channels =
                listOf(
                    M3uChannel(
                        title = "News One",
                        streamUrl = "http://s/1.ts",
                        tvgId = "news-1",
                        tvgLogo = "http://l/1.png",
                        groupTitle = "News",
                    ),
                    M3uChannel(title = "Sports Arena", streamUrl = "http://s/2.ts", tvgId = "sports-1"),
                ),
        )

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `starts empty`() =
        runTest {
            assertTrue(repository.playlists.first().isEmpty())
        }

    @Test
    fun `add persists the playlist and numbered channels`() =
        runTest {
            repository.add("http://p/playlist.m3u", playlist)

            val row = database.playlistDao().all().single()
            assertEquals("playlist.m3u", row.name)
            assertEquals("http://p/playlist.m3u", row.url)
            assertEquals("http://e/epg.xml", row.epgUrl)
            assertEquals(1_000L, row.lastUpdatedMs)
            assertEquals(0L, row.epgLastUpdatedMs)

            val channels = database.channelDao().forPlaylist(row.id)
            assertEquals(listOf(1, 2), channels.map { it.number })
            assertEquals(listOf("News One", "Sports Arena"), channels.map { it.source.name })
        }

    @Test
    fun `the playlists flow round-trips stored channels`() =
        runTest {
            repository.add("http://p/playlist.m3u", playlist)

            val stored = repository.playlists.first().single()

            assertEquals("http://p/playlist.m3u", stored.sourceUrl)
            assertEquals("http://e/epg.xml", stored.playlist.epgUrl)
            val channel = stored.playlist.channels.first()
            assertEquals("News One", channel.title)
            assertEquals("http://s/1.ts", channel.streamUrl)
            assertEquals("news-1", channel.tvgId)
            assertEquals("http://l/1.png", channel.tvgLogo)
            assertEquals("News", channel.groupTitle)
        }

    @Test
    fun `re-adding the same url replaces channels and keeps user flags`() =
        runTest {
            repository.add("http://p/playlist.m3u", playlist)
            val channelDao = database.channelDao()
            val favorite = channelDao.forPlaylist(database.playlistDao().all().single().id).first()
            channelDao.update(favorite.copy(flags = favorite.flags.copy(favorite = true)))

            nowMs = 2_000L
            val refreshed =
                playlist.copy(
                    channels = listOf(playlist.channels[1], playlist.channels[0].copy(title = "News One HD")),
                )
            repository.add("http://p/playlist.m3u", refreshed)

            val row = database.playlistDao().all().single()
            assertEquals(2_000L, row.lastUpdatedMs)
            val channels = channelDao.forPlaylist(row.id)
            assertEquals(listOf("Sports Arena", "News One HD"), channels.map { it.source.name })
            assertEquals(listOf(1, 2), channels.map { it.number })
            // Matched by tvg-id, the favorite flag survived the refresh + rename.
            assertEquals(listOf(false, true), channels.map { it.flags.favorite })
        }

    @Test
    fun `video-file entries import as VOD items, never as channels`() =
        runTest {
            val withVod =
                playlist.copy(
                    channels =
                        playlist.channels +
                            M3uChannel(
                                title = "Big Buck Bunny",
                                streamUrl = "http://s/bbb.mp4",
                                tvgLogo = "http://l/bbb.png",
                                groupTitle = "Cinema",
                            ),
                )

            repository.add("http://p/playlist.m3u", withVod)

            val row = database.playlistDao().all().single()
            assertEquals(
                listOf("News One", "Sports Arena"),
                database.channelDao().forPlaylist(row.id).map { it.source.name },
            )
            val vod = database.vodItemDao().observeAll().first().single()
            assertEquals("Big Buck Bunny", vod.name)
            assertEquals("Cinema", vod.groupTitle)
            assertEquals("http://l/bbb.png", vod.logoUrl)
            assertEquals("http://s/bbb.mp4", vod.streamUrl)

            // A refresh without the entry replaces the playlist's VOD rows.
            repository.add("http://p/playlist.m3u", playlist)
            assertEquals(0, database.vodItemDao().totalCount())
        }

    @Test
    fun `deleting a playlist cascades to its VOD items`() =
        runTest {
            val withVod =
                playlist.copy(
                    channels = listOf(M3uChannel(title = "Movie", streamUrl = "http://s/m.mkv")),
                )
            repository.add("http://p/playlist.m3u", withVod)
            assertEquals(1, database.vodItemDao().totalCount())

            repository.delete("http://p/playlist.m3u")

            assertEquals(0, database.vodItemDao().totalCount())
        }

    @Test
    fun `distinct urls stay as separate playlists`() =
        runTest {
            repository.add("http://p/a.m3u", playlist)
            repository.add("http://p/b.m3u", M3uPlaylist())

            assertEquals(
                listOf("http://p/a.m3u", "http://p/b.m3u"),
                repository.playlists.first().map { it.sourceUrl },
            )
        }

    @Test
    fun `an explicit name is persisted and survives a nameless re-add`() =
        runTest {
            repository.add("http://p/playlist.m3u", playlist, name = "My IPTV")
            assertEquals("My IPTV", database.playlistDao().all().single().name)
            assertEquals("My IPTV", repository.playlists.first().single().name)

            // A refresh without a name (e.g. background update) keeps the custom name.
            repository.add("http://p/playlist.m3u", playlist)
            assertEquals("My IPTV", database.playlistDao().all().single().name)
        }

    @Test
    fun `playlist names use the last path segment ignoring queries`() =
        runTest {
            repository.add("http://host/lists/tv.m3u?token=abc", M3uPlaylist())
            assertEquals("tv.m3u", database.playlistDao().all().single().name)
        }

    @Test
    fun `playlist names fall back to the source url when no segment exists`() =
        runTest {
            repository.add("?token=abc", M3uPlaylist())
            assertEquals("?token=abc", database.playlistDao().all().single().name)
        }

    @Test
    fun `rename persists across a fresh repository instance`() =
        runTest {
            repository.add("http://p/playlist.m3u", playlist)

            repository.rename("http://p/playlist.m3u", "Living room")

            assertEquals("Living room", RoomPlaylistRepository(database) { nowMs }.playlists.first().single().name)
        }

    @Test
    fun `delete removes the playlist and all of its channels`() =
        runTest {
            repository.add("http://p/playlist.m3u", playlist)
            repository.add("http://p/other.m3u", M3uPlaylist())

            repository.delete("http://p/playlist.m3u")

            assertEquals(listOf("http://p/other.m3u"), repository.playlists.first().map { it.sourceUrl })
            assertEquals(0, database.channelDao().totalCount())
        }

    @Test
    fun `deleting an unknown url is a no-op`() =
        runTest {
            repository.add("http://p/playlist.m3u", playlist)
            repository.delete("http://p/unknown.m3u")
            assertEquals(1, repository.playlists.first().size)
        }

    @Test
    fun `changeUrl re-keys the row in place, keeping name, channels and flags`() =
        runTest {
            repository.add("http://p/playlist.m3u", playlist, name = "Living room")
            val channelDao = database.channelDao()
            val before = database.playlistDao().all().single()
            val favorite = channelDao.forPlaylist(before.id).first()
            channelDao.update(favorite.copy(flags = favorite.flags.copy(favorite = true)))

            assertTrue(repository.changeUrl("http://p/playlist.m3u", "http://p/moved.m3u"))

            val row = database.playlistDao().all().single()
            assertEquals(before.id, row.id)
            assertEquals("http://p/moved.m3u", row.url)
            assertEquals("Living room", row.name)
            assertEquals(
                listOf(true, false),
                channelDao.forPlaylist(row.id).map { it.flags.favorite },
            )
        }

    @Test
    fun `changeUrl refuses a url owned by another playlist or an unknown source`() =
        runTest {
            repository.add("http://p/a.m3u", playlist)
            repository.add("http://p/b.m3u", M3uPlaylist())

            assertFalse(repository.changeUrl("http://p/a.m3u", "http://p/b.m3u"))
            assertFalse(repository.changeUrl("http://p/unknown.m3u", "http://p/c.m3u"))

            assertEquals(
                listOf("http://p/a.m3u", "http://p/b.m3u"),
                repository.playlists.first().map { it.sourceUrl },
            )
        }
}
