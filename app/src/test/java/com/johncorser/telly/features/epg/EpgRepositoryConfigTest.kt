package com.johncorser.telly.features.epg

import android.util.Xml
import com.johncorser.telly.core.db.inMemoryDb
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** The settings-driven EpgRepository config: description opt-out + trim. */
@RunWith(RobolectricTestRunner::class)
class EpgRepositoryConfigTest {
    private val database = inMemoryDb()
    private val server = MockWebServer()
    private var storeDescriptions = true

    private val repository =
        EpgRepository(
            programDao = database.programDao(),
            newParser = { Xml.newPullParser() },
            storeDescriptions = { storeDescriptions },
        )

    private val xml =
        """
        <tv>
          <programme start="20260913000000 +0000" stop="20260913010000 +0000" channel="one">
            <title>Morning Show</title><desc>All the news.</desc>
          </programme>
          <programme start="20260913010000 +0000" stop="20260913020000 +0000" channel="one">
            <title>Late Show</title><desc>More news.</desc>
          </programme>
        </tv>
        """.trimIndent()

    @After
    fun tearDown() {
        server.shutdown()
        database.close()
    }

    private fun epgUrl(): String {
        server.start()
        return server.url("/epg.xml").toString()
    }

    private suspend fun storedPrograms() =
        database
            .programDao()
            .observeWindow(listOf("one"), 0, Long.MAX_VALUE)
            .first()

    @Test
    fun `descriptions are dropped when the setting is off`() =
        runTest {
            storeDescriptions = false
            server.enqueue(MockResponse().setBody(xml))

            repository.refresh(epgUrl())

            storedPrograms().forEach { assertNull(it.details.description) }
        }

    @Test
    fun `descriptions are kept by default`() =
        runTest {
            server.enqueue(MockResponse().setBody(xml))

            repository.refresh(epgUrl())

            assertEquals("All the news.", storedPrograms().first().details.description)
        }

    @Test
    fun `trimEndedBefore drops only programmes past the horizon`() =
        runTest {
            server.enqueue(MockResponse().setBody(xml))
            repository.refresh(epgUrl())
            val endOfFirst = storedPrograms().first().endMs

            repository.trimEndedBefore(endOfFirst + 1)

            val remaining = storedPrograms()
            assertEquals(1, remaining.size)
            assertTrue(remaining.single().details.title == "Late Show")
        }
}
