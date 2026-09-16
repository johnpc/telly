package com.johncorser.telly.features.onboarding

import com.johncorser.telly.core.navigation.Route

/**
 * Cold-start destination policy (round3 P0 item 3): the app boots on
 * [Route.Boot] — a bare #131619 frame, like TiviMate's skeleton — and only
 * navigates once the Room channel count resolves. A fresh install goes to
 * the welcome flow; with channels, "Turn on last channel on app start"
 * decides: ON (telly's default — see the TellySettings default-flip note)
 * goes straight to fullscreen playback of the last channel, OFF lands on
 * the TV guide with NO tune — [consumeUntunedStart] tells the guide, once,
 * to skip its preview resume. No interstitial ever flashes.
 */
class StartRoute(
    private val lastChannelOnStart: () -> Boolean = { true },
) {
    private var untunedGuideStart = false

    fun forChannelCount(channelCount: Int): Route =
        when {
            channelCount <= 0 -> Route.Welcome
            lastChannelOnStart() -> Route.Playback
            else -> {
                untunedGuideStart = true
                Route.Guide
            }
        }

    /** True exactly once after a cold start routed to the untuned guide. */
    fun consumeUntunedStart(): Boolean {
        val was = untunedGuideStart
        untunedGuideStart = false
        return was
    }
}
