package com.johncorser.telly.features.guide

import com.johncorser.telly.features.guide.GuideTestData.at
import com.johncorser.telly.features.guide.GuideTestData.cell
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GuideKeyPolicyTest {
    private fun at(
        layer: GuideLayer,
        key: GuideKey,
    ): GuideCommand? = GuideKeyPolicy.commandFor(layer, key)

    @Test
    fun `the grid maps the d-pad to focus moves and ok to activation`() {
        assertEquals(GuideCommand.FocusLeft, at(GuideLayer.Grid, GuideKey.LEFT))
        assertEquals(GuideCommand.FocusRight, at(GuideLayer.Grid, GuideKey.RIGHT))
        assertEquals(GuideCommand.FocusUp, at(GuideLayer.Grid, GuideKey.UP))
        assertEquals(GuideCommand.FocusDown, at(GuideLayer.Grid, GuideKey.DOWN))
        assertEquals(GuideCommand.Activate, at(GuideLayer.Grid, GuideKey.OK))
    }

    @Test
    fun `held left and right jump a day like the hint toast advertises`() {
        assertEquals(GuideCommand.DayJump(-1), at(GuideLayer.Grid, GuideKey.LONG_LEFT))
        assertEquals(GuideCommand.DayJump(+1), at(GuideLayer.Grid, GuideKey.LONG_RIGHT))
    }

    @Test
    fun `back on the grid bubbles up so the app can exit at guide root`() {
        assertNull(at(GuideLayer.Grid, GuideKey.BACK))
    }

    @Test
    fun `the groups column closes on back or right, per capture 25`() {
        assertEquals(GuideCommand.CloseLayer, at(GuideLayer.Groups, GuideKey.BACK))
        assertEquals(GuideCommand.CloseLayer, at(GuideLayer.Groups, GuideKey.RIGHT))
        assertNull(at(GuideLayer.Groups, GuideKey.UP))
        assertNull(at(GuideLayer.Groups, GuideKey.OK))
    }

    @Test
    fun `long-ok and menu open the row context sheet from the grid`() {
        assertEquals(GuideCommand.OpenRowMenu, at(GuideLayer.Grid, GuideKey.LONG_OK))
        assertEquals(GuideCommand.OpenRowMenu, at(GuideLayer.Grid, GuideKey.MENU))
    }

    @Test
    fun `the dropdown and paywall only close on back`() {
        val menu = GuideLayer.CellMenu(cell(at(15, 45), at(17, 0)))
        assertEquals(GuideCommand.CloseLayer, at(menu, GuideKey.BACK))
        assertNull(at(menu, GuideKey.RIGHT))
        assertEquals(GuideCommand.CloseLayer, at(GuideLayer.Paywall("Remind"), GuideKey.BACK))
        assertNull(at(GuideLayer.Paywall("Remind"), GuideKey.OK))
    }

    @Test
    fun `the row sheet and its pushed screens only close on back`() {
        val layers =
            listOf(
                GuideLayer.RowMenu,
                GuideLayer.ComingSoon("Assign EPG"),
                GuideLayer.Description("Title", "Text"),
                GuideLayer.ChannelOptions("News One"),
            )
        layers.forEach { layer ->
            assertEquals(GuideCommand.CloseLayer, at(layer, GuideKey.BACK))
            assertNull(at(layer, GuideKey.RIGHT))
            assertNull(at(layer, GuideKey.OK))
            assertNull(at(layer, GuideKey.MENU))
        }
    }
}
