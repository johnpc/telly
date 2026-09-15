package com.johncorser.telly.features.settings

import com.johncorser.telly.core.settings.InMemoryKeyValueStore
import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.core.settings.TellySettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** The four Appearance sub-panes: rows, defaults, pickers and routing. */
class SettingsRowsAppearancePanesTest {
    private val s = SettingsRepository(InMemoryKeyValueStore())

    @Test
    fun `tv guide pane lists density, transparency and channel numbers with today's defaults`() {
        val rows = appearanceTvGuideRows(s)
        assertEquals(
            listOf("Number of visible channels", "Panel transparency", "Show channel numbers"),
            rows.map { (it as? SettingsRow.Value)?.title ?: (it as SettingsRow.Toggle).title },
        )
        assertEquals("7", (rows[0] as SettingsRow.Value).summary)
        assertEquals("Opaque", (rows[1] as SettingsRow.Value).summary)
        assertTrue((rows[2] as SettingsRow.Toggle).checked)
    }

    @Test
    fun `player pane lists transparency, timeout and clock with today's defaults`() {
        val rows = appearancePlayerRows(s)
        assertEquals("0%", (rows[0] as SettingsRow.Value).summary)
        assertEquals("Panels timeout, sec", (rows[1] as SettingsRow.Value).title)
        assertEquals("5", (rows[1] as SettingsRow.Value).summary)
        assertTrue((rows[2] as SettingsRow.Toggle).checked)
    }

    @Test
    fun `groups pane toggles the two synthetic groups, both on by default`() {
        val rows = appearanceGroupsRows(s)
        assertEquals(
            listOf("Show 'All channels' group", "Show 'Favorites' group"),
            rows.map { (it as SettingsRow.Toggle).title },
        )
        rows.forEach { assertTrue((it as SettingsRow.Toggle).checked) }
    }

    @Test
    fun `logos pane offers background and rounded corners with today's defaults`() {
        val rows = appearanceLogosRows(s)
        assertEquals("Default", (rows[0] as SettingsRow.Value).summary)
        assertTrue((rows[1] as SettingsRow.Toggle).checked)
    }

    @Test
    fun `summaries reflect changed settings`() {
        s.set(TellySettings.GUIDE_VISIBLE_CHANNELS, 9)
        s.set(TellySettings.PLAYER_TRANSPARENCY, 25)
        assertEquals("9", (appearanceTvGuideRows(s)[0] as SettingsRow.Value).summary)
        assertEquals("25%", (appearancePlayerRows(s)[0] as SettingsRow.Value).summary)
    }

    @Test
    fun `the four sub-screen rows route to their panes and nothing else does`() {
        assertEquals(SettingsPane.AppearanceTvGuide, appearancePaneFor(RowIds.APPEARANCE_TV_GUIDE))
        assertEquals(SettingsPane.AppearancePlayer, appearancePaneFor(RowIds.APPEARANCE_PLAYER))
        assertEquals(SettingsPane.AppearanceGroups, appearancePaneFor(RowIds.APPEARANCE_GROUPS))
        assertEquals(SettingsPane.AppearanceLogos, appearancePaneFor(RowIds.APPEARANCE_LOGOS))
        assertNull(appearancePaneFor(RowIds.APPEARANCE_COLOR_THEME))
    }

    @Test
    fun `rowsFor and paneTitle serve the appearance panes`() {
        val panes =
            mapOf(
                SettingsPane.AppearanceTvGuide to "TV guide",
                SettingsPane.AppearancePlayer to "Player",
                SettingsPane.AppearanceGroups to "Groups",
                SettingsPane.AppearanceLogos to "Logos",
            )
        panes.forEach { (pane, title) ->
            assertTrue("$title pane renders rows", rowsFor(pane, s, emptyList(), "0.1.0").isNotEmpty())
            assertEquals(title, paneTitle(pane, emptyList()))
        }
    }

    @Test
    fun `every appearance picker exists with the stored default highlighted`() {
        val expected =
            mapOf(
                RowIds.APPEARANCE_GUIDE_VISIBLE_CHANNELS to "7",
                RowIds.APPEARANCE_GUIDE_TRANSPARENCY to "Opaque",
                RowIds.APPEARANCE_PLAYER_TRANSPARENCY to "0",
                RowIds.APPEARANCE_PLAYER_TIMEOUT to "5",
                RowIds.APPEARANCE_LOGOS_BACKGROUND to "Default",
                RowIds.APPEARANCE_LANGUAGE to "System",
                RowIds.APPEARANCE_FONT_SIZE to "Medium",
            )
        expected.forEach { (rowId, default) ->
            val spec = SettingsPickers.byRowId.getValue(rowId)
            assertEquals(rowId, default, SettingsPickers.currentRaw(spec, emptyMap()))
            assertTrue(rowId, spec.options.any { it.raw == default })
        }
    }

    @Test
    fun `appearance toggles flip their settings keys`() {
        val toggles =
            mapOf(
                RowIds.APPEARANCE_GUIDE_CHANNEL_NUMBERS to TellySettings.SHOW_CHANNEL_NUMBERS,
                RowIds.APPEARANCE_PLAYER_SHOW_CLOCK to TellySettings.PLAYER_SHOW_CLOCK,
                RowIds.APPEARANCE_GROUPS_ALL_CHANNELS to TellySettings.SHOW_ALL_CHANNELS_GROUP,
                RowIds.APPEARANCE_GROUPS_FAVORITES to TellySettings.SHOW_FAVORITES_GROUP,
                RowIds.APPEARANCE_LOGOS_ROUNDED to TellySettings.LOGO_ROUNDED_CORNERS,
            )
        toggles.forEach { (rowId, setting) -> assertEquals(rowId, setting, SettingsToggles.byRowId[rowId]) }
    }
}
