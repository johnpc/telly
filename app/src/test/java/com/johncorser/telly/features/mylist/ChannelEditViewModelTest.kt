package com.johncorser.telly.features.mylist

import com.johncorser.telly.features.panel.PanelViewModel
import com.johncorser.telly.testutil.FakeChannelDao
import com.johncorser.telly.testutil.asFavorite
import com.johncorser.telly.testutil.testChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Manage Favorites (null group) and Reorder channels (a group) state. */
@OptIn(ExperimentalCoroutinesApi::class)
class ChannelEditViewModelTest {
    private val dao =
        FakeChannelDao(
            listOf(
                testChannel(1, 1, "News One", group = "News"),
                testChannel(2, 2, "News Two", group = "News"),
                testChannel(3, 3, "Sports Arena", group = "Sports"),
            ),
        )

    private fun TestScope.buildVm(group: String? = null): ChannelEditViewModel =
        ChannelEditViewModel(dao, CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler)), group)

    private fun byId(id: Long) = dao.channels.value.first { it.id == id }

    @Test
    fun `manage favorites lists favorites first and OK toggles the flag`() =
        runTest {
            dao.update(byId(3).asFavorite())
            val vm = buildVm()
            assertEquals(listOf(3L, 1L, 2L), vm.rows.value.map { it.id })

            vm.toggle(byId(1))
            assertTrue(byId(1).flags.favorite)
            assertEquals(listOf(3L, 1L, 2L), vm.rows.value.map { it.id })

            vm.toggle(byId(3))
            assertFalse(byId(3).flags.favorite)
            assertEquals(listOf(1L, 2L, 3L), vm.rows.value.map { it.id })
        }

    @Test
    fun `manage favorites moves persist the favorites order`() =
        runTest {
            dao.update(byId(1).asFavorite())
            dao.update(byId(2).asFavorite())
            val vm = buildVm()

            vm.move(byId(1), +1)

            assertEquals(listOf(2L, 1L, 3L), vm.rows.value.map { it.id })
            assertTrue(byId(2).flags.favoriteOrder < byId(1).flags.favoriteOrder)
        }

    @Test
    fun `reorder within a group swaps the neighbours' sort indices`() =
        runTest {
            val vm = buildVm(group = "News")
            assertEquals(listOf(1L, 2L), vm.rows.value.map { it.id })

            vm.move(byId(1), +1)

            assertEquals(listOf(2L, 1L), vm.rows.value.map { it.id })
            // The moved channel keeps its group but owns the later slot.
            assertTrue(byId(2).sortIndex < byId(1).sortIndex)
        }

    @Test
    fun `reordering the Favorites group moves the favorites order, not sort indices`() =
        runTest {
            dao.update(byId(1).asFavorite())
            dao.update(byId(3).asFavorite())
            val vm = buildVm(group = PanelViewModel.FAVORITES)
            assertEquals(listOf(1L, 3L), vm.rows.value.map { it.id })

            vm.move(byId(1), +1)

            assertEquals(listOf(3L, 1L), vm.rows.value.map { it.id })
            assertEquals(0, byId(1).sortIndex)
        }

    @Test
    fun `reorder screens keep OK inert and edge moves change nothing`() =
        runTest {
            val vm = buildVm(group = "News")

            vm.toggle(byId(1))
            vm.move(byId(1), -1)

            assertFalse(byId(1).flags.favorite)
            assertEquals(listOf(1L, 2L), vm.rows.value.map { it.id })
        }
}
