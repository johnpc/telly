package com.johncorser.telly.features.search

import com.johncorser.telly.features.epg.NowNext
import com.johncorser.telly.features.epg.ProgramTitle
import com.johncorser.telly.features.epg.db.ProgramEntity
import com.johncorser.telly.features.playback.ClockStyle
import com.johncorser.telly.features.playback.ProgramTimes
import com.johncorser.telly.features.playlist.db.ChannelEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Pure assembly of the search shelves from DAO rows (captures 50/51 +
 * ref-round6 §D): channel cards keep the DAO's name order and carry their
 * airing programme; the Programs section groups programme matches into one
 * master entry per channel (case-insensitive name order) whose airings stay
 * chronological, one per airing, never deduped or merged across channels.
 */
object SearchResultsBuilder {
    fun channels(
        matches: List<ChannelEntity>,
        guide: Map<String, NowNext>,
        atMs: Long,
    ): List<SearchChannelHit> =
        matches.map { channel ->
            val now = guide[channel.source.tvgId]?.now
            SearchChannelHit(
                channel = channel,
                nowTitle = now?.details?.let(ProgramTitle::of),
                progressPermille = now?.let { ProgramTimes.progressPermille(it.startMs, it.endMs, atMs) } ?: 0,
            )
        }

    /**
     * The Programs master lane (ref-round6 §D): one entry per channel with a
     * matching programme, ordered by channel name (case-insensitive, ties by
     * number), each carrying all of its airings chronologically. Hidden and
     * unknown channels drop out with their airings.
     */
    fun programs(
        matches: List<ProgramEntity>,
        channels: List<ChannelEntity>,
        atMs: Long,
        style: ClockStyle,
    ): List<SearchProgramChannel> {
        val byTvgId = channelByTvgId(channels)
        return matches
            .groupBy { it.channelTvgId }
            .mapNotNull { (tvgId, airings) -> byTvgId[tvgId]?.let { group(it, airings, atMs, style) } }
            .sortedWith(
                compareBy<SearchProgramChannel, String>(String.CASE_INSENSITIVE_ORDER) { it.channel.source.name }
                    .thenBy { it.channel.number },
            )
    }

    private fun group(
        channel: ChannelEntity,
        airings: List<ProgramEntity>,
        atMs: Long,
        style: ClockStyle,
    ): SearchProgramChannel =
        SearchProgramChannel(
            channel = channel,
            airings = airings.sortedBy { it.startMs }.map { hit(it, channel, atMs, style) },
        )

    /** Airing rows add dash progress + remaining minutes (live tm-03). */
    private fun hit(
        program: ProgramEntity,
        channel: ChannelEntity,
        atMs: Long,
        style: ClockStyle,
    ): SearchProgramHit {
        val airing = program.startMs <= atMs
        return SearchProgramHit(
            program = program,
            channel = channel,
            title = ProgramTitle.of(program.details),
            timeText = airTime(program, atMs, style),
            progressPermille = if (airing) ProgramTimes.progressPermille(program.startMs, program.endMs, atMs) else 0,
            remaining = if (airing) "${ProgramTimes.remainingMinutes(program.endMs, atMs)} min" else null,
        )
    }

    /**
     * "03:45 — 05:15 PM" for programmes starting today; other days carry the
     * reference date prefix, "Mon, Sep 14, 12:45 — 01:45 AM" (capture 50).
     */
    fun airTime(
        program: ProgramEntity,
        atMs: Long,
        style: ClockStyle,
    ): String {
        val range = ProgramTimes.range(program.startMs, program.endMs, style)
        if (localDay(program.startMs, style.zone) == localDay(atMs, style.zone)) return range
        return "${format("EEE, MMM d", program.startMs, style.zone)}, $range"
    }

    /** First visible channel wins when several share a tvg-id. */
    private fun channelByTvgId(channels: List<ChannelEntity>): Map<String, ChannelEntity> {
        val map = mutableMapOf<String, ChannelEntity>()
        for (channel in channels) {
            channel.source.tvgId?.let { map.putIfAbsent(it, channel) }
        }
        return map
    }

    private fun localDay(
        atMs: Long,
        zone: TimeZone,
    ): String = format("yyyy-DDD", atMs, zone)

    private fun format(
        pattern: String,
        atMs: Long,
        zone: TimeZone,
    ): String = SimpleDateFormat(pattern, Locale.US).apply { timeZone = zone }.format(Date(atMs))
}
