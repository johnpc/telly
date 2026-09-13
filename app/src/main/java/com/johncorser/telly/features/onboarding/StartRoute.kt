package com.johncorser.telly.features.onboarding

import com.johncorser.telly.core.navigation.Route

/**
 * Cold-start destination policy: once channels are persisted the app skips
 * onboarding and lands on the (stub) main screen; a fresh install stays on
 * the welcome flow (null = keep the navigator's current stack).
 */
object StartRoute {
    fun forChannelCount(channelCount: Int): Route? = if (channelCount > 0) Route.ChannelsLoaded(channelCount) else null
}
