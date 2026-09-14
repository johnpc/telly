package com.johncorser.telly.features.guide

import com.johncorser.telly.features.guide.GuideTestData.at
import com.johncorser.telly.features.history.HistoryGroup
import com.johncorser.telly.features.panel.PanelViewModel
import com.johncorser.telly.testutil.asFavorite
import com.johncorser.telly.testutil.testChannel
import com.johncorser.telly.testutil.testProgram
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GuideRowsBuilderTest {
    private val span = GuideSpan(at(14, 0), at(17, 0))
    private val channels =
        listOf(
            testChannel(1, 1, "News One", group = "News"),
            testChannel(2, 2, "Sports Arena", group = "Sports").asFavorite(),
            testChannel(3, 3, "Sports Extra", group = "Sports"),
            testChannel(4, 4, "Silent", group = "Music", tvgId = null),
        )
    private val programs =
        listOf(
            testProgram("tvg-1", at(14, 30), at(15, 45), "Business Hour"),
            testProgram("tvg-2", at(14, 0), at(17, 0), "Boxing Classics"),
        )

    private fun build(
        group: String,
        historyKeys: List<String> = emptyList(),
    ): List<GuideRow> = GuideRowsBuilder.build(GuideRowsInput(channels, group, historyKeys, span), programs)

    @Test
    fun `all channels keeps playlist numbers`() {
        val rows = build(PanelViewModel.ALL_CHANNELS)

        assertEquals(listOf(1, 2, 3, 4), rows.map { it.displayNumber })
        assertEquals("Business Hour", rows[0].cells.first { it.hasInfo }.program?.details?.title)
    }

    @Test
    fun `groups renumber from one like capture 74`() {
        val rows = build("Sports")

        assertEquals(listOf("Sports Arena", "Sports Extra"), rows.map { it.channel.source.name })
        assertEquals(listOf(1, 2), rows.map { it.displayNumber })
    }

    @Test
    fun `the favorites group filters to favorite channels`() {
        val rows = build(PanelViewModel.FAVORITES)

        assertEquals(listOf(2L), rows.map { it.channel.id })
    }

    @Test
    fun `EPG-less channels get no-information strips covering the span`() {
        val rows = build(PanelViewModel.ALL_CHANNELS)

        val silent = rows.last()
        assertTrue(silent.cells.isNotEmpty())
        assertTrue(silent.cells.none { it.hasInfo })
    }

    @Test
    fun `the history group orders by watch recency and renumbers from one`() {
        val rows = build(HistoryGroup.NAME, historyKeys = listOf("http://s/4.ts|Silent", "tvg-1", "tvg-9"))

        assertEquals(listOf("Silent", "News One"), rows.map { it.channel.source.name })
        assertEquals(listOf(1, 2), rows.map { it.displayNumber })
    }
}
