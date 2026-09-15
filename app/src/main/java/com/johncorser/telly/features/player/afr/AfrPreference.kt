package com.johncorser.telly.features.player.afr

/**
 * The "Auto frame rate (AFR)" picker variants (TellySettings
 * AUTO_FRAME_RATE; captured default Off). The reference sells AFR as a
 * premium picker whose exact option list is not capturable — telly ships
 * the documented shape: off, on, and on-with-restore-on-stop.
 */
enum class AfrPreference(
    val raw: String,
) {
    OFF("Off"),
    ON("On"),
    ON_RESTORE("On (also switch refresh rate on stop)"),
    ;

    val enabled: Boolean get() = this != OFF

    /** Only the third variant restores the original mode on stop/background. */
    val restoreOnStop: Boolean get() = this == ON_RESTORE

    companion object {
        val options: List<String> = entries.map { it.raw }

        fun fromRaw(raw: String): AfrPreference = entries.firstOrNull { it.raw == raw } ?: OFF
    }
}
