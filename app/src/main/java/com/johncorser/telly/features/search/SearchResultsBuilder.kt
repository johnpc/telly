package com.johncorser.telly.features.search

import com.johncorser.telly.features.epg.NowNext
import com.johncorser.telly.features.epg.ProgramTitle
import com.johncorser.telly.features.epg.db.ProgramEntity
import com.johncorser.telly.features.playback.ProgramTimes
import com.johncorser.telly.features.playlist.db.ChannelEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Pure assembly of the search shelves from DAO rows (captures 50/51):
 * channel cards keep the DAO's name order and carry their airing programme;
 * programme rows are chronological, drop hidden/unknown channels, and share
 * one channel card per consecutive same-channel run.
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

    fun programs(
        matches: List<ProgramEntity>,
        channels: List<ChannelEntity>,
        atMs: Long,
        zone: TimeZone,
    ): List<SearchProgramHit> {
        val byTvgId = channelByTvgId(channels)
        var previousTvgId: String? = null
        return matches.mapNotNull { program ->
            val channel = byTvgId[program.channelTvgId] ?: return@mapNotNull null
            hit(program, channel, atMs, zone, showsCard = program.channelTvgId != previousTvgId)
                .also { previousTvgId = program.channelTvgId }
        }
    }

    /** Airing rows add dash progress + remaining minutes (live tm-03). */
    private fun hit(
        program: ProgramEntity,
        channel: ChannelEntity,
        atMs: Long,
        zone: TimeZone,
        showsCard: Boolean,
    ): SearchProgramHit {
        val airing = program.startMs <= atMs
        return SearchProgramHit(
            program = program,
            channel = channel,
            title = ProgramTitle.of(program.details),
            timeText = airTime(program, atMs, zone),
            progressPermille = if (airing) ProgramTimes.progressPermille(program.startMs, program.endMs, atMs) else 0,
            remaining = if (airing) "${ProgramTimes.remainingMinutes(program.endMs, atMs)} min" else null,
            showsChannelCard = showsCard,
        )
    }

    /**
     * "03:45 — 05:15 PM" for programmes starting today; other days carry the
     * reference date prefix, "Mon, Sep 14, 12:45 — 01:45 AM" (capture 50).
     */
    fun airTime(
        program: ProgramEntity,
        atMs: Long,
        zone: TimeZone,
    ): String {
        val range = ProgramTimes.range(program.startMs, program.endMs, zone)
        if (localDay(program.startMs, zone) == localDay(atMs, zone)) return range
        return "${format("EEE, MMM d", program.startMs, zone)}, $range"
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
