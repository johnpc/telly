package com.johncorser.telly.features.settings

import com.johncorser.telly.core.settings.InMemoryKeyValueStore
import com.johncorser.telly.core.settings.ParentalControls
import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.features.epg.InMemoryEpgSourceStore
import com.johncorser.telly.features.playlist.InMemoryPlaylistRepository
import com.johncorser.telly.features.playlist.M3uChannel
import com.johncorser.telly.features.playlist.M3uPlaylist
import com.johncorser.telly.features.playlist.PlaylistUrlChanger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

/** The playlist-extras rows: URL edit, UA, update options, Manage groups. */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelPlaylistExtrasTest {
    private val url = "http://p/x.m3u"
    private val settings = SettingsRepository(InMemoryKeyValueStore())
    private val playlists = InMemoryPlaylistRepository()
    private val epgSourceStore = InMemoryEpgSourceStore()
    private var fetchFails = false

    private val body =
        """
        #EXTM3U url-tvg="http://e/epg.xml"
        #EXTINF:-1 tvg-id="one" group-title="News",News One
        http://s/1.ts
        """.trimIndent()

    private val fetch: suspend (String) -> String = {
        if (fetchFails) throw IOException("down") else body
    }

    private fun graph(): SettingsGraph =
        SettingsGraph(
            settings = settings,
            playlists = playlists,
            parental = ParentalControls(settings),
            actions =
                SettingsActions(
                    updater = PlaylistUpdater(fetch, playlists),
                    updateEpgNow = {},
                    backup = SettingsBackupManager(settings, playlists),
                    changePlaylistUrl =
                        PlaylistUrlChanger(
                            fetchPlaylist = fetch,
                            repository = playlists,
                            rekeySettings = { old, new -> PlaylistKeyMigration.apply(settings, old, new) },
                            rekeyEpgSources = epgSourceStore::rekeyPlaylist,
                        )::change,
                ),
            versionName = "0.1.0",
            stores = SettingsStores(epgSources = epgSourceStore),
        )

    private suspend fun TestScope.openDetail(): SettingsViewModel {
        playlists.add(
            url,
            M3uPlaylist(
                channels =
                    listOf(
                        M3uChannel(title = "News One", streamUrl = "http://s/1.ts", groupTitle = "News"),
                        M3uChannel(title = "Movie House", streamUrl = "http://s/2.ts", groupTitle = "Movies"),
                    ),
            ),
            name = "Living room",
        )
        val model =
            graph().viewModel(
                scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
                callbacks = SettingsCallbacks(),
            )
        model.activate(RowIds.PLAYLIST_PREFIX + url)
        return model
    }

    @Test
    fun `the five premium rows render unlocked`() =
        runTest {
            val unlockedIds = openDetail().rows.value.filter { !lockedOf(it) }.map { it.id }
            listOf(
                RowIds.PLAYLIST_URL,
                RowIds.PLAYLIST_USER_AGENT,
                RowIds.PLAYLIST_MANAGE_GROUPS,
                RowIds.PLAYLIST_UPDATE_INTERVAL,
                RowIds.PLAYLIST_UPDATE_ON_START,
            ).forEach { id -> assertTrue("$id unlocked", id in unlockedIds) }
        }

    @Test
    fun `the url row opens a text edit prefilled with the current url`() =
        runTest {
            val model = openDetail()
            model.activate(RowIds.PLAYLIST_URL)
            val edit = model.state.value.overlay as SettingsOverlay.TextEdit
            assertEquals("Playlist URL", edit.title)
            assertEquals(url, edit.value)
        }

    @Test
    fun `committing a new url re-keys the playlist and retargets the pane`() =
        runTest {
            settings.set(playlistUserAgentSetting(url), "agent")
            epgSourceStore.add(url, "http://e/custom.xml")
            val model = openDetail()
            model.activate(RowIds.PLAYLIST_URL)

            model.submitText("http://p/y.m3u")

            assertEquals("http://p/y.m3u", playlists.playlists.value.single().sourceUrl)
            assertEquals("Living room", playlists.playlists.value.single().name)
            assertEquals(SettingsPane.PlaylistDetail("http://p/y.m3u"), model.state.value.activePane)
            assertEquals("agent", settings.get(playlistUserAgentSetting("http://p/y.m3u")))
            assertEquals("http://p/y.m3u", epgSourceStore.sources.value.single().playlistUrl)
            // The commit triggered the update fetch: the new body is imported.
            assertEquals(listOf("News One"), playlists.playlists.value.single().playlist.channels.map { it.title })
        }

    @Test
    fun `a failing fetch keeps the old url and the pane`() =
        runTest {
            fetchFails = true
            val model = openDetail()
            model.activate(RowIds.PLAYLIST_URL)

            model.submitText("http://p/y.m3u")

            assertEquals(url, playlists.playlists.value.single().sourceUrl)
            assertEquals(SettingsPane.PlaylistDetail(url), model.state.value.activePane)
        }

    @Test
    fun `a blank or unchanged url is a no-op`() =
        runTest {
            val model = openDetail()
            model.activate(RowIds.PLAYLIST_URL)
            model.submitText("  ")
            model.activate(RowIds.PLAYLIST_URL)
            model.submitText(url)
            assertEquals(url, playlists.playlists.value.single().sourceUrl)
        }

    @Test
    fun `the user-agent row edits and persists the per-playlist setting`() =
        runTest {
            val model = openDetail()
            model.activate(RowIds.PLAYLIST_USER_AGENT)
            assertEquals("", (model.state.value.overlay as SettingsOverlay.TextEdit).value)

            model.submitText(" telly-agent ")

            assertNull(model.state.value.overlay)
            assertEquals("telly-agent", settings.get(playlistUserAgentSetting(url)))
            val row = model.rows.value.first { it.id == RowIds.PLAYLIST_USER_AGENT } as SettingsRow.Value
            assertEquals("telly-agent", row.summary)
        }

    @Test
    fun `the interval picker offers the playlist choices and persists`() =
        runTest {
            val model = openDetail()
            model.activate(RowIds.PLAYLIST_UPDATE_INTERVAL)
            val picker = model.state.value.overlay as SettingsOverlay.Picker
            assertEquals(listOf("None", "1", "2", "4", "8", "12", "24"), picker.spec.options.map { it.label })
            assertEquals("0", picker.current)

            model.choosePickerOption("8")

            assertEquals(8, settings.get(playlistUpdateIntervalSetting(url)))
            val row = model.rows.value.first { it.id == RowIds.PLAYLIST_UPDATE_INTERVAL } as SettingsRow.Value
            assertEquals("8", row.summary)
        }

    @Test
    fun `update on app start toggles the per-playlist flag`() =
        runTest {
            val model = openDetail()
            model.activate(RowIds.PLAYLIST_UPDATE_ON_START)
            assertTrue(settings.get(playlistUpdateOnStartSetting(url)))
            model.activate(RowIds.PLAYLIST_UPDATE_ON_START)
            assertFalse(settings.get(playlistUpdateOnStartSetting(url)))
        }

    @Test
    fun `manage groups lists the playlist's groups as on-by-default toggles`() =
        runTest {
            val model = openDetail()
            model.activate(RowIds.PLAYLIST_MANAGE_GROUPS)

            assertEquals(SettingsPane.PlaylistGroups(url), model.state.value.activePane)
            assertEquals("Manage groups", paneTitle(model.state.value.activePane, model.playlistItems.value))
            val toggles = model.rows.value.filterIsInstance<SettingsRow.Toggle>()
            assertEquals(listOf("News", "Movies"), toggles.map { it.title })
            assertTrue(toggles.all { it.checked })
        }

    @Test
    fun `toggling a group flips its enabled setting and back pops the pane`() =
        runTest {
            val model = openDetail()
            model.activate(RowIds.PLAYLIST_MANAGE_GROUPS)

            model.activate(RowIds.PLAYLIST_GROUP_PREFIX + "Movies")

            assertFalse(settings.get(playlistGroupEnabledSetting(url, "Movies")))
            assertTrue(settings.get(playlistGroupEnabledSetting(url, "News")))
            assertTrue(model.back())
            assertEquals(SettingsPane.PlaylistDetail(url), model.state.value.activePane)
        }

    private fun lockedOf(row: SettingsRow): Boolean =
        when (row) {
            is SettingsRow.Toggle -> row.locked
            is SettingsRow.Value -> row.locked
            is SettingsRow.Action -> row.locked
            else -> false
        }
}
