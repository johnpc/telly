package com.johncorser.telly.features.settings

import com.johncorser.telly.features.epg.EpgSource
import com.johncorser.telly.features.playlist.db.ChannelEntity

/** The live lists some panes render (EPG sources, blocked channels). */
data class SettingsRowSources(
    val epgSources: List<EpgSource> = emptyList(),
    val blockedChannels: List<ChannelEntity> = emptyList(),
)
