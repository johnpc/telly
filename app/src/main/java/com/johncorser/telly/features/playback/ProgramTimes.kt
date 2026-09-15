package com.johncorser.telly.features.playback

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * TiviMate-style 12-hour time strings (captures 34/47): programme ranges
 * render as "02:30 — 03:45 PM" (start meridiem only when it differs) and the
 * overlay clock as "Sun, Sep 13, 2:45 PM". Epoch-millis in, strings out; the
 * zone is injected so tests stay deterministic.
 */
object ProgramTimes {
    private const val MINUTE_MS = 60_000L
    private const val SECOND_MS = 1_000L
    private const val SECONDS_PER_MINUTE = 60L
    private const val MINUTES_PER_HOUR = 60L
    private const val SECONDS_PER_HOUR = 3_600L
    const val PERMILLE = 1000

    fun range(
        startMs: Long,
        endMs: Long,
        zone: TimeZone,
    ): String {
        val start = format("hh:mm", startMs, zone)
        val startMeridiem = format("a", startMs, zone)
        val end = format("hh:mm a", endMs, zone)
        val prefix = if (startMeridiem == format("a", endMs, zone)) start else "$start $startMeridiem"
        return "$prefix — $end"
    }

    fun clock(
        atMs: Long,
        zone: TimeZone,
    ): String = format("EEE, MMM d, h:mm a", atMs, zone)

    /** Bare start stamp "12:45 AM" (multiview picker schedule rows, capture 05). */
    fun startTime(
        atMs: Long,
        zone: TimeZone,
    ): String = format("hh:mm a", atMs, zone)

    /** Transport-row span "00:16" / "45:00" / "1:15:00" (round3-ref 03b). */
    fun span(ms: Long): String {
        val totalSeconds = ms.coerceAtLeast(0) / SECOND_MS
        val minutes = (totalSeconds / SECONDS_PER_MINUTE) % MINUTES_PER_HOUR
        val seconds = totalSeconds % SECONDS_PER_MINUTE
        val hours = totalSeconds / SECONDS_PER_HOUR
        val minSec = "%02d:%02d".format(minutes, seconds)
        return if (hours > 0) "$hours:$minSec" else minSec
    }

    /** Minutes left in the programme, rounded up ("61 min" in capture 34). */
    fun remainingMinutes(
        endMs: Long,
        atMs: Long,
    ): Long = ((endMs - atMs).coerceAtLeast(0) + MINUTE_MS - 1) / MINUTE_MS

    fun progressPermille(
        startMs: Long,
        endMs: Long,
        atMs: Long,
    ): Int {
        if (endMs <= startMs) return 0
        val fraction = (atMs - startMs).toDouble() / (endMs - startMs)
        return (fraction * PERMILLE).toInt().coerceIn(0, PERMILLE)
    }

    private fun format(
        pattern: String,
        atMs: Long,
        zone: TimeZone,
    ): String = SimpleDateFormat(pattern, Locale.US).apply { timeZone = zone }.format(Date(atMs))
}
