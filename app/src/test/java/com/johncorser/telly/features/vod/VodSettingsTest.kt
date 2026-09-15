package com.johncorser.telly.features.vod

import com.johncorser.telly.core.settings.InMemoryKeyValueStore
import com.johncorser.telly.core.settings.ParentalControls
import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.core.settings.TellySettings
import com.johncorser.telly.features.epg.InMemoryEpgSourceStore
import com.johncorser.telly.features.playlist.InMemoryPlaylistRepository
import com.johncorser.telly.features.settings.PlaylistUpdater
import com.johncorser.telly.features.settings.RowIds
import com.johncorser.telly.features.settings.SettingsActions
import com.johncorser.telly.features.settings.SettingsBackupManager
import com.johncorser.telly.features.settings.SettingsCallbacks
import com.johncorser.telly.features.settings.SettingsGraph
import com.johncorser.telly.features.settings.SettingsOverlay
import com.johncorser.telly.features.settings.SettingsPane
import com.johncorser.telly.features.settings.SettingsRow
import com.johncorser.telly.features.settings.SettingsViewModel
import com.johncorser.telly.features.settings.paneTitle
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

/** Settings -> Other -> VOD wiring: rows, pane push, toggle and clear. */
@OptIn(ExperimentalCoroutinesApi::class)
class VodSettingsTest {
    private val settings = SettingsRepository(InMemoryKeyValueStore())
    private var cleared = 0

    private fun graph(): SettingsGraph {
        val playlists = InMemoryPlaylistRepository()
        return SettingsGraph(
            settings = settings,
            playlists = playlists,
            parental = ParentalControls(settings),
            actions =
                SettingsActions(
                    updater = PlaylistUpdater(fetchPlaylist = { "" }, repository = playlists),
                    updateEpgNow = {},
                    backup = SettingsBackupManager(settings, playlists),
                    clearVodPositions = { cleared++ },
                ),
            versionName = "0.1.0",
            epgSources = InMemoryEpgSourceStore(),
        )
    }

    private fun TestScope.model(): SettingsViewModel =
        graph().viewModel(
            scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            callbacks = SettingsCallbacks(onAddPlaylist = {}, onExportBackup = {}),
        )

    @Test
    fun `the pane offers the remember toggle (default on) and the clear action`() {
        val rows = vodSettingsRows(settings)

        assertEquals(listOf(RowIds.VOD_REMEMBER_POSITION, RowIds.VOD_CLEAR_POSITIONS), rows.map { it.id })
        assertTrue((rows[0] as SettingsRow.Toggle).checked)
        assertEquals("Remember playback position", (rows[0] as SettingsRow.Toggle).title)
        assertEquals("Clear playback positions", (rows[1] as SettingsRow.Action).title)
    }

    @Test
    fun `the VOD row pushes the pane titled VOD`() =
        runTest {
            val model = model()

            model.activate(RowIds.OTHER_VOD)

            assertEquals(SettingsPane.Vod, model.state.value.activePane)
            assertEquals("VOD", paneTitle(SettingsPane.Vod, emptyList()))
            assertEquals(
                listOf(RowIds.VOD_REMEMBER_POSITION, RowIds.VOD_CLEAR_POSITIONS),
                model.rows.value.map { it.id },
            )
        }

    @Test
    fun `the remember toggle flips the persisted setting`() =
        runTest {
            val model = model()

            model.activate(RowIds.VOD_REMEMBER_POSITION)

            assertFalse(settings.get(TellySettings.VOD_REMEMBER_POSITION))
        }

    @Test
    fun `clear asks for confirmation, then runs the action once confirmed`() =
        runTest {
            val model = model()

            model.activate(RowIds.VOD_CLEAR_POSITIONS)
            assertEquals(SettingsOverlay.ConfirmClearVodPositions, model.state.value.overlay)
            assertEquals(0, cleared)

            model.confirmClearVodPositions()

            assertEquals(1, cleared)
            assertNull(model.state.value.overlay)
        }
}
