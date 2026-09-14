package com.johncorser.telly.core

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.johncorser.telly.features.playlist.M3uChannel
import com.johncorser.telly.features.playlist.M3uPlaylist
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ServiceLocatorTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val server = MockWebServer()

    @After
    fun tearDown() {
        server.shutdown()
    }

    /** One end-to-end pass so the composition root shares a single database. */
    @Test
    fun `wires the persistence stack end to end`() =
        runTest {
            assertSame(ServiceLocator.database(context), ServiceLocator.database(context))

            server.start()
            val fixtureXml =
                javaClass.getResourceAsStream("/fixtures/epg.xml")!!.use { it.readBytes().decodeToString() }
            server.enqueue(MockResponse().setBody(fixtureXml))
            val epgUrl = server.url("/epg.xml").toString()

            val repository = ServiceLocator.playlistRepository(context)
            repository.add(
                "http://p/playlist.m3u",
                M3uPlaylist(
                    epgUrl = epgUrl,
                    channels =
                        listOf(
                            M3uChannel(
                                title = "News One",
                                streamUrl = "http://s/1.ts",
                                tvgId = "news-one-1.fixture",
                            ),
                        ),
                ),
            )
            assertEquals(1, ServiceLocator.database(context).channelDao().totalCount())

            // The refresher pulls the EPG through android.util.Xml's pull parser.
            val refreshed = ServiceLocator.epgRefresher(context).refreshDue()

            assertEquals(1, refreshed.size)
            assertEquals(917, ServiceLocator.database(context).programDao().count())
            assertTrue(
                ServiceLocator.database(context).playlistDao().all().single().epgLastUpdatedMs > 0,
            )

            val stored = repository.playlists.first().single()
            assertEquals("http://p/playlist.m3u", stored.sourceUrl)

            // epgRepository is buildable standalone against the same database.
            val nowNext =
                ServiceLocator
                    .epgRepository(context)
                    .nowNext(listOf("news-one-1.fixture"), atMs = 1_789_302_660_000L)
                    .first()
            assertEquals("Weather Watch", nowNext.getValue("news-one-1.fixture").now?.details?.title)
        }

    @Test
    fun `playback deps wire the kv store, engine factory and clock`() {
        val deps = ServiceLocator.playbackDeps(context)

        deps.keyValueStore.putLong("lastChannelId", 7)
        assertEquals(7L, ServiceLocator.keyValueStore(context).getLong("lastChannelId"))

        val engine = deps.engineFactory()
        assertSame(deps.sources.channelDao, ServiceLocator.database(context).channelDao())
        engine.release()
        assertTrue(deps.clock() > 0)
    }
}
