package com.johncorser.telly.features.mylist

import com.johncorser.telly.testutil.testChannel
import com.johncorser.telly.testutil.testProgram
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** The My-list toggle + saved-key state both sheet hosts share. */
@OptIn(ExperimentalCoroutinesApi::class)
class MyListMenuTest {
    private val store = InMemoryMyListStore()
    private val channel = testChannel(1, 1, "News One")
    private val programme =
        MyListProgramme(title = "Business Hour", startMs = 1_000L, endMs = 2_000L, description = "D")

    private fun TestScope.build(): MyListMenu =
        MyListMenu(store, { 42L }, CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler)))

    @Test
    fun `toggle saves the programme with the stable channel key and the clock stamp`() =
        runTest {
            build().toggle(channel, programme)

            val saved = store.entries.first().single()
            assertEquals("tvg-1", saved.channelKey)
            assertEquals("Business Hour", saved.title)
            assertEquals(1_000L to 2_000L, saved.startMs to saved.endMs)
            assertEquals("D", saved.description)
            assertEquals(42L, saved.addedAtMs)
        }

    @Test
    fun `toggling the same programme again removes it`() =
        runTest {
            val menu = build()

            menu.toggle(channel, programme)
            menu.toggle(channel, programme)

            assertEquals(emptyList<Any>(), store.entries.first())
        }

    @Test
    fun `keys flip with the store so the row labels can follow`() =
        runTest {
            val menu = build()
            assertFalse(MyListKeys.saved(menu.keys.value, channel, programme.startMs))

            menu.toggle(channel, programme)

            assertTrue(MyListKeys.saved(menu.keys.value, channel, programme.startMs))
            assertEquals("Remove from My list", MyListKeys.label(saved = true))
            assertEquals("Add to My list", MyListKeys.label(saved = false))
        }

    @Test
    fun `a null programme (no-information cell) never toggles`() =
        runTest {
            build().toggle(channel, programme = null)

            assertEquals(emptyList<Any>(), store.entries.first())
        }

    @Test
    fun `saved is false without a start time`() {
        assertFalse(MyListKeys.saved(setOf("tvg-1|1000"), channel, startMs = null))
    }

    @Test
    fun `the host resolves the programme per channel for toggles and labels`() =
        runTest {
            val menu = build()
            var manage = 0
            val reordered = mutableListOf<String>()
            val host =
                MyListMenuHost(
                    menu = menu,
                    programmeFor = { _ -> programme },
                    group = { "Sports" },
                    openManageFavorites = { manage += 1 },
                    openReorderChannels = { reordered += it },
                )

            host.toggleFor(channel)
            assertTrue(host.savedFor(channel, menu.keys.value))

            host.openManageFavorites()
            host.openReorder()
            assertEquals(1, manage)
            assertEquals(listOf("Sports"), reordered)
        }

    @Test
    fun `programmes convert from entities and panel rows alike`() {
        val fromEntity = MyListProgramme.of(testProgram("tvg-1", 1_000L, 2_000L, "Business Hour", episode = "S1 E7"))
        assertEquals("Business Hour. S1 E7", fromEntity.title)
        assertEquals("Description of Business Hour", fromEntity.description)

        val row =
            com.johncorser.telly.features.panel
                .PanelRow(
                    channel = channel,
                    displayNumber = 1,
                    nowTitle = "Business Hour",
                    nowRange = null,
                    nowStartMs = 1_000L,
                    nowEndMs = 2_000L,
                    remaining = null,
                    description = "D",
                    nextTitle = null,
                    progressPermille = 0,
                )
        assertEquals(programme, MyListProgramme.of(row))
        assertNull(MyListProgramme.of(row.copy(nowTitle = null)))
        assertNull(MyListProgramme.of(row.copy(nowStartMs = null)))
        assertNull(MyListProgramme.of(row.copy(nowEndMs = null)))
    }
}
