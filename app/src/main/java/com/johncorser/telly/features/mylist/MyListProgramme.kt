package com.johncorser.telly.features.mylist

import com.johncorser.telly.features.epg.ProgramTitle
import com.johncorser.telly.features.epg.db.ProgramEntity
import com.johncorser.telly.features.panel.PanelRow
import com.johncorser.telly.features.playlist.ChannelImporter
import com.johncorser.telly.features.playlist.db.ChannelEntity

/**
 * The programme a My-list toggle acts on, host-agnostic: the guide feeds it
 * from the focused cell's [ProgramEntity], the panel from its row's airing
 * programme (which carries no entity, only pre-resolved fields).
 */
data class MyListProgramme(
    val title: String,
    val startMs: Long,
    val endMs: Long,
    val description: String?,
) {
    companion object {
        fun of(program: ProgramEntity): MyListProgramme =
            MyListProgramme(
                title = ProgramTitle.of(program.details),
                startMs = program.startMs,
                endMs = program.endMs,
                description = program.details.description,
            )

        /** The panel row's airing programme; null when the EPG has a gap. */
        fun of(row: PanelRow): MyListProgramme? {
            val title = row.nowTitle ?: return null
            val start = row.nowStartMs ?: return null
            val end = row.nowEndMs ?: return null
            return MyListProgramme(title = title, startMs = start, endMs = end, description = row.description)
        }
    }
}

/** Identity of a saved programme: stable channel key + airing start. */
object MyListKeys {
    const val ADD_LABEL = "Add to My list"
    const val REMOVE_LABEL = "Remove from My list"

    /** Playlist-refresh-stable channel identity, same key as user flags. */
    fun channelKeyOf(channel: ChannelEntity): String =
        ChannelImporter.identityOf(channel.source.tvgId, channel.source.streamUrl, channel.source.name)

    fun of(
        channelKey: String,
        startMs: Long,
    ): String = "$channelKey|$startMs"

    fun of(
        channel: ChannelEntity,
        startMs: Long,
    ): String = of(channelKeyOf(channel), startMs)

    /** Whether the programme starting at [startMs] on [channel] is saved. */
    fun saved(
        keys: Set<String>,
        channel: ChannelEntity,
        startMs: Long?,
    ): Boolean = startMs != null && of(channel, startMs) in keys

    /** The dropdown/sheet row label, flipped once the programme is saved. */
    fun label(saved: Boolean): String = if (saved) REMOVE_LABEL else ADD_LABEL
}
