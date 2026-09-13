package com.johncorser.telly.features.playback

import com.johncorser.telly.features.epg.NowNext
import com.johncorser.telly.features.epg.db.ProgramDetails
import com.johncorser.telly.features.player.VideoDetails
import com.johncorser.telly.features.playlist.db.ChannelEntity
import java.util.TimeZone

/** Everything the bottom info overlay renders, pre-formatted (capture 34). */
data class PlaybackInfoData(
    val number: Int,
    val name: String,
    val logoUrl: String?,
    val group: String?,
    val clockText: String,
    val title: String?,
    val timeRange: String?,
    val remaining: String?,
    val progressPermille: Int,
    val nextLine: String?,
    val badges: List<String>,
)

object PlaybackInfoBuilder {
    fun build(
        channel: ChannelEntity,
        nowNext: NowNext,
        video: VideoDetails?,
        atMs: Long,
        zone: TimeZone,
    ): PlaybackInfoData {
        val now = nowNext.now
        val next = nowNext.next
        return PlaybackInfoData(
            number = channel.number,
            name = channel.source.name,
            logoUrl = channel.source.logoUrl,
            group = channel.source.groupTitle,
            clockText = ProgramTimes.clock(atMs, zone),
            title = now?.details?.let(::displayTitle),
            timeRange = now?.let { ProgramTimes.range(it.startMs, it.endMs, zone) },
            remaining = now?.let { "${ProgramTimes.remainingMinutes(it.endMs, atMs)} min" },
            progressPermille = now?.let { ProgramTimes.progressPermille(it.startMs, it.endMs, atMs) } ?: 0,
            nextLine = next?.let { "${ProgramTimes.range(it.startMs, it.endMs, zone)}  ${displayTitle(it.details)}" },
            badges = PlaybackBadges.badges(video),
        )
    }

    /** TiviMate renders "Title. S1 E7" when an episode number is known. */
    fun displayTitle(details: ProgramDetails): String = listOfNotNull(details.title, details.episode).joinToString(". ")
}
