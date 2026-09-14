package com.johncorser.telly.features.search

import com.johncorser.telly.features.epg.db.ProgramEntity
import com.johncorser.telly.features.playlist.db.ChannelEntity

/**
 * One card of the Channels shelf (capture 50): logo tile, channel name,
 * airing programme title in accent blue and a thin progress line.
 */
data class SearchChannelHit(
    val channel: ChannelEntity,
    val nowTitle: String?,
    val progressPermille: Int,
)

/**
 * One row of the Programs list (captures 50/51): the programme, the channel
 * it airs on, its formatted air time, and whether this row starts a new
 * same-channel run (the reference renders the channel card once per run).
 * Currently-airing rows also carry the dash progress + "50 min" remaining
 * shown after the times (live tm-03); both stay empty for upcoming rows.
 */
data class SearchProgramHit(
    val program: ProgramEntity,
    val channel: ChannelEntity,
    val title: String,
    val timeText: String,
    val progressPermille: Int,
    val remaining: String?,
    val showsChannelCard: Boolean,
)

/** Everything the typed state renders; empty query = empty shelves. */
data class SearchResults(
    val query: String = "",
    val channels: List<SearchChannelHit> = emptyList(),
    val programs: List<SearchProgramHit> = emptyList(),
) {
    val isEmpty: Boolean get() = channels.isEmpty() && programs.isEmpty()
}
