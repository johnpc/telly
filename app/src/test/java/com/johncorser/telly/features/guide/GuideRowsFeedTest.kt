package com.johncorser.telly.features.guide

import com.johncorser.telly.features.guide.GuideTestData.at
import com.johncorser.telly.features.panel.PanelViewModel
import com.johncorser.telly.testutil.testChannel
import com.johncorser.telly.testutil.testProgram
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The feed's reactive contract behind the Assign-EPG scenario: a
 * `channels.epgOverride` write must re-run the programme window query with
 * the REMAPPED id and rebuild the rows — no caching keyed on the raw tvg-id.
 */
class GuideRowsFeedTest {
    private val programs = listOf(testProgram("news-1", at(14, 0), at(17, 0), "World News Now"))

    @Test
    fun `an epg override write re-queries programmes and fills the row`() =
        runTest {
            val channels = MutableStateFlow(listOf(testChannel(1, 1, "Sports Arena", group = "Sports", tvgId = null)))
            val queried = mutableListOf<List<String>>()
            val feed =
                GuideRowsFeed(
                    sources = GuideRowsSources(channels, MutableStateFlow(PanelViewModel.ALL_CHANNELS)),
                    scrollX = MutableStateFlow(0f),
                    programsFor = { ids, _, _ ->
                        queried += ids
                        flowOf(programs.filter { it.channelTvgId in ids })
                    },
                    originMs = GuideTestData.originMs,
                    scope = backgroundScope,
                )
            runCurrent()
            assertTrue(feed.rows.value.single().cells.none { it.hasInfo })

            val base = channels.value.single()
            channels.value = listOf(base.copy(overrides = base.overrides.copy(epgOverride = "news-1")))
            runCurrent()

            assertEquals(listOf(emptyList(), listOf("news-1")), queried)
            val row = feed.rows.value.single()
            assertEquals("World News Now", row.cells.first { it.hasInfo }.program?.details?.title)
        }

    @Test
    fun `clearing the override falls back to the tvg-id lookup`() =
        runTest {
            val base = testChannel(1, 1, "News One", tvgId = "news-1")
            val overridden = base.copy(overrides = base.overrides.copy(epgOverride = "other"))
            val channels = MutableStateFlow(listOf(overridden))
            val feed =
                GuideRowsFeed(
                    sources = GuideRowsSources(channels, MutableStateFlow(PanelViewModel.ALL_CHANNELS)),
                    scrollX = MutableStateFlow(0f),
                    programsFor = { ids, _, _ -> flowOf(programs.filter { it.channelTvgId in ids }) },
                    originMs = GuideTestData.originMs,
                    scope = backgroundScope,
                )
            runCurrent()
            assertTrue(feed.rows.value.single().cells.none { it.hasInfo })

            channels.value = listOf(base)
            runCurrent()

            assertEquals("World News Now", feed.rows.value.single().cells.first { it.hasInfo }.program?.details?.title)
        }
}
