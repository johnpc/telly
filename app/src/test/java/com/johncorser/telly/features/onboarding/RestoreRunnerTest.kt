package com.johncorser.telly.features.onboarding

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RestoreRunnerTest {
    private val updated = mutableListOf<List<String>>()
    private var importResult: Boolean = true
    private var urls = listOf("http://host/one.m3u", "http://host/two.m3u")
    private var channels = 31

    private fun runner(importJson: suspend (String) -> Boolean = { importResult }) =
        RestoreRunner(
            importJson = importJson,
            playlistUrls = { urls },
            updatePlaylists = { updated += it },
            channelCount = { channels },
        )

    @Test
    fun `restores, re-fetches every playlist and reports channels landed`() =
        runTest {
            assertTrue(runner().restore("{}"))
            assertEquals(listOf(urls), updated)
        }

    @Test
    fun `a rejected backup restores nothing`() =
        runTest {
            importResult = false
            assertFalse(runner().restore("nope"))
            assertTrue(updated.isEmpty())
        }

    @Test
    fun `an import crash is contained`() =
        runTest {
            assertFalse(runner(importJson = { error("boom") }).restore("{}"))
            assertTrue(updated.isEmpty())
        }

    @Test
    fun `no channels after the re-fetch is an honest failure`() =
        runTest {
            channels = 0
            assertFalse(runner().restore("{}"))
            assertEquals(listOf(urls), updated)
        }
}
