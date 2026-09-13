package com.johncorser.telly.features.epg

import android.util.Xml
import com.johncorser.telly.core.db.inMemoryDb
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class EpgRepositoryTest {
    private val database = inMemoryDb()
    private val server = MockWebServer()

    // Robolectric's sandbox cannot instrument kxml2's ancient class files, so
    // these tests use the platform parser Robolectric provides via android.util.Xml.
    private val repository = EpgRepository(programDao = database.programDao(), newParser = { Xml.newPullParser() })

    private val fixtureXml =
        javaClass.getResourceAsStream("/fixtures/epg.xml")!!.use { it.readBytes().decodeToString() }

    @After
    fun tearDown() {
        server.shutdown()
        database.close()
    }

    private fun epgUrl(): String {
        server.start()
        return server.url("/epg.xml").toString()
    }

    @Test
    fun `refresh downloads parses and stores the fixture epg`() =
        runTest {
            server.enqueue(MockResponse().setBody(fixtureXml))

            val stored = repository.refresh(epgUrl())

            assertEquals(917, stored)
            assertEquals(917, database.programDao().count())
            assertEquals("/epg.xml", server.takeRequest().path)
        }

    @Test
    fun `a second refresh replaces programmes instead of duplicating them`() =
        runTest {
            server.enqueue(MockResponse().setBody(fixtureXml))
            server.enqueue(MockResponse().setBody(fixtureXml))
            val url = epgUrl()

            repository.refresh(url)
            repository.refresh(url)

            assertEquals(917, database.programDao().count())
        }

    @Test
    fun `programsFor exposes the stored guide window`() =
        runTest {
            server.enqueue(MockResponse().setBody(fixtureXml))
            repository.refresh(epgUrl())
            val startMs = 1_789_302_600_000L // first fixture programme, 12:30Z

            val window =
                repository
                    .programsFor(listOf("news-one-1.fixture"), fromMs = startMs, toMs = startMs + 1)
                    .first()

            assertEquals("Weather Watch", window.single().details.title)
        }

    @Test
    fun `nowNext resolves the airing and following programme per channel`() =
        runTest {
            server.enqueue(MockResponse().setBody(fixtureXml))
            repository.refresh(epgUrl())
            val atMs = 1_789_302_600_000L + 60_000 // one minute into the first programme

            val nowNext =
                repository
                    .nowNext(listOf("news-one-1.fixture", "absent.fixture"), atMs = atMs)
                    .first()

            val newsOne = nowNext.getValue("news-one-1.fixture")
            assertEquals("Weather Watch", newsOne.now?.details?.title)
            assertEquals(1_789_307_100_000L, newsOne.next?.startMs)
            assertTrue("absent.fixture" !in nowNext)
        }

    @Test
    fun `http errors surface as io exceptions`() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(500))

            try {
                repository.refresh(epgUrl())
                fail("expected IOException")
            } catch (expected: IOException) {
                assertTrue(expected.message!!.contains("500"))
            }
            assertEquals(0, database.programDao().count())
        }

    @Test
    fun `an empty body stores nothing`() =
        runTest {
            server.enqueue(MockResponse().setBody("<tv/>"))

            assertEquals(0, repository.refresh(epgUrl()))
            assertEquals(0, database.programDao().count())
        }
}
