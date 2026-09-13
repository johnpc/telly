package com.johncorser.telly.features.onboarding

import com.johncorser.telly.core.navigation.Route

/**
 * Cold-start destination policy (round3 P0 item 3): the app boots on
 * [Route.Boot] — a bare #131619 frame, like TiviMate's skeleton — and only
 * navigates once the Room channel count resolves: persisted channels go
 * straight to fullscreen playback, a fresh install to the welcome flow.
 * No interstitial ever flashes.
 */
object StartRoute {
    fun forChannelCount(channelCount: Int): Route = if (channelCount > 0) Route.Playback else Route.Welcome
}
