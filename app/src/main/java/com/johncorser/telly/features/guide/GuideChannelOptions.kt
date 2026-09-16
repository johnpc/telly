package com.johncorser.telly.features.guide

import com.johncorser.telly.features.playlist.db.ChannelEntity
import com.johncorser.telly.features.playlist.db.displayName
import com.johncorser.telly.features.settings.SettingsRow

/**
 * Rows of the "Channel options" sub-pane (captures 41-42), all live:
 * rename/restore, the bulk names editor, per-channel decoder /
 * external-player / EPG-offset overrides, plus the sheet's Block and Hide
 * flows verbatim. Summaries render the CURRENT per-channel state.
 */
object GuideChannelOptions {
    const val NAME = "channel_options.name"
    const val RESTORE_NAME = "channel_options.restore_name"
    const val NAMES_EDITOR = "channel_options.names_editor"
    const val AUDIO_DECODER = "channel_options.audio_decoder"
    const val VIDEO_DECODER = "channel_options.video_decoder"
    const val EXTERNAL_PLAYER = "channel_options.external_player"
    const val EPG_OFFSET = "channel_options.epg_offset"
    const val BLOCK = "channel_options.block"
    const val HIDE = "channel_options.hide"

    fun rows(
        channel: ChannelEntity,
        externalDefault: Boolean,
    ): List<SettingsRow> =
        listOfNotNull(
            SettingsRow.Value(NAME, "Channel name", channel.displayName),
            restoreRow(channel),
            SettingsRow.Value(NAMES_EDITOR, "Channel names editor", null),
            SettingsRow.Value(
                AUDIO_DECODER,
                "Audio decoder",
                ChannelOptionsValues.decoderLabel(channel.overrides.audioDecoder),
            ),
            SettingsRow.Value(
                VIDEO_DECODER,
                "Video decoder",
                ChannelOptionsValues.decoderLabel(channel.overrides.videoDecoder),
            ),
            SettingsRow.Toggle(
                EXTERNAL_PLAYER,
                "Use external player",
                checked = ChannelOptionsValues.externalEffective(channel, externalDefault),
            ),
            SettingsRow.Value(
                EPG_OFFSET,
                "EPG time offset, h:min",
                ChannelOptionsValues.offsetLabel(channel.overrides.epgOffsetMinutes),
            ),
            SettingsRow.Action(BLOCK, if (channel.flags.blocked) "Unblock channel" else "Block channel"),
            SettingsRow.Action(HIDE, "Hide channel"),
        )

    /** "Restore channel name" appears only while a custom name is set. */
    private fun restoreRow(channel: ChannelEntity): SettingsRow? =
        channel.overrides.customName?.takeIf { it.isNotBlank() }?.let {
            SettingsRow.Value(RESTORE_NAME, "Restore channel name", channel.source.name)
        }
}
