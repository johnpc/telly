package com.johncorser.telly.features.epg

import java.util.GregorianCalendar
import java.util.TimeZone

/**
 * Parses XMLTV timestamps such as `20260913123000 +0000` into epoch millis.
 * Deliberately epoch-Long based: java.time needs API 26 / desugaring at our
 * minSdk 23 (see CLAUDE.md decisions), while GregorianCalendar is everywhere.
 */
object XmltvTimestamp {
    private val utc = TimeZone.getTimeZone("UTC")
    private val timestampPattern = Regex("""(\d{8})(\d{6})(?:\s*([+-]\d{4}))?""")
    private val datePattern = Regex("""(\d{4})(\d{2})(\d{2})""")
    private val timePattern = Regex("""(\d{2})(\d{2})(\d{2})""")
    private val offsetPattern = Regex("""([+-])(\d{2})(\d{2})""")
    private const val MS_PER_MINUTE = 60_000L
    private const val MINUTES_PER_HOUR = 60L

    /** Epoch millis for [raw], or null when it is not a full XMLTV timestamp. */
    fun parseMs(raw: String?): Long? {
        val match = timestampPattern.matchEntire(raw?.trim().orEmpty()) ?: return null
        val (date, time, offset) = match.destructured
        return utcMs(date, time) - offsetMs(offset)
    }

    private fun utcMs(
        date: String,
        time: String,
    ): Long {
        val (year, month, day) = datePattern.matchEntire(date)!!.destructured
        val (hour, minute, second) = timePattern.matchEntire(time)!!.destructured
        val calendar = GregorianCalendar(utc)
        calendar.clear()
        calendar.set(
            year.toInt(),
            month.toInt() - 1,
            day.toInt(),
            hour.toInt(),
            minute.toInt(),
            second.toInt(),
        )
        return calendar.timeInMillis
    }

    /** Millis east of UTC for `+HHMM`/`-HHMM`; 0 for a missing offset. */
    private fun offsetMs(offset: String): Long {
        val match = offsetPattern.matchEntire(offset) ?: return 0L
        val (sign, hours, minutes) = match.destructured
        val magnitude = (hours.toLong() * MINUTES_PER_HOUR + minutes.toLong()) * MS_PER_MINUTE
        return if (sign == "-") -magnitude else magnitude
    }
}
