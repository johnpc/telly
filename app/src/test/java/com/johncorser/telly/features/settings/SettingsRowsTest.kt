package com.johncorser.telly.features.settings

import com.johncorser.telly.core.settings.InMemoryKeyValueStore
import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.core.settings.TellySettings
import com.johncorser.telly.features.epg.EpgSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Golden checks of every pane against the capture catalogue (§6). */
class SettingsRowsTest {
    private val s = SettingsRepository(InMemoryKeyValueStore())
    private val item =
        PlaylistItem(
            url = "http://10.0.2.2:8090/playlist.m3u",
            name = "10.0.2.2",
            channelCount = 30,
            epgUrl = "http://10.0.2.2:8090/epg.xml",
        )

    private fun titles(rows: List<SettingsRow>): List<String> =
        rows.mapNotNull {
            when (it) {
                is SettingsRow.Toggle -> it.title
                is SettingsRow.Value -> it.title
                is SettingsRow.Action -> it.title
                is SettingsRow.Header -> it.text
                is SettingsRow.Note -> null
            }
        }

    @Test
    fun `no pane prepends a premium note or Unlock Premium row`() {
        val panes =
            listOf(
                generalRows(s),
                playlistsRows(s, listOf(item)),
                epgRows(s),
                appearanceRows(s),
                playbackRows(s),
                remoteControlRows(s),
                parentalRows(s),
                otherRows(),
                aboutRows(s, "0.1.0"),
            )
        panes.forEach { rows ->
            assertFalse(rows.any { it is SettingsRow.Action && it.title == "Unlock Premium" })
        }
    }

    @Test
    fun `general lists the captured rows in order with captured defaults`() {
        val rows = generalRows(s)
        assertEquals(
            listOf(
                "Auto start app on boot", "Auto start app on wake up from sleep mode",
                "Turn on last channel on app start", "Switch to picture-in-picture mode on press Home",
                "Confirm exit by second press Back", "User-Agent", "UDP proxy (address:port)",
                "Back up data", "Restore data",
            ),
            titles(rows),
        )
        rows.filterIsInstance<SettingsRow.Toggle>().forEach { assertFalse(it.checked) }
        assertEquals("May not work on all devices", (rows[1] as SettingsRow.Toggle).summary)
        assertEquals("Not set", (rows[5] as SettingsRow.Value).summary)
        assertEquals("Not set", (rows[6] as SettingsRow.Value).summary)
    }

    @Test
    fun `playlists pane shows the playlist with channel count then list actions`() {
        val rows = playlistsRows(s, listOf(item))
        val playlistRow = rows[0] as SettingsRow.Value
        assertEquals("10.0.2.2", playlistRow.title)
        assertEquals("Channels: 30", playlistRow.summary)
        assertTrue(playlistRow.checkIcon)
        assertEquals(listOf("Playlists sorting", "Add playlist", "Update all playlists"), titles(rows.drop(1)))
        assertEquals("By name", (rows[1] as SettingsRow.Value).summary)
    }

    @Test
    fun `playlists sorting By name orders the list`() {
        val rows =
            playlistsRows(
                s,
                listOf(item.copy(url = "b", name = "Zeta"), item.copy(url = "a", name = "Alpha")),
            )
        assertEquals(listOf("Alpha", "Zeta"), rows.take(2).map { (it as SettingsRow.Value).title })
    }

    @Test
    fun `playlist detail matches capture 20 with every row unlocked`() {
        val rows = playlistDetailRows(s, item)
        assertEquals(
            listOf(
                "Enable playlist", "Playlist name", "Playlist URL", "EPG sources", "User-Agent",
                "Manage groups", "Update options", "Update interval, hours", "Update on app start",
                "Update playlist", "Delete playlist",
            ),
            titles(rows),
        )
        assertTrue((rows[0] as SettingsRow.Toggle).checked)
        assertEquals("1 source", (rows[3] as SettingsRow.Value).summary)
        // The reference sells these as premium; telly ships them unlocked
        // (charter precedent), so no detail row is locked at all.
        assertFalse((rows[2] as SettingsRow.Value).locked)
        assertFalse((rows[4] as SettingsRow.Value).locked)
        assertFalse((rows[5] as SettingsRow.Value).locked)
        assertFalse((rows[7] as SettingsRow.Value).locked)
        assertFalse((rows[8] as SettingsRow.Toggle).locked)
        assertFalse((rows[9] as SettingsRow.Action).locked)
        assertFalse((rows[10] as SettingsRow.Action).locked)
    }

