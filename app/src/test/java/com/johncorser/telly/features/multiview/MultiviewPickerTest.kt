package com.johncorser.telly.features.multiview

import com.johncorser.telly.testutil.FakeChannelDao
import com.johncorser.telly.testutil.FakeProgramDao
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
import org.junit.Test
import java.util.GregorianCalendar
import java.util.TimeZone

@OptIn(ExperimentalCoroutinesApi::class)
class MultiviewPickerTest {
    private val utc = TimeZone.getTimeZone("UTC")
    private val dao =
        FakeChannelDao(
            listOf(
                testChannel(1, 1, "News One"),
                testChannel(2, 2, "News Two"),
                testChannel(3, 3, "Sports Arena"),
            ),
        )

    private fun at(
        hour: Int,
        minute: Int,
    ): Long =
        GregorianCalendar(utc)
            .apply {
                set(2026, 8, 13, hour, minute, 0)
                set(GregorianCalendar.MILLISECOND, 0)
            }.timeInMillis

    private val programs =
        FakeProgramDao(
            listOf(
                testProgram("tvg-1", at(14, 0), at(15, 0), "Now One"),
                testProgram("tvg-1", at(15, 0), at(16, 0), "Next One"),
                testProgram("tvg-2", at(14, 30), at(15, 30), "Now Two"),
            ),
        )

    private fun TestScope.buildPicker(): MultiviewPicker =
        MultiviewPicker(
            channelDao = dao,
            epgRepository = testEpgRepository(programs),
            clock = { at(14, 45) },
            scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler)),
            zone = utc,
        )

    @Test
    fun `the focused row drives the schedule pane and the detail card`() =
        runTest {
            val picker = buildPicker()

            assertEquals("News One", picker.focusedRow.value?.channel?.source?.name)
            assertEquals("Now One", picker.focusedRow.value?.nowTitle)
            assertEquals(listOf("Now One", "Next One"), picker.schedule.value.map { it.title })
            assertEquals(listOf("02:00 PM", "03:00 PM"), picker.schedule.value.map { it.timeText })
            assertEquals(listOf(true, false), picker.schedule.value.map { it.airing })
        }

    @Test
    fun `focusing another channel swaps the schedule`() =
        runTest {
            val picker = buildPicker()

            picker.panel.onRowFocused(1)

            assertEquals("News Two", picker.focusedRow.value?.channel?.source?.name)
            assertEquals(listOf("Now Two"), picker.schedule.value.map { it.title })
        }

    @Test
    fun `a channel without EPG identity has an empty schedule`() =
        runTest {
            dao.channels.value = listOf(testChannel(9, 1, "Bare", tvgId = null))
            val picker = buildPicker()

            assertEquals(emptyList<MultiviewScheduleRow>(), picker.schedule.value)
        }

    @Test
    fun `open focuses the pane's current channel row`() =
        runTest {
            val picker = buildPicker()

            picker.open(3L)

            assertEquals(2, picker.panel.focusIndex.value)
            assertEquals("Sports Arena", picker.focusedRow.value?.channel?.source?.name)
        }
}
