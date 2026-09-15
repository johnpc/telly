package com.johncorser.telly.e2e.steps

import android.view.KeyEvent
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocused
import com.johncorser.telly.e2e.PlaybackDriver
import com.johncorser.telly.e2e.TellyWorld
import com.johncorser.telly.e2e.fixtures.FixtureServer
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When

/**
 * Steps for the multiview grid (multiview-round captures). Audio ownership
 * is asserted via the pane semantics ("audio"/"muted"), never real sound.
 */
class MultiviewSteps(
    private val world: TellyWorld,
    private val driver: PlaybackDriver,
) {
    @Given("I opened multiview")
    fun openedMultiview() {
        driver.dismissChrome()
        world.longPressOk()
        world.select("Multiview")
        world.waitForText("Press OK to show menu")
    }

    @Given("I opened multiview with a second screen {string}")
    fun openedMultiviewWithSecondScreen(name: String) {
        openedMultiview()
        world.pressKey(KeyEvent.KEYCODE_DPAD_CENTER)
        world.select("Add screen")
        pickChannel(name)
        seePaneCount(2)
    }

    @When("I pick the channel {string} in the multiview picker")
    fun pickChannel(name: String) {
        driver.focusRow(name)
        world.pressKey(KeyEvent.KEYCODE_DPAD_CENTER)
    }

    @Then("I see {int} multiview pane(s)")
    fun seePaneCount(count: Int) {
        driver.awaitCondition("$count multiview pane(s) composed") {
            world.nodeCount(hasTestTag("multiview-pane")) == count
        }
    }

    @Then("the multiview pane {string} is focused with audio")
    fun paneFocusedWithAudio(name: String) {
        world.waitFor(paneMatcher(name, "audio") and isFocused())
    }

    @Then("the multiview pane {string} is muted")
    fun paneMuted(name: String) {
        world.waitFor(paneMatcher(name, "muted"))
    }

    @Then("I see the multiview menu rows {string}, {string} and {string}")
    fun seeMenuRows(
        first: String,
        second: String,
        third: String,
    ) {
        listOf(first, second, third).forEach { world.waitForText(it) }
    }

    @Then("the multiview channel picker shows the list, schedule and detail panes")
    fun seePickerPanes() {
        world.waitForText("All channels")
        world.waitFor(hasContentDescription("Multiview schedule"))
        // The detail card carries the focused channel's airing programme.
        val programme = FixtureServer.nowProgramme(driver.currentChannel.tvgId, System.currentTimeMillis())
        world.waitFor(hasText(programme.title, substring = true), atLeast = 1)
    }

    private fun paneMatcher(
        name: String,
        audioState: String,
    ) = hasTestTag("multiview-pane") and hasContentDescription("$name, $audioState", substring = true)
}
