package com.johncorser.telly.features.player.skip

import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.core.settings.TellySettings

/**
 * Reader for Settings -> Playback -> "Skip steps": parses the persisted
 * preset ("10s / 30s / 1m / 5m") into the ordered skip lengths in millis.
 * Seeking features (catch-up/VOD transport, D-pad skipping) consume
 * [of]/[parse]; the settings picker consumes [PRESETS].
 */
object SkipSteps {
    /** The default preset (mirrors TellySettings.SKIP_STEPS's default). */
    val DEFAULT_RAW: String = TellySettings.SKIP_STEPS.default

    /** The picker's step sets (the reference locks the row; sets designed). */
    val PRESETS: List<String> =
        listOf(
            DEFAULT_RAW,
            "5s / 15s / 30s / 1m",
            "30s / 1m / 5m / 10m",
            "1m / 5m / 15m / 30m",
        )

    private val token = Regex("""(\d+)\s*([smh])""")
    private val unitMs = mapOf("s" to 1_000L, "m" to 60_000L, "h" to 3_600_000L)

    /**
     * Ordered steps in ms. Malformed tokens are dropped; input with no
     * usable token at all falls back to the default preset.
     */
    fun parse(raw: String): List<Long> {
        val steps =
            token
                .findAll(raw)
                .map { match -> match.groupValues[1].toLong() * unitMs.getValue(match.groupValues[2]) }
                .toList()
        return steps.ifEmpty { parse(DEFAULT_RAW) }
    }

    /** The active steps for the persisted setting. */
    fun of(settings: SettingsRepository): List<Long> = parse(settings.get(TellySettings.SKIP_STEPS))
}
