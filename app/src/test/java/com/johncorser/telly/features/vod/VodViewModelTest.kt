package com.johncorser.telly.features.vod

import com.johncorser.telly.features.player.Media3PlayerEngine
import com.johncorser.telly.features.vod.db.VodItemEntity
import com.johncorser.telly.features.vod.db.VodPositionEntity
import io.mockk.mockk
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
class VodViewModelTest {
    private val items = FakeVodItemDao()
    private val positions = FakeVodPositionDao()

    private fun deps(): VodDeps =
        VodDeps(
            items = items,
            positions = positions,
            engineFactory = { mockk<Media3PlayerEngine>(relaxed = true) },
            rememberPosition = { true },
            clock = { 1_000L },
        )

    private fun TestScope.model(): VodViewModel =
        VodViewModel(deps(), CoroutineScope(UnconfinedTestDispatcher(testScheduler)))

    private fun item(
        name: String,
        group: String?,
        index: Int,
    ) = VodItemEntity(
        playlistId = 1,
        sortIndex = index,
        itemKey = "http://s/$name.mp4|$name",
        name = name,
        groupTitle = group,
        streamUrl = "http://s/$name.mp4",
    )

    @Test
    fun `empty until the import produces items`() =
        runTest {
            val model = model()
            assertTrue(model.empty.value)
            assertNull(model.selected.value)

            items.insertAll(listOf(item("A", "Cinema", 0)))

            assertFalse(model.empty.value)
        }

    @Test
    fun `the first category is selected by default`() =
        runTest {
            items.insertAll(listOf(item("A", "Cinema", 0), item("B", "Docs", 1)))
            val model = model()

            assertEquals(listOf("Cinema", "Docs"), model.categories.value)
            assertEquals("Cinema", model.selected.value)
            assertEquals(listOf("A"), model.cards.value.map { it.item.name })
        }

    @Test
    fun `selecting a category swaps the card grid`() =
        runTest {
            items.insertAll(listOf(item("A", "Cinema", 0), item("B", "Docs", 1)))
            val model = model()

            model.select("Docs")

            assertEquals("Docs", model.selected.value)
            assertEquals(listOf("B"), model.cards.value.map { it.item.name })
        }

    @Test
    fun `a vanished chosen category falls back to the first`() =
        runTest {
            items.insertAll(listOf(item("A", "Cinema", 0), item("B", "Docs", 1)))
            val model = model()
            model.select("Docs")

            items.items.value = listOf(item("A", "Cinema", 0))

            assertEquals("Cinema", model.selected.value)
        }

    @Test
    fun `stored positions surface as card progress`() =
        runTest {
            val a = item("A", "Cinema", 0)
            items.insertAll(listOf(a))
            positions.upsert(VodPositionEntity(a.itemKey, positionMs = 30_000, durationMs = 60_000, updatedAtMs = 1))
            val model = model()

            assertEquals(500, model.cards.value.single().progressPermille)
        }
}
