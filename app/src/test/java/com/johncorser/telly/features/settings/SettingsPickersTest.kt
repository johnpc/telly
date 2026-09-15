package com.johncorser.telly.features.settings

import com.johncorser.telly.core.settings.TellySettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsPickersTest {
    @Test
    fun `interval picker offers None first with the captured default`() {
        val spec = SettingsPickers.byRowId.getValue(RowIds.EPG_UPDATE_INTERVAL)
        assertEquals("Update interval, hours", spec.title)
        assertEquals("None", spec.options.first().label)
        assertEquals("0", spec.options.first().raw)
        assertEquals(listOf(0, 1, 2, 3, 6, 12, 24), SettingsPickers.EPG_INTERVAL_HOURS)
    }

    @Test
    fun `past days picker covers one to seven`() {
        val spec = SettingsPickers.byRowId.getValue(RowIds.EPG_PAST_DAYS)
        assertEquals((1..7).map { it.toString() }, spec.options.map { it.raw })
    }

    @Test
    fun `color theme picker renders Dark-dot-accent labels over accent names`() {
        val spec = SettingsPickers.byRowId.getValue(RowIds.APPEARANCE_COLOR_THEME)
        assertEquals("Dark  •  Blue", spec.options.first().label)
        assertEquals("Blue", spec.options.first().raw)
    }

    @Test
    fun `currentRaw prefers the stored value and falls back to the default`() {
        val spec = SettingsPickers.byRowId.getValue(RowIds.EPG_PAST_DAYS)
        assertEquals("7", SettingsPickers.currentRaw(spec, emptyMap()))
        assertEquals("3", SettingsPickers.currentRaw(spec, mapOf(spec.key to "3")))
    }

    @Test
    fun `every picker default raw is one of its options`() {
        SettingsPickers.byRowId.values.forEach { spec ->
            val default = SettingsPickers.currentRaw(spec, emptyMap())
            assertTrue("${spec.rowId} default $default", spec.options.any { it.raw == default })
        }
    }

    @Test
    fun `toggle bindings target the expected keys`() {
        assertEquals(TellySettings.CONFIRM_EXIT, SettingsToggles.byRowId[RowIds.CONFIRM_EXIT])
        assertEquals(TellySettings.SEND_STATISTICS, SettingsToggles.byRowId[RowIds.ABOUT_STATISTICS])
        assertEquals(TellySettings.SEEK_RWFF_CATCHUP, SettingsToggles.byRowId[RowIds.REMOTE_SEEK_RWFF])
        assertEquals(TellySettings.VOD_REMEMBER_POSITION, SettingsToggles.byRowId[RowIds.VOD_REMEMBER_POSITION])
        assertEquals(27, SettingsToggles.byRowId.size)
    }

    @Test
    fun `the remote key pickers offer the keymap labels with current-map defaults`() {
        val ok = SettingsPickers.byRowId.getValue(RowIds.REMOTE_PLAYER_OK)
        assertEquals("OK button", ok.title)
        assertEquals(listOf("Show info panel", "Open channels list", "Nothing"), ok.options.map { it.label })
        assertEquals("Show info panel", SettingsPickers.currentRaw(ok, emptyMap()))
        // Labels persist raw, so the key policies parse exactly what is shown.
        ok.options.forEach { assertEquals(it.label, it.raw) }

        val upDown = SettingsPickers.byRowId.getValue(RowIds.REMOTE_PLAYER_UP_DOWN)
        assertEquals(listOf("Show info panel", "Switch channels", "Nothing"), upDown.options.map { it.raw })
        // No seek plumbing yet: Left/Right offers only remaps telly honors.
        val leftRight = SettingsPickers.byRowId.getValue(RowIds.REMOTE_PLAYER_LEFT_RIGHT)
        assertEquals(listOf("Nothing", "Switch channels"), leftRight.options.map { it.raw })
        val longOk = SettingsPickers.byRowId.getValue(RowIds.REMOTE_PLAYER_LONG_OK)
        assertEquals(listOf("Open quick menu", "Open channels list"), longOk.options.map { it.raw })
    }

    @Test
    fun `the guide key pickers offer the keymap labels with current-map defaults`() {
        val leftRight = SettingsPickers.byRowId.getValue(RowIds.REMOTE_GUIDE_LEFT_RIGHT)
        assertEquals(listOf("Move by programme", "Move by page"), leftRight.options.map { it.raw })
        assertEquals("Move by programme", SettingsPickers.currentRaw(leftRight, emptyMap()))

        val channels = SettingsPickers.byRowId.getValue(RowIds.REMOTE_GUIDE_CHANNEL_UP_DOWN)
        assertEquals(listOf("Nothing", "Page the channel list", "Move by day"), channels.options.map { it.raw })
        assertEquals("Nothing", SettingsPickers.currentRaw(channels, emptyMap()))

        val longOk = SettingsPickers.byRowId.getValue(RowIds.REMOTE_GUIDE_LONG_OK)
        assertEquals(listOf("Open channel menu", "Play channel"), longOk.options.map { it.raw })
        assertEquals("Open channel menu", SettingsPickers.currentRaw(longOk, emptyMap()))
    }

    @Test
    fun `interval labels map zero to None`() {
        assertEquals("None", SettingsPickers.intervalLabel(0))
        assertEquals("12", SettingsPickers.intervalLabel(12))
    }
}
