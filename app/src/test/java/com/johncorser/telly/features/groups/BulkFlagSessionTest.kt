package com.johncorser.telly.features.groups

import com.johncorser.telly.core.settings.InMemoryKeyValueStore
import com.johncorser.telly.core.settings.ParentalControls
import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.features.settings.SettingsRow
import com.johncorser.telly.testutil.FakeChannelDao
import com.johncorser.telly.testutil.testChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Manage blocking / Manage visibility: bulk toggles + the PIN gate. */
@OptIn(ExperimentalCoroutinesApi::class)
class BulkFlagSessionTest {
    private val dao =
        FakeChannelDao(
            listOf(
                testChannel(1, 1, "News One"),
                testChannel(2, 2, "Ghost").let { it.copy(flags = it.flags.copy(hidden = true)) },
            ),
        )
    private val parental = ParentalControls(SettingsRepository(InMemoryKeyValueStore()), Random(seed = 7))

    private fun TestScope.build(
        kind: BulkFlagKind,
        gate: ParentalControls? = null,
    ): BulkFlagSession {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        return BulkFlagSession(
            kind = kind,
            channels = dao.observeAll().stateIn(scope, SharingStarted.Eagerly, emptyList()),
            update = dao::update,
            parental = gate,
            scope = scope,
        )
    }

    @Test
    fun `manage visibility lists every channel with its hidden state`() {
        runTest {
            val ui = build(BulkFlagKind.VISIBILITY).ui.value

            assertEquals("Manage visibility", ui.title)
            assertFalse(ui.pin)
            assertEquals(
                listOf("News One" to false, "Ghost" to true),
                ui.rows.filterIsInstance<SettingsRow.Toggle>().map { it.title to it.checked },
            )
        }
    }

    @Test
    fun `ok toggles hidden and persists it immediately`() {
        runTest {
            val session = build(BulkFlagKind.VISIBILITY)

            session.activate("channel:1")
            assertTrue(dao.channels.value.first { it.id == 1L }.flags.hidden)

            session.activate("channel:2")
            assertFalse(dao.channels.value.first { it.id == 2L }.flags.hidden)
        }
    }

    @Test
    fun `manage blocking toggles the blocked flag`() {
        runTest {
            val session = build(BulkFlagKind.BLOCKING)

            session.activate("channel:1")
            assertTrue(dao.channels.value.first { it.id == 1L }.flags.blocked)

            session.activate("channel:1")
            assertFalse(dao.channels.value.first { it.id == 1L }.flags.blocked)
        }
    }

    @Test
    fun `blocking is gated behind the pin once per entry when parental is on`() {
        runTest {
            parental.setEnabled(true)
            parental.setPin("2468")
            val session = build(BulkFlagKind.BLOCKING, gate = parental)

            assertTrue(session.ui.value.pin)
            session.activate("channel:1")
            assertFalse(dao.channels.value.first { it.id == 1L }.flags.blocked)

            session.submitPin("0000")
            assertTrue(session.ui.value.pin)

            session.submitPin("2468")
            assertFalse(session.ui.value.pin)
            assertTrue(session.ui.value.rows.isNotEmpty())
        }
    }

    @Test
    fun `no pin gate while parental controls are off or visibility is managed`() {
        runTest {
            parental.setPin("2468")
            assertFalse(build(BulkFlagKind.BLOCKING, gate = parental).ui.value.pin)

            parental.setEnabled(true)
            assertFalse(build(BulkFlagKind.VISIBILITY, gate = parental).ui.value.pin)
        }
    }
}
