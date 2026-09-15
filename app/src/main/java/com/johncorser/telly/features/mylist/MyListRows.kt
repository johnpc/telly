package com.johncorser.telly.features.mylist

import com.johncorser.telly.features.mylist.db.MyListEntity
import com.johncorser.telly.features.playback.ProgramTimes
import com.johncorser.telly.features.playlist.db.ChannelEntity
import java.util.TimeZone

/** One My List row: the saved programme plus its resolved channel. */
data class MyListRow(
    val entry: MyListEntity,
    val channel: ChannelEntity,
    /** "Sun, Sep 13, 2:45 PM" air-time stamp (the shared clock format). */
    val airTimeText: String,
    /** Airing now: OK tunes; a future entry shows its description instead. */
    val airing: Boolean,
)

/**
 * Pure assembly for the My List screen: entries come most-recently-added
 * first from the store, keys resolve to channels via the refresh-stable
 * identity (keys without a visible channel drop, the History precedent),
 * and entries whose stop time has passed auto-hide.
 */
object MyListRows {
    fun rows(
        entries: List<MyListEntity>,
        channels: List<ChannelEntity>,
        nowMs: Long,
        zone: TimeZone,
    ): List<MyListRow> {
        val byKey = channels.associateBy(MyListKeys::channelKeyOf)
        return entries
            .filter { it.endMs > nowMs }
            .mapNotNull { entry ->
                byKey[entry.channelKey]?.let { channel ->
                    MyListRow(
                        entry = entry,
                        channel = channel,
                        airTimeText = ProgramTimes.clock(entry.startMs, zone),
                        airing = entry.startMs <= nowMs,
                    )
                }
            }
    }
}
