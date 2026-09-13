package com.johncorser.telly.features.playlist.db

import com.johncorser.telly.core.db.inMemoryDb
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ChannelDaoTest {
    private val database = inMemoryDb()
    private val channelDao = database.channelDao()
    private val playlistDao = database.playlistDao()

    @After
    fun tearDown() {
        database.close()
    }

    private fun channel(
        playlistId: Long,
        number: Int,
        name: String,
        group: String?,
        hidden: Boolean = false,
    ) = ChannelEntity(
        playlistId = playlistId,
        number = number,
        sortIndex = number - 1,
        source = ChannelSource(name = name, groupTitle = group, streamUrl = "http://s/$number.ts"),
        flags = ChannelFlags(hidden = hidden),
    )

    private suspend fun seed(): Long {
        val playlistId = playlistDao.upsert(PlaylistEntity(name = "p", url = "http://p/x.m3u"))
        channelDao.insertAll(
            listOf(
                channel(playlistId, 1, "News One", "News"),
                channel(playlistId, 2, "News Two", "News", hidden = true),
                channel(playlistId, 3, "Sports Arena", "Sports"),
                channel(playlistId, 4, "Loose Channel", null),
            ),
        )
        return playlistId
    }

    @Test
    fun `channels come back ordered by number`() =
        runTest {
            val playlistId = seed()
            val names = channelDao.observeForPlaylist(playlistId).first().map { it.source.name }
            assertEquals(listOf("News One", "News Two", "Sports Arena", "Loose Channel"), names)
        }

    @Test
    fun `by-group filters the group and hides hidden channels`() =
        runTest {
            val playlistId = seed()
            val news = channelDao.observeByGroup(playlistId, "News").first()
            assertEquals(listOf("News One"), news.map { it.source.name })
        }

    @Test
    fun `groups come with visible-channel counts in playlist order`() =
        runTest {
            val playlistId = seed()
            val groups = channelDao.observeGroups(playlistId).first()
            assertEquals(
                listOf(
                    ChannelGroupCount(groupTitle = "News", channelCount = 1),
                    ChannelGroupCount(groupTitle = "Sports", channelCount = 1),
                    ChannelGroupCount(groupTitle = null, channelCount = 1),
                ),
                groups,
            )
        }

    @Test
    fun `observeVisible spans playlists, skips hidden and orders by number`() =
        runTest {
            seed()
            val visible = channelDao.observeVisible().first()
            assertEquals(listOf("News One", "Sports Arena", "Loose Channel"), visible.map { it.source.name })
            assertEquals(listOf(1, 3, 4), visible.map { it.number })
        }

    @Test
    fun `updating a row persists user flags`() =
        runTest {
            val playlistId = seed()
            val target = channelDao.forPlaylist(playlistId).first()

            channelDao.update(target.copy(flags = target.flags.copy(favorite = true)))

            assertEquals(true, channelDao.forPlaylist(playlistId).first().flags.favorite)
        }

    @Test
    fun `deleting the playlist row cascades to its channels`() =
        runTest {
            val playlistId = seed()
            assertEquals(4, channelDao.totalCount())

            // Insert-or-replace on the same unique url replaces the playlist row.
            playlistDao.upsert(PlaylistEntity(id = playlistId, name = "p2", url = "http://p/x.m3u"))

            assertEquals(0, channelDao.totalCount())
        }
}
