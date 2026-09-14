package com.johncorser.telly.e2e.steps

import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import com.johncorser.telly.e2e.PlaybackDriver
import com.johncorser.telly.e2e.TellyWorld
import com.johncorser.telly.e2e.fixtures.FixtureServer
import com.johncorser.telly.e2e.fixtures.rangeText
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When

/** Steps for the fullscreen playback surface and its overlays. */
class PlaybackSteps(
    private val world: TellyWorld,
    private val driver: PlaybackDriver,
) {
    @Then("playback starts fullscreen on channel {int} {string}")
    fun playbackStartsOn(
        number: Int,
        name: String,
    ) = driver.assertPlaybackOn(number, name)

    @Then("no chrome is visible over the video")
    fun noChrome() = driver.assertNoChrome()

    @Given("I zapped to channel {int} {string}")
    fun zappedTo(
        number: Int,
        name: String,
    ) {
        // A DIRECT tune (panel row OK), not CHANNEL_UP hops: every hop is a
        // real tune the watch history records, which would pollute the
        // History scenarios with the channels stepped over on the way.
        driver.openPanel()
        driver.focusRow(name)
        world.pressKey(android.view.KeyEvent.KEYCODE_DPAD_CENTER)
        driver.assertZapOverlayShows(number, name)
    }

    @Then("playback switches to channel {int} {string}")
    fun playbackSwitchesTo(
        number: Int,
        name: String,
    ) = driver.assertZapOverlayShows(number, name)

    @Then("I see the bottom info overlay")
    fun seeInfoOverlay() {
        world.waitForText("TV guide")
    }

    @Then("I see the channel logo, number {int} and name {string}")
    fun seeChannelIdentity(
        number: Int,
        name: String,
    ) {
        world.waitForText(driver.channelLabel(number, name), substring = true)
        world.waitFor(hasContentDescription("$name logo"), unmerged = true)
    }

    @Then("I see the current programme title with its time range and progress")
    fun seeCurrentProgramme() {
        val tvgId = driver.currentChannel.tvgId
        val now = System.currentTimeMillis()
        val programme = FixtureServer.nowProgramme(tvgId, now)
        world.waitForText(programme.title, substring = true)
        world.waitForText(programme.rangeText(), substring = true)
        world.waitFor(hasTestTag("progress-bar"), unmerged = true)
    }

    @Then("I see the next programme line")
    fun seeNextProgramme() {
        val tvgId = driver.currentChannel.tvgId
        val next = FixtureServer.nextProgramme(tvgId, System.currentTimeMillis())
        world.waitForText(next.title, substring = true)
    }

    @Then("I see stream badges like {string}, {string} and {string}")
    fun seeBadges(
        first: String,
        second: String,
        third: String,
    ) {
        // Badges appear once the decoder reports the stream format; the
        // overlay auto-hides, so re-open it until they are rendered.
        driver.awaitCondition("badges $first/$second/$third visible", timeoutMs = 60_000L) {
            val visible = listOf(first, second, third).all { world.nodeCount(hasText(it)) > 0 }
            if (!visible && driver.noChromeNow()) {
                world.pressKey(android.view.KeyEvent.KEYCODE_DPAD_CENTER)
            }
            visible
        }
    }

    @Then("I see the {string} and {string} cards")
    fun seeCards(
        first: String,
        second: String,
    ) {
        world.waitForText(first)
        world.waitForText(second)
    }

    @When("I select the {string} card")
    fun selectCard(label: String) {
        // The cards live on the playback info overlay; reaching this step
        // from the guide's preview means OK first goes fullscreen and a
        // second OK opens the overlay.
        driver.awaitCondition("$label card visible") {
            if (world.nodeCount(hasText(label)) > 0) {
                true
            } else {
                world.pressKey(android.view.KeyEvent.KEYCODE_DPAD_CENTER)
                false
            }
        }
        world.select(label)
    }

    @Then("I see the quick-bar slots {string}, {string}, {string}, {string} and {string}")
    fun seeQuickBarSlots(
        a: String,
        b: String,
        c: String,
        d: String,
        e: String,
    ) = awaitQuickBarTexts(listOf(a, b, c, d, e))

    @Then("I see the live stream slots {string}, {string}, {string} and {string}")
    fun seeLiveSlots(
        a: String,
        b: String,
        c: String,
        d: String,
    ) {
        // The resolution/audio slots read from the real decoder; give the
        // stream time to be probed before the labels settle.
        awaitQuickBarTexts(listOf(a, b, c, d))
    }

    /** The quick-bar auto-hides after 5 s; reopen it until [labels] render. */
    private fun awaitQuickBarTexts(labels: List<String>) {
        driver.awaitCondition("quick-bar slots $labels visible", timeoutMs = 60_000L) {
            val visible = labels.all { world.nodeCount(hasText(it)) > 0 }
            if (!visible && driver.noChromeNow()) {
                world.longPressOk()
            }
            visible
        }
    }
}
