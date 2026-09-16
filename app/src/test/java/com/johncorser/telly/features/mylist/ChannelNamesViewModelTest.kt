package com.johncorser.telly.features.mylist

import com.johncorser.telly.features.playlist.db.displayName
import com.johncorser.telly.testutil.FakeChannelDao
import com.johncorser.telly.testutil.testChannel
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

/** The bulk "Channel names editor" behind the Channel-options row. */
@OptIn(ExperimentalCoroutinesApi::class)
class ChannelNamesViewModelTest {
    private val dao =
        FakeChannelDao(
            listOf(
                testChannel(1, 1, "News One"),
                testChannel(2, 2, "News Two"),
                testChannel(3, 3, "Hidden").let { it.copy(flags = it.flags.copy(hidden = true)) },
            ),
        )

    private fun TestScope.build(): ChannelNamesViewModel =
        ChannelNamesViewModel(dao, CoroutineScope(UnconfinedTestDispatcher(testScheduler)))

    @Test
    fun `lists the visible channels in zap order`() =
        runTest {
            assertEquals(listOf("News One", "News Two"), build().rows.value.map { it.displayName })
        }

    @Test
    fun `ok opens the rename dialog and a commit persists the custom name`() =
        runTest {
            val viewModel = build()
            val target = viewModel.rows.value.first()

            viewModel.edit(target)
            assertEquals(target, viewModel.editing.value)

            viewModel.rename("News Uno")
            assertNull(viewModel.editing.value)
            assertEquals("News Uno", dao.channels.value.first { it.id == 1L }.overrides.customName)
            assertEquals(listOf("News Uno", "News Two"), viewModel.rows.value.map { it.displayName })
        }

    @Test
    fun `a blank commit restores the playlist name`() =
        runTest {
            val viewModel = build()
            viewModel.edit(viewModel.rows.value.first())
            viewModel.rename("News Uno")

            viewModel.edit(viewModel.rows.value.first())
            viewModel.rename("")

            assertNull(dao.channels.value.first { it.id == 1L }.overrides.customName)
        }

    @Test
    fun `back closes an open dialog first without renaming`() =
        runTest {
            val viewModel = build()
            assertFalse(viewModel.closeDialog())

            viewModel.edit(viewModel.rows.value.first())
            assertTrue(viewModel.closeDialog())
            assertNull(viewModel.editing.value)
            assertNull(dao.channels.value.first().overrides.customName)
        }
}
