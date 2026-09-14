package com.johncorser.telly.features.guide

import com.johncorser.telly.features.guide.GuideTestData.at
import com.johncorser.telly.features.guide.GuideTestData.originMs
import com.johncorser.telly.features.guide.GuideTestData.utc
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.TimeZone

class GuideGeometryTest {
    @Test
    fun `the origin floors now to the wall-clock half hour`() {
        assertEquals(at(14, 30), GuideGeometry.halfHourFloor(at(14, 38), utc))
        assertEquals(at(14, 30), GuideGeometry.halfHourFloor(at(14, 59), utc))
        assertEquals(at(14, 0), GuideGeometry.halfHourFloor(at(14, 0), utc))
    }

    @Test
    fun `the origin respects zones with odd offsets`() {
        val kathmandu = TimeZone.getTimeZone("Asia/Kathmandu")
        // 14:38 UTC = 20:23 local (+5:45); the local half-hour floor is 20:00 = 14:15 UTC.
        assertEquals(at(14, 15), GuideGeometry.halfHourFloor(at(14, 38), kathmandu))
    }

    @Test
    fun `x maps 30 minutes to 160 dp per the captured timeline`() {
        assertEquals(0f, GuideGeometry.xOf(originMs, originMs))
        assertEquals(160f, GuideGeometry.xOf(at(15, 0), originMs))
        assertEquals(-320f, GuideGeometry.xOf(at(13, 30), originMs))
        assertEquals(240f, GuideGeometry.widthOf(at(14, 30), at(15, 15)))
    }

    @Test
    fun `timeAt inverts xOf`() {
        assertEquals(at(15, 30), GuideGeometry.timeAt(320f, originMs))
        assertEquals(at(14, 30), GuideGeometry.timeAt(0f, originMs))
        assertEquals(at(13, 30), GuideGeometry.timeAt(-320f, originMs))
    }
}
