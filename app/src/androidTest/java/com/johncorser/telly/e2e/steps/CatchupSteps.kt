package com.johncorser.telly.e2e.steps

import android.os.SystemClock
import android.view.KeyEvent
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import com.johncorser.telly.e2e.PlaybackDriver
import com.johncorser.telly.e2e.TellyWorld
import com.johncorser.telly.e2e.fixtures.FixturePlan
import com.johncorser.telly.e2e.fixtures.FixtureProgramme
import com.johncorser.telly.e2e.fixtures.FixtureServer
import io.cucumber.java.en.Then
import io.cucumber.java.en.When

/** Steps for catch-up playback of already-aired programmes (@catch-up). */
class CatchupSteps(
    private val world: TellyWorld,
    private val driver: PlaybackDriver,
) {
    /** The programme a −24 h day jump lands on for [channelNumber]'s row. */
    private fun yesterdaysProgramme(channelNumber: Int): FixtureProgramme =
        FixtureServer.nowProgramme(
            FixturePlan.channels[channelNumber - 1].tvgId,
            System.currentTimeMillis() - DAY_MS,
        )

    @When("I long-press dpad left")
    fun longPressLeft() = world.longPressKey(KeyEvent.KEYCODE_DPAD_LEFT)

    @When("I press fast-forward")
    fun pressFastForward() = world.pressKey(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD)

    @Then("the guide shows yesterday's programme on channel {int}")
    fun guideShowsYesterdaysProgramme(channelNumber: Int) {
        world.waitForText(yesterdaysProgramme(channelNumber).displayTitle, substring = true)
    }

    @Then("catch-up playback starts for yesterday's programme on channel {int}")
    fun catchupPlaybackStarts(channelNumber: Int) {
        val channel = FixturePlan.channels[channelNumber - 1]
        driver.currentChannel = channel
        // The guide is gone (fullscreen playback) and the transport-bearing
        // info overlay identifies the archived programme, not the airing one.
        world.waitFor(hasTestTag(CATCHUP_TRANSPORT_TAG), unmerged = true)
        world.waitForText(yesterdaysProgramme(channelNumber).displayTitle, substring = true)
        world.waitForText(driver.channelLabel(channel.number, channel.name), substring = true)
    }

    @Then("the seek transport shows the position readout")
    fun seekTransportVisible() {
        // The overlay auto-hides after ~5 s; UP re-opens it (a second UP
        // expands the transport row) exactly like the live key map.
        driver.awaitCondition("catch-up seek transport visible") {
            if (world.nodeCount(hasTestTag(CATCHUP_TRANSPORT_TAG), unmerged = true) > 0) {
                true
            } else {
                world.pressKey(KeyEvent.KEYCODE_DPAD_UP)
                false
            }
        }
        world.waitFor(world.hasTextMatching(POSITION_READOUT))
        world.waitFor(world.hasTextMatching(DURATION_READOUT))
    }

    /** Focuses a transport button by its semantics label and clicks it. */
    @When("I activate the catch-up transport {string} button")
    fun activateTransportButton(label: String) {
        seekTransportVisible()
        world.select(label)
    }

    @Then("catch-up playback is paused with the transport pinned")
    fun pausedWithPinnedTransport() {
        // The ⏸ slot's icon/semantics flip to Resume the moment pause lands.
        world.waitFor(hasContentDescription("Resume"))
        // Pinned = still up well past the 5.35 s auto-hide (VOD pin idiom).
        SystemClock.sleep(PIN_PROVE_MS)
        driver.assertNow(
            "transport still pinned while paused",
            world.nodeCount(hasTestTag(CATCHUP_TRANSPORT_TAG), unmerged = true) > 0,
        )
    }

    @Then("catch-up playback shows the programme before yesterday's on channel {int}")
    fun showsProgrammeBeforeYesterdays(channelNumber: Int) {
        val yesterday = yesterdaysProgramme(channelNumber)
        val previous =
            FixtureServer.nowProgramme(FixturePlan.channels[channelNumber - 1].tvgId, yesterday.startMs - 1)
        world.waitFor(hasTestTag(CATCHUP_TRANSPORT_TAG), unmerged = true)
        world.waitForText(previous.displayTitle, substring = true)
    }

    @Then("the archive ends and live playback of channel {int} resumes")
    fun archiveEndsToLive(channelNumber: Int) {
        val channel = FixturePlan.channels[channelNumber - 1]
        // The end-of-archive retune leaves catch-up mode; re-opening the
        // transport shows the live row again (LIVE badge, no catch-up tag).
        driver.awaitCondition("live transport after the archive ends", timeoutMs = ARCHIVE_END_TIMEOUT_MS) {
            if (world.nodeCount(hasText("LIVE")) > 0) {
                true
            } else {
                world.pressKey(KeyEvent.KEYCODE_DPAD_UP)
                false
            }
        }
        driver.assertNow(
            "catch-up transport gone after the archive ends",
            world.nodeCount(hasTestTag(CATCHUP_TRANSPORT_TAG), unmerged = true) == 0,
        )
        world.waitForText(
            FixtureServer.nowProgramme(channel.tvgId, System.currentTimeMillis()).displayTitle,
            substring = true,
        )
    }

    @When("I return from catch-up playback")
    fun returnFromCatchup() {
        // Clear the transport/info chrome first, then one BACK leaves the
        // archive for the guide (the entry point).
        driver.dismissChrome()
        world.pressKey(KeyEvent.KEYCODE_BACK)
    }

    @Then("the dropdown offers no {string} action")
    fun dropdownOffersNo(label: String) {
        driver.assertNow("no \"$label\" row in the dropdown", world.nodeCount(hasText(label)) == 0)
    }

    private companion object {
        const val DAY_MS = 24 * 3_600_000L
        const val CATCHUP_TRANSPORT_TAG = "catchup-transport"

        /** Comfortably past the transport's ~5.35 s auto-hide. */
        const val PIN_PROVE_MS = 6_500L

        /** Covers a full natural play-out of the 60 s fixture archive. */
        const val ARCHIVE_END_TIMEOUT_MS = 90_000L
        val POSITION_READOUT = Regex("\\d{2}:\\d{2}")
        val DURATION_READOUT = Regex(" / .+")
    }
}
