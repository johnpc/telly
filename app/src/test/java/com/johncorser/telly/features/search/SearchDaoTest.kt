package com.johncorser.telly.features.search

import com.johncorser.telly.core.db.inMemoryDb
import com.johncorser.telly.features.epg.db.ProgramDetails
import com.johncorser.telly.features.epg.db.ProgramEntity
import com.johncorser.telly.features.playlist.db.ChannelEntity
import com.johncorser.telly.features.playlist.db.ChannelFlags
import com.johncorser.telly.features.playlist.db.ChannelSource
import com.johncorser.telly.features.playlist.db.PlaylistEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** The real LIKE queries behind search, against an in-memory Room DB. */
@RunWith(RobolectricTestRunner::class)
class SearchDaoTest {
    private val database = inMemoryDb()
    private val searchDao = database.searchDao()

    @After
    fun tearDown() {
        database.close()
    }

    private suspend fun seedChannels(vararg specs: Triple<Int, String, Boolean>) {
        val playlistId = database.playlistDao().upsert(PlaylistEntity(name = "p", url = "http://p/x.m3u"))
        database.channelDao().insertAll(
            specs.map { (number, name, hidden) ->
                ChannelEntity(
                    playlistId = playlistId,
                    number = number,
                    sortIndex = number - 1,
                    source = ChannelSource(name = name, streamUrl = "http://s/$number.ts"),
                    flags = ChannelFlags(hidden = hidden),
                )
            },
        )
    }

    private fun program(
        startMs: Long,
        endMs: Long,
        title: String,
    ) = ProgramEntity(channelTvgId = "one", startMs = startMs, endMs = endMs, details = ProgramDetails(title = title))

    @Test
    fun `channel search matches name substrings case-insensitively in number order`() =
        runTest {
            seedChannels(
                Triple(3, "World NEWS Now", false),
                Triple(1, "News One", false),
                Triple(2, "Sports Arena", false),
            )

            val hits = searchDao.channels(SearchQuery.nameLike("news"), SearchQuery.numberLike("news"))

            assertEquals(listOf("News One", "World NEWS Now"), hits.map { it.source.name })
        }

    @Test
    fun `channel search matches digit queries as a number prefix`() =
        runTest {
            seedChannels(
                Triple(2, "Alpha", false),
                Triple(24, "Beta", false),
                Triple(42, "Gamma", false),
            )

            val hits = searchDao.channels(SearchQuery.nameLike("2"), SearchQuery.numberLike("2"))

            assertEquals(listOf(2, 24), hits.map { it.number })
        }

    @Test
    fun `channel search never returns hidden channels`() =
        runTest {
            seedChannels(Triple(1, "News One", true))

            assertEquals(0, searchDao.channels(SearchQuery.nameLike("news"), SearchQuery.numberLike("")).size)
        }

    @Test
    fun `LIKE wildcards in the query are treated literally`() =
        runTest {
            seedChannels(
                Triple(1, "100% News", false),
                Triple(2, "100x News", false),
            )

            val hits = searchDao.channels(SearchQuery.nameLike("100%"), SearchQuery.numberLike("100%"))

            assertEquals(listOf("100% News"), hits.map { it.source.name })
        }

    @Test
    fun `programme search is title substring upcoming first with a limit`() =
        runTest {
            database.programDao().upsertAll(
                listOf(
                    program(0, 100, "Newsroom Ended"),
                    program(300, 400, "Newsroom Late"),
                    program(100, 200, "Newsroom Airing"),
                    program(200, 300, "Other Show"),
                ),
            )

            val hits = searchDao.programs(SearchQuery.nameLike("newsroom"), atMs = 150, limit = 10)

            assertEquals(listOf("Newsroom Airing", "Newsroom Late"), hits.map { it.details.title })
            assertEquals(1, searchDao.programs(SearchQuery.nameLike("newsroom"), atMs = 150, limit = 1).size)
        }

    @Test
    fun `programme search escapes underscores`() =
        runTest {
            database.programDao().upsertAll(
                listOf(
                    program(100, 200, "a_b show"),
                    program(200, 300, "axb show"),
                ),
            )

            val hits = searchDao.programs(SearchQuery.nameLike("a_b"), atMs = 0, limit = 10)

            assertEquals(listOf("a_b show"), hits.map { it.details.title })
        }
}
