package com.johncorser.telly.features.player.skip

import com.johncorser.telly.core.settings.InMemoryKeyValueStore
import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.core.settings.TellySettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The Skip steps reader other slices (catch-up/VOD seeking) consume. */
class SkipStepsTest {
    @Test
    fun `the default preset parses to 10s 30s 1m 5m in millis`() {
        assertEquals(listOf(10_000L, 30_000L, 60_000L, 300_000L), SkipSteps.parse(SkipSteps.DEFAULT_RAW))
    }

    @Test
    fun `every picker preset parses to four ascending steps`() {
        SkipSteps.PRESETS.forEach { preset ->
            val steps = SkipSteps.parse(preset)
            assertEquals(preset, 4, steps.size)
            assertEquals(preset, steps.sorted(), steps)
        }
    }

    @Test
    fun `hours parse and malformed tokens drop`() {
        assertEquals(listOf(90_000L, 3_600_000L), SkipSteps.parse("90s / soon / 1h"))
    }

    @Test
    fun `unusable input falls back to the default preset`() {
        assertEquals(SkipSteps.parse(SkipSteps.DEFAULT_RAW), SkipSteps.parse(""))
        assertEquals(SkipSteps.parse(SkipSteps.DEFAULT_RAW), SkipSteps.parse("nonsense"))
    }

    @Test
    fun `of reads the persisted setting with the catalog default`() {
        val settings = SettingsRepository(InMemoryKeyValueStore())
        assertEquals(listOf(10_000L, 30_000L, 60_000L, 300_000L), SkipSteps.of(settings))

        settings.set(TellySettings.SKIP_STEPS, "30s / 1m / 5m / 10m")
        assertEquals(listOf(30_000L, 60_000L, 300_000L, 600_000L), SkipSteps.of(settings))
    }

    @Test
    fun `the default raw mirrors the TellySettings default`() {
        assertEquals(TellySettings.SKIP_STEPS.default, SkipSteps.DEFAULT_RAW)
        assertTrue(SkipSteps.PRESETS.contains(SkipSteps.DEFAULT_RAW))
    }
}
