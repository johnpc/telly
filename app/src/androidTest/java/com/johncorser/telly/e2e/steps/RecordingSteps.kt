package com.johncorser.telly.e2e.steps

import android.view.KeyEvent
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performSemanticsAction
import com.johncorser.telly.e2e.PlaybackDriver
import com.johncorser.telly.e2e.TellyWorld
import io.cucumber.java.en.Then
import io.cucumber.java.en.When

/**
 * Steps for the recording / DVR slice: opening the library, recording the
 * playing channel through the context sheet, the custom-recording form,
 * and library row actions (play / stop / delete). Every regex was
 * cross-checked against the existing step definitions to avoid clashes.
 */
class RecordingSteps(
    private val world: TellyWorld,
    private val driver: PlaybackDriver,
) {
    @When("I open the recordings library")
    fun openLibrary() {
        world.longPressOk()
        world.select("Recordings")
    }

    @When("I record the playing channel from the context sheet")
    fun recordPlaying() {
        driver.openPanel()
        driver.longPressRow(driver.currentChannel.name)
        world.select("Record")
    }

    @When("I open the custom recording form for the playing channel")
    fun openCustomForm() {
        driver.openPanel()
        driver.longPressRow(driver.currentChannel.name)
        world.select("Custom recording")
    }

    @Then("the recordings library is empty")
    fun libraryEmpty() = world.waitForText("No recordings")

    @Then("the recordings library shows a recording of {string}")
    fun libraryShows(channel: String) {
        world.waitFor(recordingRow(channel))
    }

    @Then("the recording shows the REC badge")
    fun recBadge() = world.waitForText("REC")

    @Then("the recording shows the {string} state")
    fun recState(state: String) = world.waitForText(state)

    @When("I select the recording of {string}")
    fun selectRecording(channel: String) {
        focusRecording(channel)
        world.pressKey(KeyEvent.KEYCODE_DPAD_CENTER)
    }

    @When("I long-press ok on the recording of {string}")
    fun longPressRecording(channel: String) {
        focusRecording(channel)
        world.longPressOk()
        if (world.nodeCount(hasText("Delete recording?")) == 0) {
            world.compose.onAllNodes(
                recordingRow(channel),
            ).onFirst().performSemanticsAction(SemanticsActions.OnLongClick)
        }
    }

    @Then("the recording plays back fullscreen")
    fun playsBack() = world.waitFor(hasTestTag("recording-player"))

    private fun recordingRow(channel: String): SemanticsMatcher =
        hasTestTag("recording-row") and hasText(channel, substring = true)

    private fun focusRecording(channel: String) {
        world.waitFor(recordingRow(channel))
        world.compose.onAllNodes(recordingRow(channel)).onFirst().performSemanticsAction(SemanticsActions.RequestFocus)
        world.waitFor(recordingRow(channel) and isFocused())
    }
}
