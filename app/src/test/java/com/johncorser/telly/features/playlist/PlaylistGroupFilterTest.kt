package com.johncorser.telly.features.playlist

import com.johncorser.telly.features.playlist.db.PlaylistEntity
import com.johncorser.telly.testutil.FakeChannelDao
import com.johncorser.telly.testutil.testChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaylistGroupFilterTest {
    private val url = "http://p/list.m3u"
    private val urlById = mapOf(1L to url)
    private val news = testChannel(id = 1, number = 1, name = "News One", group = "News")
    private val movie = testChannel(id = 2, number = 2, name = "Movie House", group = "Movies")
    private val ungrouped = testChannel(id = 3, number = 3, name = "Loose", group = null)

    @Test
    fun `channels of disabled groups drop out`() {
        val visible =
            PlaylistGroupFilter.visible(listOf(news, movie), urlById) { _, group -> group != "Movies" }

        assertEquals(listOf("News One"), visible.map { it.source.name })
    }

    @Test
    fun `ungrouped channels and unknown playlists always stay visible`() {
        val foreign = movie.copy(playlistId = 99)

        val visible =
            PlaylistGroupFilter.visible(listOf(news, ungrouped, foreign), urlById) { _, _ -> false }

        assertEquals(listOf("Loose", "Movie House"), visible.map { it.source.name }.sorted())
    }

    @Test
    fun `the dao wrapper filters observeVisible live as toggles flip`() =
        runTest {
            val delegate = FakeChannelDao(listOf(news, movie))
            val settingsChanges = MutableStateFlow(emptyMap<String, String>())
            val disabled = mutableSetOf<String>()
            val dao =
                GroupFilteredChannelDao(
                    delegate = delegate,
                    playlists = MutableStateFlow(listOf(PlaylistEntity(id = 1, name = "p", url = url))),
                    settingsChanges = settingsChanges,
                    groupEnabled = { _, group -> group !in disabled },
                )

            assertEquals(listOf("News One", "Movie House"), dao.observeVisible().first().map { it.source.name })

            disabled += "News"
            settingsChanges.value = mapOf("playlist_group_enabled:$url:News" to "false")

            assertEquals(listOf("Movie House"), dao.observeVisible().first().map { it.source.name })
        }
}
