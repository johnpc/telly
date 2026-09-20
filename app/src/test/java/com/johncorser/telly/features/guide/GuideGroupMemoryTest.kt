package com.johncorser.telly.features.guide

import com.johncorser.telly.features.groups.CustomGroup
import com.johncorser.telly.features.panel.PanelViewModel
import com.johncorser.telly.features.playlist.db.ChannelEntity
import com.johncorser.telly.testutil.FakeKeyValueStore
import com.johncorser.telly.testutil.testChannel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GuideGroupMemoryTest {
    private fun channels(vararg groups: String): StateFlow<List<ChannelEntity>> =
        MutableStateFlow(groups.mapIndexed { i, g -> testChannel(i + 1L, i + 1, "ch$i", group = g) })

    @Test
    fun `a persisted group restores from the store on build`() {
        val store = FakeKeyValueStore().apply { putString(GuideGroupMemory.GROUP_KEY, "News") }

        assertEquals("News", GuideGroupMemory(store).value)
    }

    @Test
    fun `arm keeps a restored group the channels snapshot still has`() =
        runTest {
            val store = FakeKeyValueStore().apply { putString(GuideGroupMemory.GROUP_KEY, "Sports") }
            val memory = GuideGroupMemory(store)

            // The valid set is read from the awaited channels snapshot, not a
            // separately-derived flow — so the restore survives even when a
            // group flow would still be empty at build time (the on-device race).
            memory.arm(this, channels("News", "Sports"), MutableStateFlow(emptyList()))
            advanceUntilIdle()

            assertEquals("Sports", memory.value)
        }

    @Test
    fun `arm keeps a restored custom group present only in the custom set`() =
        runTest {
            val store = FakeKeyValueStore().apply { putString(GuideGroupMemory.GROUP_KEY, "Faves") }
            val memory = GuideGroupMemory(store)

            memory.arm(this, channels("News"), MutableStateFlow(listOf(CustomGroup(1, "Faves"))))
            advanceUntilIdle()

            assertEquals("Faves", memory.value)
        }

    @Test
    fun `arm drops a restored group the playlist no longer has back to all channels`() =
        runTest {
            val store = FakeKeyValueStore().apply { putString(GuideGroupMemory.GROUP_KEY, "Gone") }
            val memory = GuideGroupMemory(store)

            memory.arm(this, channels("News", "Sports"), MutableStateFlow(emptyList()))
            advanceUntilIdle()

            assertEquals(PanelViewModel.ALL_CHANNELS, memory.value)
        }
}
