package com.johncorser.telly.features.guide

import com.johncorser.telly.features.epg.db.ProgramDetails
import com.johncorser.telly.features.epg.db.ProgramEntity
import java.util.GregorianCalendar
import java.util.TimeZone

/** Shared fixed-clock helpers for the guide engine tests (2026-09-13 UTC). */
object GuideTestData {
    val utc: TimeZone = TimeZone.getTimeZone("UTC")

    fun at(
        hour: Int,
        minute: Int,
        dayOffset: Int = 0,
    ): Long =
        GregorianCalendar(utc)
            .apply {
                set(2026, 8, 13 + dayOffset, hour, minute, 0)
                set(GregorianCalendar.MILLISECOND, 0)
            }.timeInMillis

    /** 14:38 — the sampled "now" of most tests; origin floors to 14:30. */
    val nowMs: Long = at(14, 38)
    val originMs: Long = at(14, 30)

    fun cell(
        startMs: Long,
        endMs: Long,
        title: String? = "Programme",
    ): GuideCell =
        GuideCell(
            startMs = startMs,
            endMs = endMs,
            program =
                title?.let {
                    ProgramEntity(
                        channelTvgId = "tvg",
                        startMs = startMs,
                        endMs = endMs,
                        details = ProgramDetails(title = it),
                    )
                },
        )
}
