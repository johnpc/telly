package com.johncorser.telly.features.catchup

import com.johncorser.telly.features.playback.ClockStyle
import com.johncorser.telly.features.playback.PlaybackInfoData
import com.johncorser.telly.testutil.testChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.GregorianCalendar
import java.util.TimeZone

@OptIn(ExperimentalCoroutinesApi::class)
class CatchupInfoTest {
    private val utc = TimeZone.getTimeZone("UTC")

    private fun at(
        hour: Int,
        minute: Int,
    ): Long =
        GregorianCalendar(utc)
            .apply {
                set(2026, 8, 13, hour, minute, 0)
                set(GregorianCalendar.MILLISECOND, 0)
            }.timeInMillis

    private val liveData =
        PlaybackInfoData(
            number = 1,
            name = "News One",
            logoUrl = null,
            group = "News",
            clockText = "Sun, Sep 13, 2:45 PM",
            title = "Airing Now",
            timeRange = "02:30 — 03:45 PM",
            remaining = "61 min",
            progressPermille = 500,
            description = "Live synopsis",
            nextLine = "03:45 — 05:15 PM  Next Up",
            badges = listOf("HD"),
            elapsed = "10:00",
            duration = "75:00",
        )

    private val request =
        CatchupRequest(
            channel = testChannel(1, 1, "News One"),
            url = "http://s/arc",
            title = "Morning Report",
            startMs = at(9, 0),
            endMs = at(10, 30),
        )

    @Test
    fun `catch-up swaps the programme lines to the archive and a position readout`() =
        runTest {
            val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
            val state = MutableStateFlow<CatchupState?>(CatchupState(request, fromLive = false))
            val position = MutableStateFlow(27 * 60_000L)

            val merged = CatchupInfo.merged(MutableStateFlow(liveData), state, position, ClockStyle(utc), scope)
            val data = merged.value

            assertEquals("Morning Report", data?.title)
            assertEquals("09:00 — 10:30 AM", data?.timeRange)
            assertEquals("27:00", data?.elapsed)
            assertEquals("1:30:00", data?.duration)
            assertEquals(300, data?.progressPermille)
            assertNull(data?.remaining)
            assertNull(data?.nextLine)
            // Channel identity and badges keep their live values.
            assertEquals("News One", data?.name)
            assertEquals(listOf("HD"), data?.badges)
        }

    @Test
    fun `without catch-up the live info passes through untouched`() =
        runTest {
            val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))

            val merged =
                CatchupInfo.merged(
                    MutableStateFlow(liveData),
                    MutableStateFlow(null),
                    MutableStateFlow(0L),
                    ClockStyle(utc),
                    scope,
                )

            assertEquals(liveData, merged.value)
        }
}
