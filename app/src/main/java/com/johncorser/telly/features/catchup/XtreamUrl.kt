package com.johncorser.telly.features.catchup

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * `catchup="xc"` (Xtream Codes): a live URL of the conventional
 * `http(s)://host[:port][/live]/user/pass/id[.ext]` shape becomes the
 * standard timeshift endpoint
 * `http(s)://host[:port]/timeshift/user/pass/{durationMinutes}/{yyyy-MM-dd:HH-mm}/{id}.ts`.
 */
object XtreamUrl {
    private const val MINUTE_MS = 60_000L
    private val liveUrl = Regex("^(https?://[^/]+)(?:/live)?/([^/?]+/[^/?]+)/(\\d+)(?:\\.\\w+)?(?:\\?.*)?$")

    /** Null when the live URL is not the conventional Xtream Codes shape. */
    fun build(
        streamUrl: String,
        startMs: Long,
        endMs: Long,
    ): String? {
        val match = liveUrl.matchEntire(streamUrl) ?: return null
        val (host, credentials, id) = match.destructured
        val minutes = ((endMs - startMs) + MINUTE_MS - 1) / MINUTE_MS
        return "$host/timeshift/$credentials/$minutes/${stamp(startMs)}/$id.ts"
    }

    private fun stamp(atMs: Long): String =
        SimpleDateFormat("yyyy-MM-dd:HH-mm", Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(Date(atMs))
}
