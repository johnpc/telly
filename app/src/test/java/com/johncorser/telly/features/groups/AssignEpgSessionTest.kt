package com.johncorser.telly.features.groups

import com.johncorser.telly.features.playlist.db.ChannelEntity
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
import org.junit.Assert.assertNull
import org.junit.Test

/** The Assign EPG picker: Auto first, ids from the loaded EPG data. */
@OptIn(ExperimentalCoroutinesApi::class)
class AssignEpgSessionTest {
    private val ids = MutableStateFlow(listOf("news-one.fixture", "sports.fixture"))
    private val updates = mutableListOf<ChannelEntity>()
    private var done = 0

    private fun TestScope.build(channel: ChannelEntity): AssignEpgSession =
        AssignEpgSession(
            channel = channel,
            epgIds = ids,
            update = { updates += it },
            onDone = { done += 1 },
            scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler)),
        )

    @Test
    fun `auto leads with the tvg-id as summary and is selected by default`() {
        runTest {
            val ui = build(testChannel(1, 1, "News One")).ui.value

            assertEquals("Assign EPG", ui.title)
            val auto = ui.rows.first() as SettingsRow.Value
            assertEquals("Auto (tvg-id)", auto.title)
            assertEquals("tvg-1", auto.summary)
            val values = ui.rows.filterIsInstance<SettingsRow.Value>()
            assertEquals(listOf(true, false, false), values.map { it.selected })
            assertEquals(listOf("Auto (tvg-id)", "news-one.fixture", "sports.fixture"), values.map { it.title })
        }
    }

    @Test
    fun `picking an id persists the override and closes the tool`() {
        runTest {
            build(testChannel(1, 1, "News One")).activate("epg:sports.fixture")

            assertEquals("sports.fixture", updates.single().flags.epgOverride)
            assertEquals("sports.fixture", updates.single().epgId)
            assertEquals(1, done)
        }
    }

    @Test
    fun `an existing override marks its row selected and auto resets it`() {
        runTest {
            val channel =
                testChannel(1, 1, "News One").let {
                    it.copy(flags = it.flags.copy(epgOverride = "sports.fixture"))
                }
            val session = build(channel)

            assertEquals(
                listOf(false, false, true),
                session.ui.value.rows.map { (it as SettingsRow.Value).selected },
            )

            session.activate("auto")
            assertNull(updates.single().flags.epgOverride)
            assertEquals("tvg-1", updates.single().epgId)
        }
    }

    @Test
    fun `foreign row ids are ignored`() {
        runTest {
            build(testChannel(1, 1, "News One")).activate("done")

            assertEquals(0, updates.size)
            assertEquals(0, done)
        }
    }
}
