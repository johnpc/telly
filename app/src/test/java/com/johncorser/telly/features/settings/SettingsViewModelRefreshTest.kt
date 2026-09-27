package com.johncorser.telly.features.settings

import com.johncorser.telly.core.settings.InMemoryKeyValueStore
import com.johncorser.telly.core.settings.ParentalControls
import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.features.epg.InMemoryEpgSourceStore
import com.johncorser.telly.features.playlist.InMemoryPlaylistRepository
import com.johncorser.telly.features.playlist.M3uChannel
import com.johncorser.telly.features.playlist.M3uPlaylist
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

/** The spinner + completion-message feedback of the manual update rows. */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelRefreshTest {
    private val settings = SettingsRepository(InMemoryKeyValueStore())
    private val playlists = InMemoryPlaylistRepository()

    private var gate: CompletableDeferred<Unit>? = null
    private var failFetches = setOf<String>()
    private val fetched = mutableListOf<String>()
    private var epgResult = 1

    private val body =
        """
        #EXTM3U
        #EXTINF:-1 tvg-id="one" group-title="News",News One
        http://s/1.ts
        """.trimIndent()

    private fun graph(): SettingsGraph =
        SettingsGraph(
            settings = settings,
            playlists = playlists,
            parental = ParentalControls(settings),
            stores = SettingsStores(epgSources = InMemoryEpgSourceStore()),
            actions =
                SettingsActions(
                    updater =
                        PlaylistUpdater(
                            fetchPlaylist = { url ->
                                gate?.await()
                                fetched += url
                                if (url in failFetches) throw IOException("boom")
                                body
                            },
                            repository = playlists,
                        ),
                    updateEpgNow = { epgResult },
                    backup = SettingsBackupManager(settings, playlists),
                ),
            versionName = "0.1.0",
        )

    private fun TestScope.model(): SettingsViewModel =
        graph().viewModel(
            scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            callbacks = SettingsCallbacks(),
        )

    private suspend fun seedPlaylist(url: String = "http://p/x.m3u") {
        playlists.add(
            url,
            M3uPlaylist(channels = listOf(M3uChannel(title = "One", streamUrl = "http://s/1.ts"))),
            name = "Ten",
        )
    }

    @Test
    fun `update playlist spins on its row then reports success and clears`() =
        runTest {
            seedPlaylist()
            val model = model()
            model.activate(RowIds.PLAYLIST_PREFIX + "http://p/x.m3u")
            gate = CompletableDeferred()

            model.activate(RowIds.PLAYLIST_UPDATE_NOW)

            assertEquals(RowIds.PLAYLIST_UPDATE_NOW, model.refreshState.value.busyRowId)
            val row = model.rows.value.first { it.id == RowIds.PLAYLIST_UPDATE_NOW }
            assertTrue((row as SettingsRow.Action).busy)

            gate!!.complete(Unit)
            assertEquals(RefreshStatus(message = "Playlist updated"), model.refreshState.value)

            advanceTimeBy(REFRESH_MESSAGE_MS + 1)
            assertEquals(RefreshStatus(), model.refreshState.value)
        }

    @Test
    fun `a failed fetch reports the failure instead of staying silent`() =
        runTest {
            seedPlaylist()
            failFetches = setOf("http://p/x.m3u")
            val model = model()
            model.activate(RowIds.PLAYLIST_PREFIX + "http://p/x.m3u")

            model.activate(RowIds.PLAYLIST_UPDATE_NOW)

            assertEquals("Playlist update failed", model.refreshState.value.message)
            assertNull(model.refreshState.value.busyRowId)
        }

    @Test
    fun `update all playlists reports the partial count`() =
        runTest {
            seedPlaylist("http://p/a.m3u")
            seedPlaylist("http://p/b.m3u")
            failFetches = setOf("http://p/b.m3u")
            val model = model()

            model.activate(RowIds.UPDATE_ALL_PLAYLISTS)

            assertEquals("Updated 1 of 2 playlists", model.refreshState.value.message)
        }

    @Test
    fun `update all playlists reports plain success when every fetch lands`() =
        runTest {
            seedPlaylist("http://p/a.m3u")
            seedPlaylist("http://p/b.m3u")
            val model = model()

            model.activate(RowIds.UPDATE_ALL_PLAYLISTS)

            assertEquals("Playlists updated", model.refreshState.value.message)
        }

    @Test
    fun `update epg reports success while sources refresh and failure when none do`() =
        runTest {
            val model = model()
            model.activate(RowIds.EPG_UPDATE_NOW)
            assertEquals("EPG updated", model.refreshState.value.message)

            advanceTimeBy(REFRESH_MESSAGE_MS + 1)
            epgResult = 0
            model.activate(RowIds.EPG_UPDATE_NOW)
            assertEquals("EPG update failed", model.refreshState.value.message)
        }

    @Test
    fun `activations while a refresh is in flight are ignored`() =
        runTest {
            seedPlaylist()
            val model = model()
            model.activate(RowIds.PLAYLIST_PREFIX + "http://p/x.m3u")
            gate = CompletableDeferred()

            model.activate(RowIds.PLAYLIST_UPDATE_NOW)
            model.activate(RowIds.PLAYLIST_UPDATE_NOW)
            model.activate(RowIds.UPDATE_ALL_PLAYLISTS)
            gate!!.complete(Unit)

            assertEquals(listOf("http://p/x.m3u"), fetched)
        }

    @Test
    fun `markBusy flags only the matching action row`() {
        val rows =
            listOf(
                SettingsRow.Header("Update options"),
                SettingsRow.Action(id = "a", title = "Update EPG"),
                SettingsRow.Action(id = "b", title = "Other"),
            )
        assertEquals(rows, rows.markBusy(null))
        val marked = rows.markBusy("a")
        assertTrue((marked[1] as SettingsRow.Action).busy)
        assertEquals(rows[0], marked[0])
        assertEquals(rows[2], marked[2])
    }
}
