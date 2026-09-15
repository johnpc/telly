package com.johncorser.telly.features.reminders

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
import com.johncorser.telly.features.settings.SettingsViewModel
import com.johncorser.telly.features.settings.choosePickerOption
import com.johncorser.telly.testutil.testChannel
import com.johncorser.telly.testutil.testProgram
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

/** The settings pane behind Other -> Reminders: push, picker, delete confirm. */
@OptIn(ExperimentalCoroutinesApi::class)
class ReminderSettingsActionsTest {
    private val settings = SettingsRepository(InMemoryKeyValueStore())
    private val playlists = InMemoryPlaylistRepository()
    private val dao = FakeReminderDao()
    private val store = ReminderStore(dao)
    private val feed =
        ReminderSettingsFeed(
            store,
            MutableStateFlow(listOf(testChannel(7, 1, "News One"))),
            TimeZone.getTimeZone("UTC"),
        )

    private fun graph(): SettingsGraph =
        SettingsGraph(
            settings = settings,
            playlists = playlists,
            parental = ParentalControls(settings),
            actions =
                SettingsActions(
                    updater = PlaylistUpdater(fetchPlaylist = { "" }, repository = playlists),
                    updateEpgNow = {},
                    backup = SettingsBackupManager(settings, playlists),
                ),
            versionName = "0.1.0",
            epgSources = InMemoryEpgSourceStore(),
        ).apply { reminders = feed }

    private fun TestScope.model(): SettingsViewModel =
        graph().viewModel(
            scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            callbacks = SettingsCallbacks(),
        )

    @Test
    fun `the unlocked Reminders row pushes its pane`() =
        runTest {
            val model = model()
            model.activate(RowIds.OTHER_REMINDERS)
            assertEquals(SettingsPane.Reminders, model.state.value.activePane)
            assertTrue(model.rows.value.any { it.id == RowIds.REMINDERS_LEAD })
        }

    @Test
    fun `the lead row opens its picker and persists the choice`() =
        runTest {
            val model = model()
            model.activate(RowIds.OTHER_REMINDERS)
            model.activate(RowIds.REMINDERS_LEAD)
            val picker = model.state.value.overlay as SettingsOverlay.Picker
            assertEquals("Show reminder before, min", picker.spec.title)
            assertEquals("5", picker.current)

            model.choosePickerOption("10")
            assertEquals(10, settings.get(TellySettings.REMINDER_LEAD_MINUTES))
        }

    @Test
    fun `OK on a scheduled reminder confirms then deletes it`() =
        runTest {
            store.toggle(7L, testProgram("tvg-7", 10_000L, 40_000L, "Morning Report"))
            val model = model()
            model.activate(RowIds.OTHER_REMINDERS)
            val row = model.reminderItems.value.single()

            model.activate(RowIds.REMINDER_PREFIX + row.id)
            val confirm = model.state.value.overlay as SettingsOverlay.ConfirmDeleteReminder
            assertEquals("Morning Report", confirm.title)

            model.confirmDeleteReminder()

            assertNull(model.state.value.overlay)
            assertTrue(model.reminderItems.value.isEmpty())
        }

    @Test
    fun `reminder handlers ignore stale or malformed activations`() =
        runTest {
            val model = model()
            model.activate(RowIds.REMINDER_PREFIX + "not-a-number")
            model.activate(RowIds.REMINDER_PREFIX + "99")
            model.confirmDeleteReminder()
            assertNull(model.state.value.overlay)
        }
}
