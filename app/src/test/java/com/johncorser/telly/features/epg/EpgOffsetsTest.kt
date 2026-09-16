package com.johncorser.telly.features.epg

import com.johncorser.telly.features.playlist.db.TvgOffset
import com.johncorser.telly.testutil.FakeProgramDao
import com.johncorser.telly.testutil.testProgram
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/** Per-channel "EPG time offset" math + its application in EpgRepository. */
class EpgOffsetsTest {
    private val hour = 3_600_000L

    @Test
    fun `offsets map converts minutes to millis and skips null tvg-ids`() {
        val map =
            EpgOffsets.ofMinutes(
                listOf(TvgOffset("a", 60), TvgOffset(null, 30), TvgOffset("b", -30), TvgOffset("a", 90)),
            )
        assertEquals(mapOf("a" to hour, "b" to -hour / 2), map)
    }

    @Test
    fun `query bounds pad by the largest forward and backward shifts only`() {
        val offsets = mapOf("a" to hour, "b" to -hour / 2)
        assertEquals(9 * hour, EpgOffsets.queryFrom(10 * hour, offsets))
        assertEquals(20 * hour + hour / 2, EpgOffsets.queryTo(20 * hour, offsets))
        // No offsets: the window is untouched.
        assertEquals(10 * hour, EpgOffsets.queryFrom(10 * hour, emptyMap()))
        assertEquals(20 * hour, EpgOffsets.queryTo(20 * hour, emptyMap()))
    }

    @Test
    fun `shifted moves only the offset channels' times`() {
        val programs = listOf(testProgram("a", 0, hour, "A"), testProgram("b", 0, hour, "B"))
        val shifted = EpgOffsets.shifted(programs, mapOf("a" to hour))
        assertEquals(listOf(hour to 2 * hour, 0L to hour), shifted.map { it.startMs to it.endMs })
    }

    @Test
    fun `windowed trims shifted programmes back to the requested display window`() {
        val programs =
            listOf(
                // Shifts INTO [2h, 3h): kept.
                testProgram("a", hour, 2 * hour, "In"),
                // Shifts OUT of the window: dropped.
                testProgram("a", 2 * hour, 3 * hour, "Out"),
            )
        val windowed = EpgOffsets.windowed(programs, mapOf("a" to hour), fromMs = 2 * hour, toMs = 3 * hour)
        assertEquals(listOf("In"), windowed.map { it.details.title })
    }

    @Test
    fun `programsFor returns shifted times so the guide grid moves with the offset`() =
        runTest {
            val dao = FakeProgramDao(listOf(testProgram("a", hour, 2 * hour, "News")))
            val offsets = MutableStateFlow(mapOf("a" to hour))
            val repository = EpgRepository(dao, newParser = { error("unused") }, offsets = offsets)

            // Displayed at 2h-3h now: found through the padded query + shift.
            val window = repository.programsFor(listOf("a"), 2 * hour, 3 * hour).first()
            assertEquals(listOf(2 * hour to 3 * hour), window.map { it.startMs to it.endMs })

            // Clearing the offset moves it back out of that window.
            offsets.value = emptyMap()
            assertEquals(0, repository.programsFor(listOf("a"), 2 * hour, 3 * hour).first().size)
        }

    @Test
    fun `nowNext resolves the programme airing at the SHIFTED time`() =
        runTest {
            // Really stored 1h-2h; a +1h offset displays it 2h-3h.
            val dao = FakeProgramDao(listOf(testProgram("a", hour, 2 * hour, "News")))
            val offsets = MutableStateFlow(mapOf("a" to hour))
            val repository = EpgRepository(dao, newParser = { error("unused") }, offsets = offsets)

            val atHalfPastTwo = repository.nowNext(listOf("a"), 2 * hour + hour / 2).first()
            assertEquals("News", atHalfPastTwo.getValue("a").now?.details?.title)

            // Without the offset the same instant has no airing programme.
            offsets.value = emptyMap()
            val without = repository.nowNext(listOf("a"), 2 * hour + hour / 2).first()
            assertEquals(null, without["a"]?.now)
        }
}
