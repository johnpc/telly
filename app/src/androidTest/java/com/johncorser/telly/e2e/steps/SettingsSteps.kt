package com.johncorser.telly.e2e.steps

import android.view.KeyEvent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.isNotEnabled
import androidx.compose.ui.test.isOff
import androidx.compose.ui.test.isOn
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performTextReplacement
import com.johncorser.telly.core.ServiceLocator
import com.johncorser.telly.core.settings.ParentalControls
import com.johncorser.telly.core.settings.TellySettings
import com.johncorser.telly.e2e.PlaybackDriver
import com.johncorser.telly.e2e.TellyWorld
import com.johncorser.telly.features.epg.RefreshScheduler
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

/** Steps for the two-pane settings shell and parental controls. */
class SettingsSteps(
    private val world: TellyWorld,
    private val driver: PlaybackDriver,
) {
    @Given("I open Settings")
    fun openSettings() {
        driver.openPanel()
        driver.longPressRow(driver.currentChannel.name)
        world.select("Settings")
        world.waitForText("All features are available in Premium version")
    }

    @Then("I see the sections {string} in order")
    fun sectionsInOrder(list: String) {
        val names = list.split(",").map { it.trim() }
        names.forEach { world.waitForText(it) }
        val tops =
            names.map { name ->
                world.compose.onAllNodes(hasText(name)).fetchSemanticsNodes().minOf { it.boundsInRoot.top }
            }
        assertTrue("sections rendered top-to-bottom: $tops", tops == tops.sorted())
    }

    @When("I open the {string} section")
    fun openSection(name: String) {
        // Sheet-stack shell: OK on a section row replaces the root sheet
        // with that section's sheet.
        world.select(name)
    }

    @When("I activate {string}")
    fun activate(text: String) = world.select(text)

    @Then("the {string} toggle is on")
    fun toggleOn(title: String) = world.waitFor(hasText(title) and isOn())

    @Then("the {string} toggle is off")
    fun toggleOff(title: String) = world.waitFor(hasText(title) and isOff())

    @Then("the {string} row shows {string}")
    fun rowShows(
        title: String,
        value: String,
    ) = world.waitFor(hasText(title) and hasText(world.mapFixtureText(value)))

    @When("I choose {string}")
    fun choose(option: String) = world.select(option)

    @Then("EPG data older than {int} hours is due for refresh")
    fun olderIsDue(hours: Int) {
        assertTrue("stale EPG due", refreshDue(staleByHours = hours + 1))
    }

    @Then("EPG data fresher than {int} hours is not due for refresh")
    fun fresherNotDue(hours: Int) {
        assertFalse("fresh EPG not due", refreshDue(staleByHours = hours - 1))
    }

    @When("I set the PIN to {string}")
    fun setPin(pin: String) {
        world.waitForText("Change PIN")
        driver.spinPinWheels(pin)
    }

    @When("I leave settings")
    fun leaveSettingsStep() = leaveSettings()

    @When("I leave settings for fullscreen playback")
    fun leaveSettingsForPlayback() {
        leaveSettings()
        driver.dismissChrome()
    }

    /** Masked keyboard entry ("PIN input method" = Keyboard): 4 digits commit. */
    @When("I type the PIN {string} on the keyboard")
    fun typePinOnKeyboard(pin: String) {
        world.waitFor(hasTestTag("pin-keyboard"))
        world.compose
            .onAllNodes(hasTestTag("pin-keyboard"))
            .onFirst()
            .performTextReplacement(pin)
        world.compose.waitForIdle()
    }

    /** Arranged through the real store, like group locking below. */
    @Given("confirm exit on second BACK is enabled")
    fun enableConfirmExit() {
        ServiceLocator.settingsRepository(world.targetContext).set(TellySettings.CONFIRM_EXIT, true)
    }

    /** The clock format is a store-only key (no captured settings row). */
    @Given("the clock format is {string}")
    fun setClockFormat(raw: String) {
        ServiceLocator.settingsRepository(world.targetContext).set(TellySettings.CLOCK_FORMAT, raw)
    }

    /** BACKs out of the settings shell to fullscreen playback. */
    private fun leaveSettings() {
        driver.awaitCondition("left the settings shell") {
            if (world.nodeCount(hasText("All features are available in Premium version")) == 0) {
                true
            } else {
                world.pressKey(KeyEvent.KEYCODE_BACK)
                false
            }
        }
    }

    @When("the group {string} is locked")
    fun lockGroup(group: String) {
        // No capture-verified UI owns group locking yet; arrange it through
        // the app's real parental-controls policy over the real store.
        ParentalControls(ServiceLocator.settingsRepository(world.targetContext)).setGroupLocked(group, true)
    }

    @Given("parental controls protect telly with the PIN {string}")
    fun parentalPinArranged(pin: String) {
        // Arranged through the real policy over the real store, like the
        // locked-group step: the PIN-setup UI has its own scenario.
        val parental = ParentalControls(ServiceLocator.settingsRepository(world.targetContext))
        parental.setEnabled(true)
        parental.setPin(pin)
    }

    @Then("the sheet asks for the PIN")
    fun sheetAsksForPin() = world.waitFor(hasTestTag("pin-wheel"))

    // "I enter the PIN {string}" lives in BlockSteps (shared with the
    // block-channel scenarios) — one definition serves every PIN prompt.

    @Then("opening the group {string} requires the PIN")
    fun groupRequiresPin(group: String) {
        leaveSettings()
        driver.openPanel()
        world.pressKey(KeyEvent.KEYCODE_DPAD_LEFT)
        world.select(group)
        world.waitForText("Enter PIN")
    }

    @Then("entering the PIN {string} unlocks it")
    fun enterPinUnlocks(pin: String) {
        driver.spinPinWheels(pin)
        world.waitForGone(hasText("Enter PIN"))
        world.waitForText("Movie House")
    }

    @Then("opening the group {string} does not require the PIN")
    fun groupWithoutPin(group: String) {
        world.select(group)
        world.waitForGone(hasText("Enter PIN"))
        world.waitForText("News One")
    }

    @When("I activate the playlist {string}")
    fun activatePlaylist(name: String) = world.select(world.mapFixtureText(name))

    @Then("the playlists section lists {string}")
    fun playlistsSectionLists(name: String) {
        driver.awaitCondition("back at the Playlists pane") {
            if (world.nodeCount(hasText("Add playlist")) > 0) {
                true
            } else {
                world.pressKey(KeyEvent.KEYCODE_BACK)
                false
            }
        }
        world.waitForText(world.mapFixtureText(name))
    }

    @Then("the channel panel group list does not include {string}")
    fun panelGroupsExclude(group: String) {
        leaveSettings()
        driver.openPanel()
        world.pressKey(KeyEvent.KEYCODE_DPAD_LEFT)
        world.waitForText("All channels")
        assertEquals(0, world.nodeCount(hasText(group)))
    }

    @Then("the row {string} is locked")
    fun rowLocked(title: String) = world.waitFor(hasText(title) and isNotEnabled())

    @Then("the row {string} is not locked")
    fun rowNotLocked(title: String) = world.waitFor(hasText(title) and isEnabled())

    private fun refreshDue(staleByHours: Int): Boolean {
        val settings = ServiceLocator.settingsRepository(world.targetContext)
        val scheduler =
            RefreshScheduler(
                intervalMs = { RefreshScheduler.hoursToMs(settings.get(TellySettings.EPG_UPDATE_INTERVAL_HOURS)) },
            )
        val now = System.currentTimeMillis()
        return scheduler.isDue(lastUpdatedMs = now - staleByHours * RefreshScheduler.HOUR_MS, nowMs = now)
    }
}
