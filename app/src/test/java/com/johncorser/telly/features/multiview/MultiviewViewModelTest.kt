package com.johncorser.telly.features.multiview

import com.johncorser.telly.features.playback.ClockStyle
import com.johncorser.telly.features.playback.PlaybackTime
import com.johncorser.telly.features.playback.TuneController
import com.johncorser.telly.testutil.FakeChannelDao
import com.johncorser.telly.testutil.FakeKeyValueStore
import com.johncorser.telly.testutil.FakePlayerEngine
import com.johncorser.telly.testutil.FakeProgramDao
import com.johncorser.telly.testutil.testChannel
import com.johncorser.telly.testutil.testEpgRepository
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
import java.util.TimeZone

@OptIn(ExperimentalCoroutinesApi::class)
class MultiviewViewModelTest {
    private val channels = (1L..5L).map { testChannel(it, it.toInt(), "Channel $it") }
    private val dao = FakeChannelDao(channels)
    private val store = FakeKeyValueStore()
    private val engines = mutableListOf<FakePlayerEngine>()
    private var exits = 0

    private fun TestScope.buildVm(): MultiviewViewModel =
        MultiviewViewModel(
            deps =
                MultiviewDeps(
                    channelDao = dao,
                    epgRepository = testEpgRepository(FakeProgramDao()),
                    engines = { FakePlayerEngine().also { engines += it } },
                    store = store,
                    time = PlaybackTime({ 0L }, ClockStyle(TimeZone.getTimeZone("UTC"))),
                ),
            scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler)),
            onExit = { exits += 1 },
        )

    private fun TestScope.startedVm(lastChannelId: Long? = 2L): MultiviewViewModel {
        lastChannelId?.let { store.putLong(TuneController.LAST_CHANNEL_KEY, it) }
        return buildVm().also { it.start() }
    }

    @Test
    fun `entry builds one pane playing the last-watched channel`() =
        runTest {
            val vm = startedVm(lastChannelId = 2L)

            assertEquals(listOf(2L), vm.panes.panes.value.map { it.channel.id })
            assertEquals(listOf("http://s/2.ts"), engines.single().loaded)
            assertFalse(engines.single().muted)
            assertEquals(MultiviewLayer.Panes, vm.layer.value)
        }

    @Test
    fun `entry falls back to the first channel without a stored id`() =
        runTest {
            val vm = startedVm(lastChannelId = null)

            assertEquals(listOf(1L), vm.panes.panes.value.map { it.channel.id })
        }

    @Test
    fun `ok on a pane focuses it and opens its menu`() =
        runTest {
            val vm = startedVm()

            vm.onPaneOk(1)

            assertEquals(1, vm.panes.focusedId.value)
            assertEquals(MultiviewLayer.Menu, vm.layer.value)
        }

    @Test
    fun `remove screen only appears while the grid has more than one pane`() =
        runTest {
            val vm = startedVm()
            assertEquals(
                listOf("Add screen", "Search and add", "Change channel"),
                vm.menuRows().map { it.label },
            )

            vm.onMenuAction(MultiviewMenuAction.ADD_SCREEN)
            vm.onPick(channels[2])

            assertTrue(vm.menuRows().contains(MultiviewMenuAction.REMOVE_SCREEN))
        }

    @Test
    fun `the add rows leave the menu at the four-pane cap`() =
        runTest {
            val vm = startedVm()
            listOf(channels[0], channels[2], channels[3]).forEach { channel ->
                vm.onMenuAction(MultiviewMenuAction.ADD_SCREEN)
                vm.onPick(channel)
            }

            assertEquals(MultiviewGrid.MAX_PANES, vm.panes.panes.value.size)
            assertEquals(
                listOf("Change channel", "Remove screen"),
                vm.menuRows().map { it.label },
            )
        }

    @Test
    fun `add screen and search and add both open the picker in add mode`() =
        runTest {
            val vm = startedVm()

            vm.onMenuAction(MultiviewMenuAction.ADD_SCREEN)
            assertEquals(MultiviewLayer.Picker(MultiviewPickerMode.ADD), vm.layer.value)

            vm.onMenuAction(MultiviewMenuAction.SEARCH_AND_ADD)
            assertEquals(MultiviewLayer.Picker(MultiviewPickerMode.ADD), vm.layer.value)
        }

    @Test
    fun `the picker opens focused on the focused pane's channel`() =
        runTest {
            val vm = startedVm(lastChannelId = 3L)

            vm.onMenuAction(MultiviewMenuAction.CHANGE_CHANNEL)

            assertEquals(MultiviewLayer.Picker(MultiviewPickerMode.CHANGE), vm.layer.value)
            assertEquals(2, vm.picker.panel.focusIndex.value)
        }

    @Test
    fun `picking in add mode fills a new pane and returns to the grid`() =
        runTest {
            val vm = startedVm()
            vm.onMenuAction(MultiviewMenuAction.ADD_SCREEN)

            vm.onPick(channels[3])

            assertEquals(listOf(2L, 4L), vm.panes.panes.value.map { it.channel.id })
            assertEquals(MultiviewLayer.Panes, vm.layer.value)
            assertFalse(engines[1].muted)
            assertTrue(engines[0].muted)
        }

    @Test
    fun `picking in change mode retunes the focused pane`() =
        runTest {
            val vm = startedVm()
            vm.onMenuAction(MultiviewMenuAction.CHANGE_CHANNEL)

            vm.onPick(channels[4])

            assertEquals(listOf(5L), vm.panes.panes.value.map { it.channel.id })
            assertEquals(MultiviewLayer.Panes, vm.layer.value)
        }

    @Test
    fun `picking outside the picker layer is ignored`() =
        runTest {
            val vm = startedVm()

            vm.onPick(channels[3])

            assertEquals(listOf(2L), vm.panes.panes.value.map { it.channel.id })
        }

    @Test
    fun `remove screen collapses the grid and stays on the pane view`() =
        runTest {
            val vm = startedVm()
            vm.onMenuAction(MultiviewMenuAction.ADD_SCREEN)
            vm.onPick(channels[2])

            vm.onMenuAction(MultiviewMenuAction.REMOVE_SCREEN)

            assertEquals(listOf(2L), vm.panes.panes.value.map { it.channel.id })
            assertEquals(MultiviewLayer.Panes, vm.layer.value)
        }

    @Test
    fun `back walks picker to panes, menu to panes, then exits to fullscreen`() =
        runTest {
            val vm = startedVm()
            vm.onPaneOk(1)
            vm.onMenuAction(MultiviewMenuAction.CHANGE_CHANNEL)

            vm.onBack()
            assertEquals(MultiviewLayer.Panes, vm.layer.value)
            assertEquals(0, exits)

            vm.onPaneOk(1)
            vm.onBack()
            assertEquals(MultiviewLayer.Panes, vm.layer.value)

            vm.onBack()
            assertEquals(1, exits)
        }

    @Test
    fun `exit persists the focused pane's channel as the fullscreen channel`() =
        runTest {
            val vm = startedVm()
            vm.onMenuAction(MultiviewMenuAction.ADD_SCREEN)
            vm.onPick(channels[3])

            vm.onBack()

            assertEquals(4L, store.getLong(TuneController.LAST_CHANNEL_KEY))
            assertEquals(1, exits)
        }

    @Test
    fun `channel keys zap the focused pane only on the pane layer`() =
        runTest {
            val vm = startedVm(lastChannelId = 1L)

            vm.onChannelKey(+1)
            assertEquals(2L, vm.panes.panes.value.single().channel.id)

            vm.onPaneOk(1)
            vm.onChannelKey(+1)
            assertEquals(2L, vm.panes.panes.value.single().channel.id)
        }

    @Test
    fun `close releases every pane engine`() =
        runTest {
            val vm = startedVm()
            vm.onMenuAction(MultiviewMenuAction.ADD_SCREEN)
            vm.onPick(channels[2])

            vm.close()

            assertTrue(engines.all { it.released })
        }
}
