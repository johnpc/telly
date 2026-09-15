package com.johncorser.telly.features.settings

import com.johncorser.telly.core.settings.InMemoryKeyValueStore
import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.features.epg.EpgSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsRowsIndexTest {
    private val s = SettingsRepository(InMemoryKeyValueStore())
    private val item =
        PlaylistItem(url = "http://p/x.m3u", name = "Home", channelCount = 3, epgUrl = "http://e/epg.xml")
    private val source = EpgSource(id = 5, playlistUrl = "http://p/x.m3u", url = "http://alt.example/epg.xml")

    @Test
    fun `rowsFor routes every section to a non-empty captured pane`() {
        SettingsSection.entries.forEach { section ->
            val rows = rowsFor(SettingsPane.Section(section), s, listOf(item), "0.1.0")
            assertTrue("$section should render rows", rows.size > 2)
            assertEquals(PREMIUM_NOTE, (rows[0] as SettingsRow.Note).text)
        }
    }

    @Test
    fun `rowsFor renders the playlist detail pane for a known url`() {
        val rows = rowsFor(SettingsPane.PlaylistDetail("http://p/x.m3u"), s, listOf(item), "0.1.0")
        assertTrue(rows.any { it.id == RowIds.PLAYLIST_DELETE })
    }

    @Test
    fun `rowsFor is empty for a deleted playlist url`() {
        assertEquals(
            emptyList<SettingsRow>(),
            rowsFor(SettingsPane.PlaylistDetail("http://gone"), s, emptyList(), "0.1.0"),
        )
    }

    @Test
    fun `rowsFor renders the epg sources pane`() {
        val rows = rowsFor(SettingsPane.EpgSources, s, listOf(item), "0.1.0", listOf(source))
        assertTrue(rows.any { it.id == RowIds.EPG_SOURCE_PREFIX + "http://p/x.m3u" })
        assertTrue(rows.any { it.id == RowIds.EPG_CUSTOM_SOURCE_PREFIX + "5" })
    }

    @Test
    fun `rowsFor renders a source detail pane and is empty once deleted`() {
        val rows = rowsFor(SettingsPane.EpgSourceDetail(5), s, listOf(item), "0.1.0", listOf(source))
        assertTrue(rows.any { it.id == RowIds.EPG_SOURCE_DELETE })
        assertEquals(
            emptyList<SettingsRow>(),
            rowsFor(SettingsPane.EpgSourceDetail(5), s, listOf(item), "0.1.0"),
        )
    }

    @Test
    fun `paneTitle names sections, playlists and the epg sources panes`() {
        assertEquals("Remote control", paneTitle(SettingsPane.Section(SettingsSection.REMOTE_CONTROL), emptyList()))
        assertEquals("Home", paneTitle(SettingsPane.PlaylistDetail("http://p/x.m3u"), listOf(item)))
        assertEquals("http://gone", paneTitle(SettingsPane.PlaylistDetail("http://gone"), listOf(item)))
        assertEquals("EPG sources", paneTitle(SettingsPane.EpgSources, listOf(item)))
        assertEquals("alt.example", paneTitle(SettingsPane.EpgSourceDetail(5), listOf(item), listOf(source)))
        assertEquals("EPG source", paneTitle(SettingsPane.EpgSourceDetail(9), listOf(item), listOf(source)))
    }
}
