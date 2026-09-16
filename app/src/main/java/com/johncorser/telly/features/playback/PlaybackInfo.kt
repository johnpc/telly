package com.johncorser.telly.features.playback

import com.johncorser.telly.features.epg.NowNext
import com.johncorser.telly.features.epg.ProgramTitle
import com.johncorser.telly.features.player.VideoDetails
import com.johncorser.telly.features.playlist.db.ChannelEntity
import com.johncorser.telly.features.playlist.db.displayName
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
    val description: String?,
    val nextLine: String?,
    val badges: List<String>,
    val elapsed: String?,
    val duration: String?,
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
            name = channel.displayName,
            logoUrl = channel.source.logoUrl,
            group = channel.source.groupTitle,
            clockText = ProgramTimes.clock(atMs, zone),
            title = now?.details?.let(ProgramTitle::of),
            timeRange = now?.let { ProgramTimes.range(it.startMs, it.endMs, zone) },
            remaining = now?.let { "${ProgramTimes.remainingMinutes(it.endMs, atMs)} min" },
            progressPermille = now?.let { ProgramTimes.progressPermille(it.startMs, it.endMs, atMs) } ?: 0,
            description = now?.details?.description,
            nextLine =
                next?.let {
                    "${ProgramTimes.range(it.startMs, it.endMs, zone)}  ${ProgramTitle.of(it.details)}"
                },
            badges = PlaybackBadges.badges(video),
            elapsed = now?.let { ProgramTimes.span(atMs - it.startMs) },
            duration = now?.let { ProgramTimes.span(it.endMs - it.startMs) },
        )
    }
}
