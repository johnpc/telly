package com.johncorser.telly.features.playback

/** Which layer currently covers the fullscreen video. */
sealed interface PlaybackOverlay {
    /** Bare playback: zero chrome (capture 33). */
    data object None : PlaybackOverlay

    /** Bottom info overlay with programme data + shortcut cards (capture 34). */
    data object Info : PlaybackOverlay

    /** Info overlay expanded with the transport row (round3-ref 03b). */
    data object InfoTransport : PlaybackOverlay

    /** Compact channel-change overlay while the new stream tunes (round3-ref 10). */
    data object ZapInfo : PlaybackOverlay

    /** Bottom icon quick-bar from long-OK / MENU at fullscreen (round3-ref 07/08). */
    data object QuickBar : PlaybackOverlay

    /** Channel list panel over the dimmed video (captures 36/47). */
    data object Panel : PlaybackOverlay

    /** Right-side sheet from long-OK on a panel row (round3-ref 05). */
    data class ChannelMenu(
        val channelId: Long,
    ) : PlaybackOverlay

    /** Branded placeholder for menu entries whose feature is a later slice. */
    data class ComingSoon(
        val feature: String,
    ) : PlaybackOverlay
}

/** D-pad / media keys playback reacts to (mapped from KeyEvents in the UI). */
enum class PlaybackKey { OK, LONG_OK, MENU, BACK, UP, DOWN, LEFT, RIGHT, CHANNEL_UP, CHANNEL_DOWN }
