package com.johncorser.telly.features.guide

import com.johncorser.telly.features.groups.CustomGroup
import com.johncorser.telly.features.guide.GuideTestData.at
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

    private fun build(group: String): List<GuideRow> =
        GuideRowsBuilder.build(GuideRowsInput(channels, group, span), programs)

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
    fun `an assigned EPG override remaps the channel's programme cells`() {
        val remapped =
            channels.map { row ->
                if (row.id == 3L) row.copy(overrides = row.overrides.copy(epgOverride = "tvg-1")) else row
            }

        val rows = GuideRowsBuilder.build(GuideRowsInput(remapped, "Sports", span), programs)

        val extra = rows.single { it.channel.id == 3L }
        assertEquals("Business Hour", extra.cells.first { it.hasInfo }.program?.details?.title)
    }

    @Test
    fun `a custom group renders its member rows renumbered from one`() {
        val custom = CustomGroup(1, "Picks", members = setOf("tvg-3", "tvg-1"))

        val rows = GuideRowsBuilder.build(GuideRowsInput(channels, "Picks", span, listOf(custom)), programs)

        assertEquals(listOf("News One", "Sports Extra"), rows.map { it.channel.source.name })
        assertEquals(listOf(1, 2), rows.map { it.displayNumber })
    }
}
