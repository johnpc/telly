package com.johncorser.telly.features.history

import com.johncorser.telly.features.epg.ProgramTitle
import com.johncorser.telly.features.epg.db.ProgramEntity
import com.johncorser.telly.features.history.db.WatchHistoryEntity
import com.johncorser.telly.features.playback.ClockStyle
import com.johncorser.telly.features.playback.ProgramTimes
import com.johncorser.telly.features.playlist.db.ChannelEntity

/** One History-screen row: the channel + what/when it was last watched. */
data class HistoryRow(
    val channel: ChannelEntity,
    /** "Sun, Sep 13, 2:45 PM" — the shared TiviMate clock format. */
    val watchedText: String,
    /** The programme airing when the channel was last watched, if the EPG knows. */
    val programmeTitle: String?,
)

/**
 * Pure assembly for both history surfaces (history-round2): the info
 * overlay's recent-channel cards row and the History screen's rows. Events
 * come newest-first from the DAO; keys resolve to channels via the
 * playlist-refresh-stable identity, and keys without a visible channel drop.
 */
object HistoryRows {
    /** The recent-cards row: newest-first, minus the channel already playing. */
    fun recentChannels(
        events: List<WatchHistoryEntity>,
        channels: List<ChannelEntity>,
        currentChannelId: Long?,
    ): List<ChannelEntity> = channelsOf(events, channels).filter { it.id != currentChannelId }

    /** The History screen's rows: the full capped list, newest first. */
    fun rows(
        events: List<WatchHistoryEntity>,
        channels: List<ChannelEntity>,
        programs: List<ProgramEntity>,
        style: ClockStyle,
    ): List<HistoryRow> {
        val byKey = channels.associateBy(WatchHistory::identityOf)
        return events.mapNotNull { event ->
            byKey[event.channelKey]?.let { channel ->
                HistoryRow(
                    channel = channel,
                    watchedText = ProgramTimes.clock(event.watchedAtMs, style),
                    programmeTitle = titleAiringOn(channel, event.watchedAtMs, programs),
                )
            }
        }
    }

    /** The span the History screen must observe to resolve [rows]' programmes. */
    fun programmeSpan(events: List<WatchHistoryEntity>): LongRange =
        (events.minOfOrNull { it.watchedAtMs } ?: 0L)..(events.maxOfOrNull { it.watchedAtMs }?.plus(1) ?: 1L)

    private fun channelsOf(
        events: List<WatchHistoryEntity>,
        channels: List<ChannelEntity>,
    ): List<ChannelEntity> {
        val byKey = channels.associateBy(WatchHistory::identityOf)
        return events.mapNotNull { byKey[it.channelKey] }
    }

    private fun titleAiringOn(
        channel: ChannelEntity,
        atMs: Long,
        programs: List<ProgramEntity>,
    ): String? =
        programs
            .firstOrNull { it.channelTvgId == channel.source.tvgId && it.startMs <= atMs && atMs < it.endMs }
            ?.details
            ?.let(ProgramTitle::of)
}
