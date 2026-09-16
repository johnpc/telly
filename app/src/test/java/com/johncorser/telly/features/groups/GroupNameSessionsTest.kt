package com.johncorser.telly.features.groups

import com.johncorser.telly.features.settings.SettingsRow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Create group + Group options: the two name-centric tool screens. */
@OptIn(ExperimentalCoroutinesApi::class)
class GroupNameSessionsTest {
    private val store = InMemoryCustomGroupStore()
    private var done = 0

    private fun TestScope.scope(): CoroutineScope =
        CoroutineScope(
            SupervisorJob() + UnconfinedTestDispatcher(testScheduler),
        )

    @Test
    fun `create group shows a name editor and commits an empty group`() {
        runTest {
            val session = CreateGroupSession(store, { done += 1 }, scope())
            assertEquals("Create group", session.ui.value.title)
            assertEquals("", session.ui.value.textInitial)

            session.submitText("  My Picks  ")

            assertEquals(listOf("My Picks"), store.groups.value.map { it.name })
            assertTrue(store.groups.value.single().members.isEmpty())
            assertEquals(1, done)
        }
    }

    @Test
    fun `a blank name does not create a group`() {
        runTest {
            val session = CreateGroupSession(store, { done += 1 }, scope())

            session.submitText("   ")

            assertTrue(store.groups.value.isEmpty())
            assertEquals(0, done)
        }
    }

    @Test
    fun `group options on a playlist group locks rename and delete`() {
        runTest {
            val session =
                GroupOptionsSession("News", custom = null, store = store, onDone = { done += 1 }, scope = scope())

            val ui = session.ui.value
            assertEquals("News", ui.title)
            assertTrue(ui.rows.filterIsInstance<SettingsRow.Action>().all { it.locked })
            assertTrue(ui.rows.last() is SettingsRow.Note)
            assertNull(ui.focusId)

            session.submitText("Renamed")
            assertEquals(0, done)
        }
    }

    @Test
    fun `rename swaps to the editor and persists the new name`() {
        runTest {
            val custom = seed("My Picks")
            val session = GroupOptionsSession("My Picks", custom, store, { done += 1 }, scope())
            assertEquals("rename", session.ui.value.focusId)

            session.activate("rename")
            assertEquals("Rename group", session.ui.value.title)
            assertEquals("My Picks", session.ui.value.textInitial)

            session.submitText("Legends")
            assertEquals(listOf("Legends"), store.groups.value.map { it.name })
            assertEquals(1, done)
        }
    }

    @Test
    fun `delete asks for confirmation and cancel returns to the menu`() {
        runTest {
            val custom = seed("My Picks")
            val session = GroupOptionsSession("My Picks", custom, store, { done += 1 }, scope())

            session.activate("delete")
            assertEquals("Delete My Picks?", session.ui.value.title)

            session.activate("cancel")
            assertEquals("My Picks", session.ui.value.title)
            assertEquals(1, store.groups.value.size)

            session.activate("delete")
            session.activate("confirm")
            assertTrue(store.groups.value.isEmpty())
            assertEquals(1, done)
        }
    }

    private suspend fun seed(name: String): CustomGroup {
        store.create(name)
        return store.groups.value.single()
    }
}
