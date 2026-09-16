package com.johncorser.telly.e2e.steps

import android.os.SystemClock
import android.view.KeyEvent
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextReplacement
import com.johncorser.telly.e2e.PlaybackDriver
import com.johncorser.telly.e2e.TellyWorld
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When

/** Steps shared across all feature areas: keys, selection, typing, seeing. */
class CommonSteps(
    private val world: TellyWorld,
    private val driver: PlaybackDriver,
) {
    @Given("a fresh install of telly")
    fun freshInstall() {
        // State is wiped in the Before hook; just cold-start the app.
        world.launchApp()
    }

    @Given("the fixture playlist is served at {string}")
    fun playlistServed(url: String) {
        // The embedded fixture server is started per scenario in the Before
        // hook; this documents the dependency and validates the mapping.
        require(world.mapFixtureText(url).endsWith("/playlist.m3u")) { "unexpected fixture URL $url" }
    }

    @Given("the fixture EPG is served at {string}")
    fun epgServed(url: String) {
        require(world.mapFixtureText(url).endsWith("/epg.xml")) { "unexpected fixture URL $url" }
    }

    @Given("an alternative fixture EPG is served at {string}")
    fun altEpgServed(url: String) {
        require(world.mapFixtureText(url).endsWith("/epg-alt.xml")) { "unexpected fixture URL $url" }
    }

    @Given("the fixture playlist and EPG are served from {string}")
    fun fixturesServed(base: String) {
        require(world.mapFixtureText(base).startsWith("http://")) { "unexpected fixture base $base" }
    }

    @Given("I completed the add-playlist wizard")
    fun completedWizard() {
        driver.completeWizard()
    }

    @Given("I added the playlist {string}")
    fun addedPlaylist(url: String) {
        driver.completeWizard(world.mapFixtureText(url))
    }

    @When("I press dpad right")
    fun pressRight() = world.pressKey(KeyEvent.KEYCODE_DPAD_RIGHT)

    @When("I press dpad right {int} times")
    fun pressRightTimes(times: Int) = world.pressKey(KeyEvent.KEYCODE_DPAD_RIGHT, times = times)

    @When("I press dpad left")
    fun pressLeft() = world.pressKey(KeyEvent.KEYCODE_DPAD_LEFT)

    @When("I press dpad up")
    fun pressUp() = world.pressKey(KeyEvent.KEYCODE_DPAD_UP)

    @When("I press dpad down")
    fun pressDown() = world.pressKey(KeyEvent.KEYCODE_DPAD_DOWN)

    @When("I press dpad down {int} times")
    fun pressDownTimes(times: Int) = world.pressKey(KeyEvent.KEYCODE_DPAD_DOWN, times = times)

    @When("I press ok")
    fun pressOk() = world.pressKey(KeyEvent.KEYCODE_DPAD_CENTER)

    @When("I long-press ok")
    fun longPressOk() = world.longPressOk()

    @When("I press back")
    fun pressBack() = world.pressKey(KeyEvent.KEYCODE_BACK)

    @When("I press menu")
    fun pressMenu() = world.pressKey(KeyEvent.KEYCODE_MENU)

    @When("I press channel up")
    fun pressChannelUp() = world.pressKey(KeyEvent.KEYCODE_CHANNEL_UP)

    @When("I press channel down")
    fun pressChannelDown() = world.pressKey(KeyEvent.KEYCODE_CHANNEL_DOWN)

    @When("I wait {int} seconds")
    fun waitSeconds(seconds: Int) {
        SystemClock.sleep(seconds * MILLIS_PER_SECOND)
    }

    @When("I select {string}")
    fun select(text: String) = world.select(world.mapFixtureText(text))

    @When("I type {string}")
    fun type(raw: String) {
        val text = world.mapFixtureText(raw)
        if (world.nodeCount(setTextNodes()) > 0) {
            world.compose.onAllNodes(setTextNodes()).onFirst().performTextReplacement(text)
            // The search query bar commits to the history only on the
            // explicit IME search action step; value dialogs commit here.
            if (world.nodeCount(hasContentDescription("Voice search")) == 0) {
                world.compose.onAllNodes(setTextNodes()).onFirst().performImeAction()
            }
            // Compose does not always hide the IME when the field leaves the
            // tree; hide it in-process (never via BACK — a BACK racing the
            // IME's own hide reaches the app and pops the pane under test).
            driver.awaitCondition("IME dismissed") {
                if (world.imeVisible()) {
                    world.hideIme()
                    false
                } else {
                    true
                }
            }
            world.compose.waitForIdle()
        } else {
            driver.typeIntoEditor(text)
        }
    }

    @When("I relaunch telly")
    fun relaunch() = world.relaunchApp()

    @Then("I see {string}")
    fun see(text: String) = world.waitForText(world.mapFixtureText(text), substring = true)

    @Then("{string} has focus")
    fun hasFocus(text: String) = world.waitFor(hasText(text) and isFocused())

    private fun setTextNodes(): SemanticsMatcher = SemanticsMatcher.keyIsDefined(SemanticsActions.SetText)

    private companion object {
        const val MILLIS_PER_SECOND = 1_000L
    }
}