    @Test
    fun `playlist detail summaries reflect the per-playlist settings`() {
        assertEquals("Not set", detailSummary(RowIds.PLAYLIST_USER_AGENT))
        assertEquals("None", detailSummary(RowIds.PLAYLIST_UPDATE_INTERVAL))
        assertFalse(detailToggle(RowIds.PLAYLIST_UPDATE_ON_START).checked)

        s.set(playlistUserAgentSetting(item.url), "telly-agent")
        s.set(playlistUpdateIntervalSetting(item.url), 8)
        s.set(playlistUpdateOnStartSetting(item.url), true)

        assertEquals("telly-agent", detailSummary(RowIds.PLAYLIST_USER_AGENT))
        assertEquals("8", detailSummary(RowIds.PLAYLIST_UPDATE_INTERVAL))
        assertTrue(detailToggle(RowIds.PLAYLIST_UPDATE_ON_START).checked)
    }

    @Test
    fun `manage groups pane lists one default-on toggle per group`() {
        val grouped = item.copy(groups = listOf("News", "Movies"))
        s.set(playlistGroupEnabledSetting(item.url, "Movies"), false)

        val rows = playlistGroupRows(s, grouped).filterIsInstance<SettingsRow.Toggle>()

        assertEquals(listOf("News", "Movies"), rows.map { it.title })
        assertEquals(listOf(true, false), rows.map { it.checked })
    }

    private fun detailSummary(rowId: String): String? =
        (playlistDetailRows(s, item).first { it.id == rowId } as SettingsRow.Value).summary

    private fun detailToggle(rowId: String): SettingsRow.Toggle =
        playlistDetailRows(s, item).first { it.id == rowId } as SettingsRow.Toggle

    @Test
    fun `epg pane matches capture 57 with the None default interval`() {
        val rows = epgRows(s)
        assertEquals(
            listOf(
                "EPG sources",
                "Past days to keep EPG",
                "Store program descriptions",
                "Update options",
                "Update interval, hours",
                "Update on app start",
                "Update on playlists change",
                "Update EPG",
            ),
            titles(rows),
        )
        assertEquals("7", (rows[1] as SettingsRow.Value).summary)
        assertTrue((rows[2] as SettingsRow.Toggle).checked)
        assertEquals("None", (rows[4] as SettingsRow.Value).summary)
    }

    @Test
    fun `epg interval summary reflects a changed setting`() {
        s.set(TellySettings.EPG_UPDATE_INTERVAL_HOURS, 6)
        val interval = epgRows(s).first { it.id == RowIds.EPG_UPDATE_INTERVAL } as SettingsRow.Value
        assertEquals("6", interval.summary)
    }

    @Test
    fun `epg sources pane lists the url-tvg source with the footer note`() {
        val rows = epgSourcesRows(listOf(item), emptyList())
        val source = rows[0] as SettingsRow.Value
        assertEquals("10.0.2.2 (default)", source.title)
        assertEquals("http://10.0.2.2:8090/epg.xml", source.summary)
        assertTrue(source.checkIcon)
        // Reference free tier locks Add source; telly ships it by directive.
        assertFalse((rows[1] as SettingsRow.Action).locked)
        assertEquals("Add source", (rows[1] as SettingsRow.Action).title)
        val note = rows[2] as SettingsRow.Note
        assertEquals("EPG sources should be assigned in the playlist settings", note.text)
        assertFalse(note.accent)
    }

    @Test
    fun `custom sources render after the default source, named by host`() {
        val custom = EpgSource(id = 7, playlistUrl = item.url, url = "http://guide.example:8080/tv.xml")
        val rows = epgSourcesRows(listOf(item), listOf(custom))
        assertEquals("10.0.2.2 (default)", (rows[0] as SettingsRow.Value).title)
        val customRow = rows[1] as SettingsRow.Value
        assertEquals(RowIds.EPG_CUSTOM_SOURCE_PREFIX + "7", customRow.id)
        assertEquals("guide.example", customRow.title)
        assertEquals("http://guide.example:8080/tv.xml", customRow.summary)
        assertTrue(customRow.checkIcon)
        assertEquals("Add source", (rows[2] as SettingsRow.Action).title)
    }

    @Test
    fun `a source detail pane offers url edit and delete`() {
        val custom = EpgSource(id = 7, playlistUrl = item.url, url = "http://guide.example/tv.xml")
        val rows = epgSourceDetailRows(custom)
        assertEquals(listOf("Source URL", "Delete source"), titles(rows))
        assertEquals("http://guide.example/tv.xml", (rows[0] as SettingsRow.Value).summary)
    }

    @Test
    fun `epg source count summary words counts like capture 20`() {
        assertEquals("No sources", epgSourceCountSummary(0))
        assertEquals("1 source", epgSourceCountSummary(1))
        assertEquals("2 sources", epgSourceCountSummary(2))
        assertEquals(
            "2 sources",
            (playlistDetailRows(s, item, customEpgSourceCount = 1)[3] as SettingsRow.Value).summary,
        )
    }

