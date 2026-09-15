package com.johncorser.telly.features.search

import com.johncorser.telly.core.settings.InMemoryKeyValueStore
import com.johncorser.telly.features.playback.TuneController
import com.johncorser.telly.testutil.FakeChannelDao
import com.johncorser.telly.testutil.FakeKeyValueStore
import com.johncorser.telly.testutil.FakeProgramDao
import com.johncorser.telly.testutil.FakeSearchDao
import com.johncorser.telly.testutil.testChannel
import com.johncorser.telly.testutil.testEpgRepository
import com.johncorser.telly.testutil.testProgram
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    private val now = 1_789_311_600_000L
    private val hour = 3_600_000L

    private val newsOne = testChannel(1, 1, "News One", tvgId = "one")
    private val channelDao = FakeChannelDao(listOf(newsOne, testChannel(2, 2, "Sports Arena", tvgId = "sports")))
    private val programDao =
        FakeProgramDao(
            listOf(
                testProgram("one", now, now + hour, "Newsroom Live"),
                testProgram("one", now + 2 * hour, now + 3 * hour, "Newsroom Live"),
                testProgram("sports", now + hour, now + 2 * hour, "Newsroom Update"),
            ),
        )
    private val historyStore = InMemoryKeyValueStore()
    private val lastChannelStore = FakeKeyValueStore()

    private fun TestScope.buildVm(): SearchViewModel =
        SearchViewModel(
            deps =
                SearchDeps(
                    repository =
                        SearchRepository(
                            FakeSearchDao(channelDao, programDao),
                            channelDao,
                            testEpgRepository(programDao),
                        ),
                    historyStore = historyStore,
                    lastChannelStore = lastChannelStore,
                    clock = { now },
                    zone = TimeZone.getTimeZone("UTC"),
                ),
            scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler)),
        )

    @Test
    fun `results start empty and follow the typed query`() =
        runTest {
            val vm = buildVm()
            assertTrue(vm.results.value.isEmpty)

            vm.onQueryChange("news")

            assertEquals(listOf("News One"), vm.results.value.channels.map { it.channel.source.name })
            assertEquals(
                listOf("News One", "Sports Arena"),
                vm.results.value.programs.map { it.channel.source.name },
            )
            assertEquals(
                listOf("Newsroom Live", "Newsroom Live"),
                vm.results.value.programs[0].airings.map { it.title },
            )
        }

    @Test
    fun `clearing the query returns to the empty state`() =
        runTest {
            val vm = buildVm()
            vm.onQueryChange("news")

            vm.onQueryChange("")

            assertTrue(vm.results.value.isEmpty)
        }

    @Test
    fun `submit records the query into the history`() =
        runTest {
            val vm = buildVm()
            vm.onQueryChange("news")

            vm.commit(vm.query.value)

            assertEquals(listOf("news"), vm.history.value)
        }

    @Test
    fun `a history entry re-runs its query`() =
        runTest {
            val vm = buildVm()

            vm.onHistoryEntry("news")

            assertEquals("news", vm.query.value)
            assertFalse(vm.results.value.isEmpty)
            assertEquals(listOf("news"), vm.history.value)
        }

    @Test
    fun `clear history empties the landing list`() =
        runTest {
            val vm = buildVm()
            vm.onHistoryEntry("news")

            vm.clearHistory()

            assertTrue(vm.history.value.isEmpty())
        }

    @Test
    fun `ok on a channel result persists it as the playback target`() =
        runTest {
            val vm = buildVm()
            vm.onQueryChange("news")

            vm.onChannelResult(newsOne)

            assertEquals(1L, lastChannelStore.getLong(TuneController.LAST_CHANNEL_KEY))
            assertEquals(listOf("news"), vm.history.value)
        }

    @Test
    fun `each result batch selects the first channel and preselects its first airing`() =
        runTest {
            val vm = buildVm()

            vm.onQueryChange("newsroom")

            assertEquals("News One", vm.selectedChannel.value?.channel?.source?.name)
            assertEquals("Newsroom Live", vm.focusedProgram.value?.title)
            assertEquals(now, vm.focusedProgram.value?.program?.startMs)

            vm.onQueryChange("")

            assertNull(vm.selectedChannel.value)
            assertNull(vm.focusedProgram.value)
        }

    @Test
    fun `focusing a master-lane card swaps the selection and preselects its first airing`() =
        runTest {
            val vm = buildVm()
            vm.onQueryChange("newsroom")
            val sports = vm.results.value.programs.last()

            vm.onProgramChannelFocused(sports)

            assertEquals("Sports Arena", vm.selectedChannel.value?.channel?.source?.name)
            assertEquals("Newsroom Update", vm.focusedProgram.value?.title)
        }

    @Test
    fun `ok on a programme result opens the guide-cell dropdown`() =
        runTest {
            val vm = buildVm()
            vm.onQueryChange("newsroom")
            val hit = vm.results.value.programs.first().airings.first()
            vm.onProgramFocused(hit)

            vm.onProgramResult(hit)

            assertEquals(SearchOverlay.ProgramMenu(hit), vm.overlays.current.value)
            assertEquals(hit, vm.focusedProgram.value)
        }

    @Test
    fun `ok on a master-lane card opens the shared paywall, not a tune`() =
        runTest {
            val vm = buildVm()
            vm.onQueryChange("newsroom")

            vm.onProgramChannelResult()

            assertEquals(SearchOverlay.Paywall, vm.overlays.current.value)
        }

    @Test
    fun `dropdown actions and the gear open the shared paywall`() =
        runTest {
            val vm = buildVm()

            vm.overlays.show(SearchOverlay.Paywall)

            assertEquals(SearchOverlay.Paywall, vm.overlays.current.value)
            assertTrue(vm.overlays.dismiss())
            assertEquals(SearchOverlay.None, vm.overlays.current.value)
        }

    @Test
    fun `voice search routes to the branded placeholder`() =
        runTest {
            val vm = buildVm()

            vm.overlays.show(SearchOverlay.ComingSoon("Voice search"))

            assertEquals(SearchOverlay.ComingSoon("Voice search"), vm.overlays.current.value)
        }

    @Test
    fun `back dismisses only an open overlay`() =
        runTest {
            val vm = buildVm()
            assertFalse(vm.overlays.dismiss())

            vm.overlays.show(SearchOverlay.ComingSoon("Voice search"))

            assertTrue(vm.overlays.dismiss())
            assertEquals(SearchOverlay.None, vm.overlays.current.value)
        }
}
