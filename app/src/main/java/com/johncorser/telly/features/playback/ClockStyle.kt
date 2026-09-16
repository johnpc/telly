package com.johncorser.telly.features.playback

import java.util.TimeZone

/**
 * How clock/time strings render: the injected zone plus the persisted
 * 12/24-hour choice (the store-only CLOCK_FORMAT key, captured default
 * "12-hour"). The flag is a provider so already-composed screens honor a
 * settings change on their next render without rebuilding.
 */
class ClockStyle(
    val zone: TimeZone = TimeZone.getDefault(),
    private val h24: () -> Boolean = { false },
) {
    val is24h: Boolean get() = h24()

    companion object {
        /** True for the "24-hour" raw setting value; anything else is 12-hour. */
        fun is24Raw(raw: String): Boolean = raw.trim().startsWith("24")
    }
}
