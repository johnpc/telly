package com.johncorser.telly.features.onboarding

import com.johncorser.telly.core.navigation.Route

/**
 * Cold-start destination policy: once channels are persisted the app skips
 * onboarding and lands on fullscreen playback of the last-watched channel;
 * a fresh install stays on the welcome flow (null = keep the current stack).
 */
object StartRoute {
    fun forChannelCount(channelCount: Int): Route? = if (channelCount > 0) Route.Playback else null
}
