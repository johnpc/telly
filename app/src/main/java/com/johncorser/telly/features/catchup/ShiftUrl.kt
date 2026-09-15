package com.johncorser.telly.features.catchup

/**
 * `catchup="shift"`: the conventional timeshift form — the live stream URL
 * with `utc` (programme start) and `lutc` (request time) query parameters
 * appended, both in epoch seconds.
 */
object ShiftUrl {
    fun build(
        streamUrl: String,
        startMs: Long,
        nowMs: Long,
    ): String {
        val separator = if ('?' in streamUrl) '&' else '?'
        return "$streamUrl${separator}utc=${CatchupTemplate.seconds(startMs)}&lutc=${CatchupTemplate.seconds(nowMs)}"
    }
}
