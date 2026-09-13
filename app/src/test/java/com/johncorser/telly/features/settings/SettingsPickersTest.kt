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
        assertEquals(21, SettingsToggles.byRowId.size)
    }

    @Test
    fun `interval labels map zero to None`() {
        assertEquals("None", SettingsPickers.intervalLabel(0))
        assertEquals("12", SettingsPickers.intervalLabel(12))
    }
}
