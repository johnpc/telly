package com.johncorser.telly.features.settings

import com.johncorser.telly.core.settings.InMemoryKeyValueStore
import com.johncorser.telly.core.settings.ParentalControls
import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.core.settings.TellySettings
import com.johncorser.telly.features.epg.InMemoryEpgSourceStore
import com.johncorser.telly.features.playlist.InMemoryPlaylistRepository
import com.johncorser.telly.features.playlist.M3uChannel
import com.johncorser.telly.features.playlist.M3uPlaylist
import com.johncorser.telly.features.search.SearchHistory
import com.johncorser.telly.testutil.FakeChannelDao
import com.johncorser.telly.testutil.testChannel
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

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val store = InMemoryKeyValueStore()
    private val settings = SettingsRepository(store)
    private val playlists = InMemoryPlaylistRepository()
    private val epgSourceStore = InMemoryEpgSourceStore()
    private val fetched = mutableListOf<String>()
    private var epgUpdates = 0
    private val exports = mutableListOf<String>()
    private var importRequests = 0
    private var addPlaylistRoutes = 0

    private val body =
        """
        #EXTM3U url-tvg="http://e/epg.xml"
        #EXTINF:-1 tvg-id="one" group-title="News",News One
        http://s/1.ts
        """.trimIndent()

    private val channelDao = FakeChannelDao()
    private val historyStore = InMemoryKeyValueStore()
    private val searchHistory = SearchHistory(historyStore)

    private fun graph(): SettingsGraph =
        SettingsGraph(
            settings = settings,
            playlists = playlists,
            parental = ParentalControls(settings),
            stores =
                SettingsStores(
                    epgSources = epgSourceStore,
                    blocked = BlockedChannels(channelDao),
                    searchHistory = searchHistory,
                ),
            actions =
                SettingsActions(
                    updater =
                        PlaylistUpdater(
                            fetchPlaylist = {
                                fetched += it
                                body
                            },
                            repository = playlists,
                        ),
                    updateEpgNow = { epgUpdates++ },
                    backup = SettingsBackupManager(settings, playlists),
                ),
            versionName = "0.1.0",
        )

    private fun TestScope.model(): SettingsViewModel =
        graph().viewModel(
            scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            callbacks =
                SettingsCallbacks(
                    onAddPlaylist = { addPlaylistRoutes++ },
                    onExportBackup = { exports += it },
                ).apply { onImportBackup = { importRequests++ } },
        )

    private suspend fun seedPlaylist() {
        playlists.add(
            "http://p/x.m3u",
            M3uPlaylist(
                epgUrl = "http://e/epg.xml",
                channels = listOf(M3uChannel(title = "One", streamUrl = "http://s/1.ts")),
            ),
            name = "10.0.2.2",
        )
    }

    @Test
    fun `starts on the root sheet listing the captured sections`() =
        runTest {
            val model = model()
            assertNull(model.state.value.activePane)
            assertEquals("Settings", paneTitle(model.state.value.activePane, model.playlistItems.value))
            assertTrue(model.rows.value.any { it.id == RowIds.SECTION_PREFIX + SettingsSection.GENERAL.name })
        }

    @Test
    fun `OK on a section row pushes its sheet over the root`() =
        runTest {
            val model = model()
            model.activate(RowIds.SECTION_PREFIX + SettingsSection.EPG.name)
            assertTrue(model.rows.value.any { it.id == RowIds.EPG_UPDATE_NOW })
            assertEquals("EPG", paneTitle(model.state.value.activePane, model.playlistItems.value))
        }

    @Test
    fun `activating a toggle persists the flip to the store`() =
        runTest {
            val model = model()
            model.activate(RowIds.CONFIRM_EXIT)
            assertTrue(settings.get(TellySettings.CONFIRM_EXIT))
            assertTrue(SettingsRepository(store).get(TellySettings.CONFIRM_EXIT))
            model.activate(RowIds.CONFIRM_EXIT)
            assertFalse(settings.get(TellySettings.CONFIRM_EXIT))
        }

    @Test
    fun `activating a picker row opens the picker with the current value`() =
        runTest {
            val model = model()
            model.activate(RowIds.EPG_UPDATE_INTERVAL)
            val picker = model.state.value.overlay as SettingsOverlay.Picker
            assertEquals("Update interval, hours", picker.spec.title)
            assertEquals("0", picker.current)
        }

    @Test
    fun `choosing a picker option writes it and closes the picker`() =
        runTest {
            val model = model()
            model.activate(RowIds.EPG_UPDATE_INTERVAL)
            model.choosePickerOption("6")
            assertNull(model.state.value.overlay)
            assertEquals(6, settings.get(TellySettings.EPG_UPDATE_INTERVAL_HOURS))
        }

    @Test
    fun `add playlist always routes into the wizard`() =
        runTest {
            // telly has no premium tier: the reference gated a second
            // playlist behind Unlock Premium, telly just adds it.
            val model = model()
            model.activate(RowIds.ADD_PLAYLIST)
            assertEquals(1, addPlaylistRoutes)
            assertNull(model.state.value.overlay)

            seedPlaylist()
            model.activate(RowIds.ADD_PLAYLIST)
            assertEquals(2, addPlaylistRoutes)
            assertNull(model.state.value.overlay)
        }

    @Test
    fun `activating the playlist row pushes its detail pane`() =
        runTest {
            seedPlaylist()
            val model = model()
            model.activate(RowIds.PLAYLIST_PREFIX + "http://p/x.m3u")
            assertEquals(SettingsPane.PlaylistDetail("http://p/x.m3u"), model.state.value.activePane)
            assertEquals("10.0.2.2", paneTitle(model.state.value.activePane, model.playlistItems.value))
            assertTrue(model.rows.value.any { it.id == RowIds.PLAYLIST_DELETE })
        }

    @Test
    fun `rename flows through the text edit overlay and persists`() =
        runTest {
            seedPlaylist()
            val model = model()
            model.activate(RowIds.PLAYLIST_PREFIX + "http://p/x.m3u")
            model.activate(RowIds.PLAYLIST_NAME)
            assertEquals("10.0.2.2", (model.state.value.overlay as SettingsOverlay.TextEdit).value)

            model.submitText("Living room")

            assertNull(model.state.value.overlay)
            assertEquals("Living room", playlists.playlists.value.single().name)
        }

    @Test
    fun `delete asks for the captured confirm then removes the playlist`() =
        runTest {
            seedPlaylist()
            val model = model()
            model.activate(RowIds.PLAYLIST_PREFIX + "http://p/x.m3u")
            model.activate(RowIds.PLAYLIST_DELETE)
            val confirm = model.state.value.overlay as SettingsOverlay.ConfirmDelete
            assertEquals("10.0.2.2", confirm.name)

            model.confirmDelete()

            assertTrue(playlists.playlists.value.isEmpty())
            assertNull(model.state.value.activePane)
        }

    @Test
    fun `update playlist re-runs the fetch path for the open playlist`() =
        runTest {
            seedPlaylist()
            val model = model()
            model.activate(RowIds.PLAYLIST_PREFIX + "http://p/x.m3u")
            model.activate(RowIds.PLAYLIST_UPDATE_NOW)
            assertEquals(listOf("http://p/x.m3u"), fetched)
        }

    @Test
    fun `update all playlists hits every stored url`() =
        runTest {
            seedPlaylist()
            val model = model()
            model.activate(RowIds.UPDATE_ALL_PLAYLISTS)
            assertEquals(listOf("http://p/x.m3u"), fetched)
        }

    @Test
    fun `update epg action reaches the refresher`() =
        runTest {
            val model = model()
            model.activate(RowIds.EPG_UPDATE_NOW)
            assertEquals(1, epgUpdates)
        }

    @Test
    fun `epg sources pane opens from both entry rows`() =
        runTest {
            seedPlaylist()
            val model = model()
            model.activate(RowIds.EPG_SOURCES)
            assertEquals(SettingsPane.EpgSources, model.state.value.activePane)
            assertTrue(model.rows.value.any { it.id == RowIds.EPG_SOURCE_PREFIX + "http://p/x.m3u" })
        }

    @Test
    fun `add source commits a valid url against the first playlist`() =
        runTest {
            seedPlaylist()
            val model = model()
            model.activate(RowIds.EPG_SOURCES)
            model.activate(RowIds.EPG_ADD_SOURCE)
            assertEquals("EPG URL", (model.state.value.overlay as SettingsOverlay.TextEdit).title)

            model.submitText("http://e/extra.xml")

            assertNull(model.state.value.overlay)
            val source = model.epgSourceItems.value.single()
            assertEquals("http://e/extra.xml", source.url)
            assertEquals("http://p/x.m3u", source.playlistUrl)
            assertTrue(model.rows.value.any { it.id == RowIds.EPG_CUSTOM_SOURCE_PREFIX + source.id })
        }

    @Test
    fun `add source from a playlist detail pane attaches to that playlist`() =
        runTest {
            seedPlaylist()
            playlists.add("http://p/y.m3u", M3uPlaylist(channels = emptyList()), name = "Second")
            val model = model()
            model.activate(RowIds.PLAYLIST_PREFIX + "http://p/y.m3u")
            model.activate(RowIds.PLAYLIST_EPG_SOURCES)
            model.activate(RowIds.EPG_ADD_SOURCE)

            model.submitText("http://e/extra.xml")

            assertEquals("http://p/y.m3u", model.epgSourceItems.value.single().playlistUrl)
        }

    @Test
    fun `invalid or blank source urls are ignored on commit`() =
        runTest {
            seedPlaylist()
            val model = model()
            model.activate(RowIds.EPG_SOURCES)
            model.activate(RowIds.EPG_ADD_SOURCE)
            model.submitText("not-a-url")
            model.activate(RowIds.EPG_ADD_SOURCE)
            model.submitText("   ")
            assertTrue(model.epgSourceItems.value.isEmpty())
        }

    @Test
    fun `a custom source row opens its pane where the url can be edited`() =
        runTest {
            seedPlaylist()
            epgSourceStore.add("http://p/x.m3u", "http://e/extra.xml")
            val model = model()
            val source = model.epgSourceItems.value.single()

            model.activate(RowIds.EPG_CUSTOM_SOURCE_PREFIX + source.id)
            assertEquals(SettingsPane.EpgSourceDetail(source.id), model.state.value.activePane)
            assertEquals(
                "e",
                paneTitle(model.state.value.activePane, model.playlistItems.value, model.epgSourceItems.value),
            )
            assertTrue(model.rows.value.any { it.id == RowIds.EPG_SOURCE_DELETE })

            model.activate(RowIds.EPG_SOURCE_URL)
            assertEquals("http://e/extra.xml", (model.state.value.overlay as SettingsOverlay.TextEdit).value)
            model.submitText("http://e/other.xml")
            assertEquals("http://e/other.xml", model.epgSourceItems.value.single().url)
        }

    @Test
    fun `delete source confirms then removes it and pops its pane`() =
        runTest {
            seedPlaylist()
            epgSourceStore.add("http://p/x.m3u", "http://e/extra.xml")
            val model = model()
            val source = model.epgSourceItems.value.single()
            model.activate(RowIds.EPG_SOURCES)
            model.activate(RowIds.EPG_CUSTOM_SOURCE_PREFIX + source.id)
            model.activate(RowIds.EPG_SOURCE_DELETE)
            assertEquals("e", (model.state.value.overlay as SettingsOverlay.ConfirmDeleteSource).name)

            model.confirmDeleteEpgSource()

            assertTrue(model.epgSourceItems.value.isEmpty())
            assertEquals(SettingsPane.EpgSources, model.state.value.activePane)
            assertNull(model.state.value.overlay)
        }

    @Test
    fun `source handlers ignore calls without an open source pane`() =
        runTest {
            seedPlaylist()
            val model = model()
            model.activate(RowIds.EPG_SOURCE_URL)
            model.activate(RowIds.EPG_SOURCE_DELETE)
            model.confirmDeleteEpgSource()
            assertNull(model.state.value.overlay)
        }

    @Test
    fun `back pops overlay first then one sheet at a time then leaves`() =
        runTest {
            seedPlaylist()
            val model = model()
            model.selectSection(SettingsSection.PLAYLISTS)
            model.activate(RowIds.PLAYLIST_PREFIX + "http://p/x.m3u")
            model.activate(RowIds.PLAYLIST_DELETE)
            assertTrue(model.state.value.consumesBack)

            assertTrue(model.back())
            assertNull(model.state.value.overlay)
            assertTrue(model.back())
            assertEquals(SettingsPane.Section(SettingsSection.PLAYLISTS), model.state.value.activePane)
            assertTrue(model.back())
            assertNull(model.state.value.activePane)
            assertFalse(model.back())
        }

    @Test
    fun `enabling parental controls without a pin forces pin setup`() =
        runTest {
            val model = model()
            model.activate(RowIds.PARENTAL_MASTER)
            assertTrue(settings.get(TellySettings.PARENTAL_ENABLED))
            assertEquals(SettingsOverlay.PinSetup, model.state.value.overlay)

            model.submitPin("2468")
            assertNull(model.state.value.overlay)
            assertTrue(ParentalControls(settings).verifyPin("2468"))

            model.activate(RowIds.PARENTAL_MASTER)
            assertFalse(settings.get(TellySettings.PARENTAL_ENABLED))
        }

    @Test
    fun `a locked group requires the pin through the gate`() =
        runTest {
            val model = model()
            model.activate(RowIds.PARENTAL_MASTER)
            model.submitPin("1234")
            val gate = ParentalControls(settings)
            gate.setGroupLocked("Movies", locked = true)
            assertTrue(gate.isGroupLocked("Movies"))
            assertFalse(gate.isGroupLocked("News"))
            assertTrue(gate.verifyPin("1234"))
        }

    @Test
    fun `back up data exports json and restore requests the picker`() =
        runTest {
            val model = model()
            settings.set(TellySettings.EPG_PAST_DAYS_TO_KEEP, 2)
            model.activate(RowIds.BACK_UP_DATA)
            assertEquals(1, exports.size)
            assertTrue(exports.single().contains("epg_past_days_to_keep"))

            model.activate(RowIds.RESTORE_DATA)
            assertEquals(1, importRequests)
        }

    @Test
    fun `importBackup applies a previously exported file`() =
        runTest {
            val model = model()
            settings.set(TellySettings.EPG_PAST_DAYS_TO_KEEP, 2)
            model.activate(RowIds.BACK_UP_DATA)
            settings.set(TellySettings.EPG_PAST_DAYS_TO_KEEP, 5)

            model.importBackup(exports.single())

            assertEquals(2, settings.get(TellySettings.EPG_PAST_DAYS_TO_KEEP))
        }

    @Test
    fun `user agent edits persist through the text overlay`() =
        runTest {
            val model = model()
            model.activate(RowIds.USER_AGENT)
            model.submitText("telly/1.0")
            assertEquals("telly/1.0", settings.get(TellySettings.USER_AGENT))
            model.activate(RowIds.UDP_PROXY)
            model.submitText("10.0.0.1:1234")
            assertEquals("10.0.0.1:1234", settings.get(TellySettings.UDP_PROXY))
        }

    @Test
    fun `enable playlist toggle flips its per-url setting`() =
        runTest {
            seedPlaylist()
            val model = model()
            model.activate(RowIds.PLAYLIST_PREFIX + "http://p/x.m3u")
            model.activate(RowIds.PLAYLIST_ENABLE)
            assertFalse(settings.get(playlistEnabledSetting("http://p/x.m3u")))
        }

    @Test
    fun `overlay handlers ignore calls when nothing is open`() =
        runTest {
            val model = model()
            model.choosePickerOption("6")
            model.submitText("x")
            model.confirmDelete()
            assertEquals(0, settings.get(TellySettings.EPG_UPDATE_INTERVAL_HOURS))
            assertNull(model.state.value.overlay)
        }

    @Test
    fun `blocked channels row pushes straight through while no pin exists`() =
        runTest {
            val model = model()
            model.selectSection(SettingsSection.PARENTAL_CONTROLS)
            model.activate(RowIds.PARENTAL_BLOCKED_CHANNELS)
            assertEquals(SettingsPane.BlockedChannels, model.state.value.activePane)
        }

    @Test
    fun `blocked channels pane entry is pin-gated once and wrong pins keep prompting`() =
        runTest {
            val base = testChannel(7, 1, "Sports Arena")
            channelDao.channels.value = listOf(base.copy(flags = base.flags.copy(blocked = true)))
            val model = model()
            model.activate(RowIds.PARENTAL_MASTER)
            model.submitPin("2468")

            model.activate(RowIds.PARENTAL_BLOCKED_CHANNELS)
            assertEquals(SettingsOverlay.PinVerify, model.state.value.overlay)

            model.submitVerifyPin("1111")
            assertEquals(SettingsOverlay.PinVerify, model.state.value.overlay)

            model.submitVerifyPin("2468")
            assertNull(model.state.value.overlay)
            assertEquals(SettingsPane.BlockedChannels, model.state.value.activePane)
            assertTrue(model.rows.value.any { it.id == RowIds.BLOCKED_CHANNEL_PREFIX + "7" })

            // OK on the listed channel unblocks it without another prompt.
            model.activate(RowIds.BLOCKED_CHANNEL_PREFIX + "7")
            assertFalse(channelDao.channels.value.single().flags.blocked)
        }

    @Test
    fun `unblock ignores unknown or malformed row ids`() =
        runTest {
            val model = model()
            model.activate(RowIds.BLOCKED_CHANNEL_PREFIX + "notanid")
            model.activate(RowIds.BLOCKED_CHANNEL_PREFIX + "99")
            assertNull(model.state.value.overlay)
        }

    @Test
    fun `other search pane offers the toggle and the confirmed clear`() =
        runTest {
            searchHistory.record("news")
            val model = model()
            model.selectSection(SettingsSection.OTHER)
            model.activate(RowIds.OTHER_SEARCH)
            assertEquals(SettingsPane.OtherSearch, model.state.value.activePane)
            assertEquals("Search", paneTitle(model.state.value.activePane, model.playlistItems.value))

            model.activate(RowIds.SEARCH_SAVE_HISTORY)
            assertFalse(settings.get(TellySettings.SEARCH_SAVE_HISTORY))

            model.activate(RowIds.SEARCH_CLEAR_HISTORY)
            assertEquals(SettingsOverlay.ConfirmClearHistory, model.state.value.overlay)
            model.confirmClearHistory()
            assertNull(model.state.value.overlay)
            assertTrue(searchHistory.list().isEmpty())
        }

    @Test
    fun `clear history handlers ignore calls without the confirm open`() =
        runTest {
            searchHistory.record("news")
            val model = model()
            model.confirmClearHistory()
            model.submitVerifyPin("2468")
            assertEquals(listOf("news"), searchHistory.list())
            assertNull(model.state.value.activePane)
        }

    @Test
    fun `graph builds a working view model`() =
        runTest {
            val graph = graph()
            val model = graph.viewModel(CoroutineScope(UnconfinedTestDispatcher(testScheduler)), SettingsCallbacks())
            assertNull(model.state.value.activePane)
            assertEquals("0.1.0", graph.versionName)
        }
}
