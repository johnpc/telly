package com.johncorser.telly.features.guide

import com.johncorser.telly.features.playlist.db.ChannelEntity
import com.johncorser.telly.features.playlist.db.displayName
import com.johncorser.telly.features.settings.PickerOption
import com.johncorser.telly.features.settings.SettingsRow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The Channel-options pane's shared state machine (guide + playback hosts):
 * builds the live rows, opens/answers the rename and picker dialogs, and
 * hands Block / Hide / the names editor back to the host so those reuse the
 * exact sheet flows (PIN-gated block, zap-away hide, a pushed route).
 */
class ChannelOptionsController(
    val store: ChannelOptionsStore,
    private val externalDefault: () -> Boolean,
) {
    private val mutableDialog = MutableStateFlow<ChannelOptionsDialog?>(null)
    val dialog: StateFlow<ChannelOptionsDialog?> = mutableDialog.asStateFlow()

    fun rows(channel: ChannelEntity): List<SettingsRow> = GuideChannelOptions.rows(channel, externalDefault())

    /** A pane row activation; non-null = the host finishes the flow. */
    fun onRow(
        channel: ChannelEntity,
        rowId: String,
    ): ChannelOptionsEffect? =
        when (rowId) {
            GuideChannelOptions.NAME -> openRename(channel)
            GuideChannelOptions.RESTORE_NAME -> quietly { store.rename(channel.id, null) }
            GuideChannelOptions.AUDIO_DECODER -> openPicker(channel, ChannelOptionsPicker.AUDIO_DECODER)
            GuideChannelOptions.VIDEO_DECODER -> openPicker(channel, ChannelOptionsPicker.VIDEO_DECODER)
            GuideChannelOptions.EXTERNAL_PLAYER -> quietly { store.toggleExternal(channel.id, externalDefault()) }
            GuideChannelOptions.EPG_OFFSET -> openPicker(channel, ChannelOptionsPicker.EPG_OFFSET)
            GuideChannelOptions.BLOCK -> ChannelOptionsEffect.BLOCK
            GuideChannelOptions.HIDE -> ChannelOptionsEffect.HIDE
            GuideChannelOptions.NAMES_EDITOR -> ChannelOptionsEffect.NAMES_EDITOR
            else -> null
        }

    /** IME Done in the rename dialog; blank restores the playlist name. */
    fun submitRename(text: String) {
        val rename = mutableDialog.value as? ChannelOptionsDialog.Rename ?: return
        store.rename(rename.channelId, text)
        mutableDialog.value = null
    }

    /** A picker row pick, dispatched by the open dialog's kind. */
    fun choose(raw: String) {
        val picker = mutableDialog.value as? ChannelOptionsDialog.Picker ?: return
        when (picker.kind) {
            ChannelOptionsPicker.AUDIO_DECODER ->
                store.setAudioDecoder(picker.channelId, ChannelOptionsValues.decoderRawOf(raw))
            ChannelOptionsPicker.VIDEO_DECODER ->
                store.setVideoDecoder(picker.channelId, ChannelOptionsValues.decoderRawOf(raw))
            ChannelOptionsPicker.EPG_OFFSET ->
                raw.toIntOrNull()?.let { store.setEpgOffset(picker.channelId, it) }
        }
        mutableDialog.value = null
    }

    fun pickerOptions(kind: ChannelOptionsPicker): List<PickerOption> =
        when (kind) {
            ChannelOptionsPicker.EPG_OFFSET -> ChannelOptionsValues.offsetOptions
            else -> ChannelOptionsValues.decoderOptions
        }

    /** The raw the open picker should check for [channel]'s current state. */
    fun pickerCurrent(
        kind: ChannelOptionsPicker,
        channel: ChannelEntity,
    ): String =
        when (kind) {
            ChannelOptionsPicker.AUDIO_DECODER -> ChannelOptionsValues.decoderLabel(channel.overrides.audioDecoder)
            ChannelOptionsPicker.VIDEO_DECODER -> ChannelOptionsValues.decoderLabel(channel.overrides.videoDecoder)
            ChannelOptionsPicker.EPG_OFFSET -> channel.overrides.epgOffsetMinutes.toString()
        }

    /** BACK closes an open dialog first; false = nothing was open. */
    fun closeDialog(): Boolean {
        val wasOpen = mutableDialog.value != null
        mutableDialog.value = null
        return wasOpen
    }

    private fun openRename(channel: ChannelEntity): ChannelOptionsEffect? {
        mutableDialog.value = ChannelOptionsDialog.Rename(channel.id, channel.displayName)
        return null
    }

    private fun openPicker(
        channel: ChannelEntity,
        kind: ChannelOptionsPicker,
    ): ChannelOptionsEffect? {
        mutableDialog.value = ChannelOptionsDialog.Picker(channel.id, kind)
        return null
    }

    /** An in-place action row: acts, no dialog, nothing for the host. */
    private fun quietly(action: () -> Unit): ChannelOptionsEffect? {
        action()
        return null
    }
}