    @Test
    fun `epg source names fall back to the raw url`() {
        assertEquals("guide.example", EpgSource(1, "p", "https://guide.example/x").name)
        assertEquals("not a url", EpgSource(1, "p", "not a url").name)
    }

    @Test
    fun `appearance unlocks every row with the captured defaults`() {
        val rows = appearanceRows(s)
        assertEquals(
            listOf("TV guide", "Player", "Groups", "Logos", "Language", "Font size", "Color theme"),
            titles(rows),
        )
        rows.forEach { assertFalse(it.id, (it as SettingsRow.Value).locked) }
        assertEquals("Dark  •  Blue", (rows[6] as SettingsRow.Value).summary)
        assertEquals("System", (rows[4] as SettingsRow.Value).summary)
        assertEquals("Medium", (rows[5] as SettingsRow.Value).summary)
    }

    @Test
    fun `playback matches capture 60 with afr external and skip steps unlocked`() {
        val rows = playbackRows(s)
        assertEquals(
            listOf(
                "Buffer size",
                "Audio decoder",
                "Video decoder",
                "Auto frame rate (AFR)",
                "Select surround audio track by default",
                "Audio passthrough",
                "Use external player",
                "Skip steps",
            ),
            titles(rows),
        )
        assertEquals("Small", (rows[0] as SettingsRow.Value).summary)
        assertEquals("Hardware", (rows[1] as SettingsRow.Value).summary)
        assertEquals("Off", (rows[3] as SettingsRow.Value).summary)
        assertEquals("Off", (rows[6] as SettingsRow.Value).summary)
        assertEquals("10s / 30s / 1m / 5m", (rows[7] as SettingsRow.Value).summary)
        // telly has no premium tier: the reference's locked rows ship live.
        listOf(3, 6, 7).forEach { assertFalse((rows[it] as SettingsRow.Value).locked) }
    }

    @Test
    fun `playback extras summaries reflect changed settings`() {
        s.set(TellySettings.AUTO_FRAME_RATE, "On")
        s.set(TellySettings.USE_EXTERNAL_PLAYER, "On")
        s.set(TellySettings.SKIP_STEPS, "30s / 1m / 5m / 10m")
        val rows = playbackRows(s)
        assertEquals("On", (rows[3] as SettingsRow.Value).summary)
        assertEquals("On", (rows[6] as SettingsRow.Value).summary)
        assertEquals("30s / 1m / 5m / 10m", (rows[7] as SettingsRow.Value).summary)
    }

    @Test
    fun `remote control has both locked sub-screens and the six seek toggles`() {
        val rows = remoteControlRows(s)
        assertEquals(9, rows.size)
        assertEquals("Seeking options", (rows[2] as SettingsRow.Header).text)
        val toggles = rows.filterIsInstance<SettingsRow.Toggle>()
        assertEquals(6, toggles.size)
        assertTrue(toggles[0].checked)
        toggles.drop(1).forEach { assertFalse(it.checked) }
    }

    @Test
    fun `parental master toggle title is its state`() {
        assertEquals("Off", (parentalRows(s)[0] as SettingsRow.Toggle).title)
        s.set(TellySettings.PARENTAL_ENABLED, true)
        assertEquals("On", (parentalRows(s)[0] as SettingsRow.Toggle).title)
    }

    @Test
    fun `parental pane matches capture 67`() {
        val rows = parentalRows(s)
        assertEquals(
            listOf(
                "Off",
                "Change PIN",
                "PIN input method",
                "Don't require PIN after unlocking",
                "Don't require for channels only",
                "Require PIN for",
                "Settings",
                "Settings | Playlists",
            ),
            titles(rows),
        )
        assertEquals("Picker", (rows[2] as SettingsRow.Value).summary)
        assertEquals("Always require", (rows[3] as SettingsRow.Value).summary)
    }

    @Test
    fun `other pane keeps Reminders and VOD live with the rest locked and about matches capture 53`() {
        val other = otherRows()
        assertEquals(listOf("Search", "Reminders", "Recording", "VOD"), titles(other))
        // Reminders and VOD shipped as telly slices; the rest stay locked.
        assertEquals(listOf(true, false, true, false), other.map { (it as SettingsRow.Value).locked })

        val about = aboutRows(s, "0.1.0")
        assertEquals(listOf("Send anonymous statistics to improve the app", "Privacy policy", "Version"), titles(about))
        assertTrue((about[0] as SettingsRow.Toggle).checked)
        assertEquals("0.1.0", (about[2] as SettingsRow.Value).summary)
    }

    @Test
    fun `sections enumerate in the exact captured order`() {
        assertEquals(
            listOf(
                "General", "Playlists", "EPG", "Appearance", "Playback",
                "Remote control", "Parental controls", "Other", "About",
            ),
            SettingsSection.entries.map { it.title },
        )
    }
}
