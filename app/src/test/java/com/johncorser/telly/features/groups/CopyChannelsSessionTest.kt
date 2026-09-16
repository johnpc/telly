package com.johncorser.telly.features.groups

import com.johncorser.telly.features.settings.SettingsRow
import com.johncorser.telly.testutil.testChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The Copy channels flow: target pick, multi-select, explicit Done. */
@OptIn(ExperimentalCoroutinesApi::class)
class CopyChannelsSessionTest {
    private val store = InMemoryCustomGroupStore()
    private val channels =
        MutableStateFlow(
            listOf(
                testChannel(1, 1, "News One"),
                testChannel(2, 2, "News Two"),
                testChannel(3, 3, "Ghost").let { it.copy(flags = it.flags.copy(hidden = true)) },
            ),
        )
    private var done = 0

    private fun TestScope.build(groups: List<CustomGroup>): CopyChannelsSession =
        CopyChannelsSession(
            groups = groups,
            channels = channels,
            store = store,
            onDone = { done += 1 },
            scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler)),
        )

    @Test
    fun `without custom groups the screen only points at Create group`() {
        runTest {
            val ui = build(emptyList()).ui.value

            assertEquals("Copy channels", ui.title)
            assertEquals(listOf(SettingsRow.Note("Create a custom group first")), ui.rows)
        }
    }

    @Test
    fun `several custom groups ask for the target first`() {
        runTest {
            val session = build(listOf(CustomGroup(1, "Picks"), CustomGroup(2, "Kids OK")))

            assertEquals("Copy channels to", session.ui.value.title)
            assertEquals(listOf("Picks", "Kids OK"), session.ui.value.rows.map { (it as SettingsRow.Action).title })

            session.activate("group:2")
            assertEquals("Copy channels to Kids OK", session.ui.value.title)
        }
    }

    @Test
    fun `a single custom group skips the target picker`() {
        runTest {
            val session = build(listOf(CustomGroup(1, "Picks")))

            assertEquals("Copy channels to Picks", session.ui.value.title)
            // Done leads, then one toggle row per VISIBLE channel.
            assertEquals(listOf("done", "channel:1", "channel:2"), session.ui.value.rows.map { it.id })
        }
    }

    @Test
    fun `ok toggles a row and done copies the checked channels in by key`() {
        runTest {
            store.create("Picks")
            val session = build(store.groups.value)

            session.activate("channel:1")
            session.activate("channel:2")
            session.activate("channel:2")
            assertEquals(
                listOf(true, false),
                session.ui.value.rows.filterIsInstance<SettingsRow.Toggle>().map { it.checked },
            )

            session.activate("done")

            assertEquals(setOf("tvg-1"), store.groups.value.single().members)
            assertEquals(1, done)
        }
    }

    @Test
    fun `copying the same channel twice keeps one membership row`() {
        runTest {
            store.create("Picks")
            store.addMembers(1, listOf("tvg-1"))
            val session = build(store.groups.value)

            session.activate("channel:1")
            session.activate("done")

            assertEquals(setOf("tvg-1"), store.groups.value.single().members)
            assertTrue(done > 0)
        }
    }
}
