package com.johncorser.telly.features.guide

import com.johncorser.telly.features.settings.SettingsRow
import com.johncorser.telly.features.settings.panePrelude

/**
 * Rows of the "Channel options" sub-pane, verbatim from captures 41-42:
 * premium note + Unlock Premium on top, then every captured row locked
 * (dimmed padlock, skipped by focus) exactly like the free reference.
 */
object GuideChannelOptions {
    fun rows(channelName: String): List<SettingsRow> =
        panePrelude() +
            listOf(
                locked("name", "Channel name", channelName),
                locked("restore_name", "Restore channel name", channelName),
                locked("names_editor", "Channel names editor", "Off"),
                locked("audio_decoder", "Audio decoder", "Hardware"),
                locked("video_decoder", "Video decoder", "Hardware"),
                locked("external_player", "Use external player", "No"),
                locked("epg_offset", "EPG time offset, h:min", "0:00"),
                locked("block", "Block channel", null),
                locked("hide", "Hide channel", null),
            )

    private fun locked(
        id: String,
        title: String,
        summary: String?,
    ): SettingsRow = SettingsRow.Value(id = "channel_options.$id", title = title, summary = summary, locked = true)
}
