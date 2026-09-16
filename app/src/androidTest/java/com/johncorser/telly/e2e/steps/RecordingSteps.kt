package com.johncorser.telly.e2e.steps

import android.view.KeyEvent
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performSemanticsAction
import com.johncorser.telly.core.ServiceLocator
import com.johncorser.telly.e2e.PlaybackDriver
import com.johncorser.telly.e2e.TellyWorld
import com.johncorser.telly.features.recording.RecordingStatus
import com.johncorser.telly.features.recording.recordingCenter
import com.johncorser.telly.features.recording.recordingStatus
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

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
        // A completed DVR action from the panel-row sheet deliberately lands
        // back on the PANEL (PlaybackRecordingPrompts.doneTarget), where
        // long-OK would reopen the row sheet instead of the quick bar —
        // BACK the chrome away first so long-OK opens the quick bar.
        driver.dismissChrome()
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

    /** Bare playback -> info overlay -> transport row (second UP). */
    @When("I open the playback transport row")
    fun openTransportRow() {
        driver.dismissChrome()
        ensureTransportVisible()
    }

    @When("I activate the transport record dot")
    fun activateRecordDot() {
        ensureTransportVisible()
        world.compose
            .onAllNodes(recordDot())
            .onFirst()
            .performSemanticsAction(SemanticsActions.RequestFocus)
        world.waitFor(recordDot() and isFocused())
        world.pressKey(KeyEvent.KEYCODE_DPAD_CENTER)
    }

    @Then("the transport record dot shows the channel recording")
    fun recordDotRecording() {
        ensureTransportVisible()
        world.waitFor(recordDot() and hasContentDescription("Recording"))
    }

    @Then("the transport record dot is idle again")
    fun recordDotIdle() {
        ensureTransportVisible()
        world.waitFor(recordDot() and hasContentDescription("Record"))
    }

    private fun recordDot(): SemanticsMatcher = hasTestTag("transport-record")

    /** A completed dot action clears the chrome; UP UP re-opens the row. */
    private fun ensureTransportVisible() {
        driver.awaitCondition("transport row visible") {
            if (world.nodeCount(recordDot()) > 0) {
                true
            } else {
                world.pressKey(KeyEvent.KEYCODE_DPAD_UP)
                false
            }
        }
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

    /**
     * DVR-facade assertion (no library UI involved) so the guide/panel
     * sheet scenarios stay independent of recording.feature's flows.
     */
    @Then("a recording of {string} is in progress")
    fun recordingInProgress(channel: String) {
        val center = ServiceLocator.recordingCenter(world.targetContext)
        driver.awaitCondition("a live recording of $channel") {
            runBlocking {
                center.recordings.first().any {
                    it.channelName == channel && it.recordingStatus == RecordingStatus.RECORDING
                }
            }
        }
    }

    private fun recordingRow(channel: String): SemanticsMatcher =
        hasTestTag("recording-row") and hasText(channel, substring = true)

    private fun focusRecording(channel: String) {
        world.waitFor(recordingRow(channel))
        world.compose.onAllNodes(recordingRow(channel)).onFirst().performSemanticsAction(SemanticsActions.RequestFocus)
        world.waitFor(recordingRow(channel) and isFocused())
    }
}
