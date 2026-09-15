package com.johncorser.telly.features.guide

import com.johncorser.telly.testutil.testChannel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** The sheet scrim's undimmed originating-row band (round7 P2). */
class GuideDimExemptionTest {
    private val rows =
        (1L..12L).map { id ->
            GuideRow(testChannel(id, id.toInt(), "Ch $id"), id.toInt(), emptyList())
        }

    private fun band(
        channelId: Long?,
        firstRow: Int,
        inRows: List<GuideRow> = rows,
    ): Float? = GuideDimExemption.bandTopDp(channelId, inRows, firstRow)

    @Test
    fun `the saved channel's current row yields the band top`() {
        assertEquals(GuideGeometry.GRID_TOP_DP, band(1L, firstRow = 0)!!, 0f)
        assertEquals(
            GuideGeometry.GRID_TOP_DP + 2 * GuideGeometry.ROW_HEIGHT_DP,
            band(5L, firstRow = 2)!!,
            0f,
        )
    }

    @Test
    fun `the band follows the channel when rows re-emit under the sheet`() {
        val shifted = rows.drop(2)

        assertEquals(GuideGeometry.GRID_TOP_DP, band(3L, firstRow = 0, inRows = shifted)!!, 0f)
    }

    @Test
    fun `no saved channel means the uniform scrim stands`() {
        assertNull(band(null, firstRow = 0))
    }

    @Test
    fun `a vanished channel means the uniform scrim stands`() {
        assertNull(band(99L, firstRow = 0))
    }

    @Test
    fun `a row scrolled above or below the grid viewport is not exempt`() {
        assertNull(band(1L, firstRow = 3))
        // 306 dp of grid shows 7 full rows + a partial 8th (still exempt).
        assertEquals(GuideGeometry.GRID_TOP_DP + 7 * GuideGeometry.ROW_HEIGHT_DP, band(8L, firstRow = 0)!!, 0f)
        assertNull(band(9L, firstRow = 0))
    }
}
