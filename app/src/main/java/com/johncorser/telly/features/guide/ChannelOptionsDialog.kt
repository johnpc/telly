package com.johncorser.telly.features.guide

/** The pane's in-pane dialogs: the rename editor and the value pickers. */
sealed interface ChannelOptionsDialog {
    val channelId: Long

    data class Rename(
        override val channelId: Long,
        val initial: String,
    ) : ChannelOptionsDialog

    data class Picker(
        override val channelId: Long,
        val kind: ChannelOptionsPicker,
    ) : ChannelOptionsDialog
}

/** The three picker rows and their sheets' titles. */
enum class ChannelOptionsPicker(
    val title: String,
) {
    AUDIO_DECODER("Audio decoder"),
    VIDEO_DECODER("Video decoder"),
    EPG_OFFSET("EPG time offset, h:min"),
}

/** Rows the pane cannot finish alone; the HOST runs its existing flow. */
enum class ChannelOptionsEffect { BLOCK, HIDE, NAMES_EDITOR }
