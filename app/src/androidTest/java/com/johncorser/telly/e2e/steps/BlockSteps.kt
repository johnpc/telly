package com.johncorser.telly.e2e.steps

import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import com.johncorser.telly.core.ServiceLocator
import com.johncorser.telly.core.settings.ParentalControls
import com.johncorser.telly.e2e.PlaybackDriver
import com.johncorser.telly.e2e.TellyWorld
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/** Steps for PIN-gated channel blocking (block-channel.feature). */
class BlockSteps(
    private val world: TellyWorld,
    private val driver: PlaybackDriver,
) {
    @Given("the parental PIN is {string}")
    fun parentalPin(pin: String) {
        // Arranged through the app's real parental policy over the real
        // store, exactly like the locked-group settings scenarios.
        ParentalControls(ServiceLocator.settingsRepository(world.targetContext)).setPin(pin)
    }

    @Given("the channel {string} is blocked behind the PIN {string}")
    fun channelBlocked(
        name: String,
        pin: String,
    ) {
        parentalPin(pin)
        val dao = ServiceLocator.database(world.targetContext).channelDao()
        runBlocking {
            val channel = dao.observeVisible().first().first { it.source.name == name }
            dao.update(channel.copy(flags = channel.flags.copy(blocked = true)))
        }
    }

    @When("I enter the PIN {string}")
    fun enterPin(pin: String) = driver.spinPinWheels(pin)

    @Then("the {string} row shows a lock indicator")
    fun rowShowsLock(name: String) {
        world.waitFor(driver.rowMatcher(name))
        world.waitFor(hasContentDescription("Blocked"), unmerged = true)
    }

    @Then("I no longer see {string}")
    fun noLongerSee(text: String) = world.waitForGone(hasText(text))
}
