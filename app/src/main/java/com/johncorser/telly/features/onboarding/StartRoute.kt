package com.johncorser.telly.features.onboarding

import com.johncorser.telly.core.navigation.Route
import com.johncorser.telly.core.state.StateMachine

/**
 * Where a cold start lands, as ONE value: the route to show and whether the
 * guide may tune its preview there. Replaces the set-here/consume-there
 * `untunedGuideStart` flag.
 */
@StateMachine("Start route")
sealed interface StartDecision {
    val route: Route

    /** False only for the untuned-guide start; the guide reads it off the decision. */
    val tunesPreview: Boolean get() = true

    data object Welcome : StartDecision {
        override val route: Route = Route.Welcome
    }

    data object Playback : StartDecision {
        override val route: Route = Route.Playback
    }

    /** "Turn on last channel on app start" OFF: the guide, with NO preview tune. */
    data object GuideUntuned : StartDecision {
        override val route: Route = Route.Guide
        override val tunesPreview: Boolean = false
    }
}

/**
 * Cold-start destination policy (round3 P0 item 3): the app boots on
 * [Route.Boot] — a bare #131619 frame, like TiviMate's skeleton — and only
 * navigates once the Room channel count resolves. A fresh install goes to
 * the welcome flow; with channels, "Turn on last channel on app start"
 * decides: ON (telly's default — see the TellySettings default-flip note)
 * goes straight to fullscreen playback of the last channel, OFF lands on
 * the TV guide with NO tune. The decision is kept as a value the FIRST
 * guide built after it reads through [claimForGuide]; later guides (any
 * route change) resume their preview as usual. No interstitial ever flashes.
 */
class StartRoute(
    private val lastChannelOnStart: () -> Boolean = { true },
) {
    private var pending: StartDecision? = null

    fun decide(channelCount: Int): StartDecision =
        when {
            channelCount <= 0 -> StartDecision.Welcome
            lastChannelOnStart() -> StartDecision.Playback
            else -> StartDecision.GuideUntuned
        }.also { pending = it }

    fun forChannelCount(channelCount: Int): Route = decide(channelCount).route

    /** The cold-start decision, handed to exactly one guide; absent afterwards. */
    fun claimForGuide(): StartDecision? = pending.also { pending = null }

    /** Whether the guide being built should tune its preview (true after the first claim). */
    fun guideTunesPreview(): Boolean = claimForGuide()?.tunesPreview ?: true
}
