package com.johncorser.telly.features.playback

/** Which layer currently covers the fullscreen video. */
sealed interface PlaybackOverlay {
    /** Bare playback: zero chrome (capture 33). */
    data object None : PlaybackOverlay

    /** Bottom info overlay with programme data + shortcut cards (capture 34). */
    data object Info : PlaybackOverlay

    /** Channel list panel over the dimmed video (captures 36/47). */
    data object Panel : PlaybackOverlay

    /** Long-OK / MENU player context menu (captures 38-40). */
    data object Menu : PlaybackOverlay

    /** Channel context menu opened by long-OK on a panel row (capture 38 §channel). */
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
