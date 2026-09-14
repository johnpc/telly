package com.johncorser.telly.e2e.steps

import android.view.KeyEvent
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.isSelected
import com.johncorser.telly.e2e.PlaybackDriver
import com.johncorser.telly.e2e.TellyWorld
import com.johncorser.telly.e2e.fixtures.FixturePlan
import com.johncorser.telly.e2e.fixtures.FixtureServer
import com.johncorser.telly.e2e.fixtures.rangeText
import io.cucumber.java.en.Then
import io.cucumber.java.en.When

/** Steps for the channel-list panel: groups, rows, favorites, hiding. */
class PanelSteps(
    private val world: TellyWorld,
    private val driver: PlaybackDriver,
) {
    @When("I open the channel panel")
    fun openPanel() = driver.openPanel()

    @Then("the channel list panel opens over the dimmed video")
    fun panelOpen() {
        world.waitForText("All channels")
        world.waitForText("Favorites")
    }

    @Then("^the groups column lists (.+)$")
    fun groupsColumnLists(rawList: String) {
        quotedNames(rawList).forEach { world.waitForText(it) }
    }

    @Then("{string} is the selected group")
    fun selectedGroup(name: String) = world.waitFor(hasText(name) and isSelected())

    @Then("each visible channel row shows its number, logo, name and current programme with progress")
    fun visibleRowsComplete() {
        val now = System.currentTimeMillis()
        FixturePlan.channels.take(VISIBLE_ROW_SAMPLE).forEach { channel ->
            val programme = FixtureServer.nowProgramme(channel.tvgId, now)
            world.waitFor(
                hasTestTag("channel-row")
                    .and(hasText(channel.name))
                    .and(hasText(channel.number.toString()))
                    .and(hasText(programme.displayTitle)),
            )
            world.waitFor(hasContentDescription("${channel.name} logo"), unmerged = true)
        }
        world.waitFor(hasTestTag("progress-bar"), unmerged = true, atLeast = VISIBLE_ROW_SAMPLE)
    }

    @Then("the focused row expands into a detail card with times and description")
    fun focusedRowDetail() {
        val channel = driver.currentChannel
        val programme = FixtureServer.nowProgramme(channel.tvgId, System.currentTimeMillis())
        world.waitFor(driver.rowMatcher(channel.name) and isFocused())
        world.waitForText(programme.rangeText(), substring = true)
        world.waitForText(programme.description, substring = true)
    }

    @Then("the focused channel row is number {int} {string}")
    fun focusedRowIs(
        number: Int,
        name: String,
    ) = world.waitFor(driver.rowMatcher(name) and hasText(number.toString()) and isFocused())

    @Then("the {string} row shows number {int}")
    fun rowShowsNumber(
        name: String,
        number: Int,
    ) = world.waitFor(driver.rowMatcher(name) and hasText(number.toString()))

    @When("I select the row {string}")
    fun selectRow(name: String) {
        driver.focusRow(name)
        world.pressKey(KeyEvent.KEYCODE_DPAD_CENTER)
    }

    @When("I focus the row {string}")
    fun focusRow(name: String) = driver.focusRow(name)

    @When("I long-press ok on the row {string}")
    fun longPressRow(name: String) {
        driver.focusRow(name)
        menuChannelName = name
        world.longPressOk()
        world.waitForText("Search")
    }

    @Then("the panel is dismissed and the zap overlay announces channel {int} {string}")
    fun panelDismissedZap(
        number: Int,
        name: String,
    ) {
        world.waitForGone(hasText("All channels"))
        driver.assertZapOverlayShows(number, name)
    }

    @Then("I see the menu rows {string} and {string}")
    fun menuRows(
        first: String,
        second: String,
    ) = listOf(first, second).forEach { world.waitForText(it) }

    @Then("I see a blue programme section with {string} and {string}")
    fun programmeSection(
        first: String,
        second: String,
    ) {
        val channel = FixturePlan.channelNamed(requireNotNull(menuChannelName))
        val programme = FixtureServer.nowProgramme(channel.tvgId, System.currentTimeMillis())
        world.waitForText(programme.displayTitle, substring = true)
        listOf(first, second).forEach { world.waitForText(it) }
    }

    @Then("I see a blue channel section {string} with {string} and {string}")
    fun channelSection(
        channelName: String,
        first: String,
        second: String,
    ) = listOf(channelName, first, second).forEach { world.waitForText(it) }

    @Then("I see a blue {string} section with {string} and {string}")
    fun namedSection(
        header: String,
        first: String,
        second: String,
    ) = listOf(header, first, second).forEach { world.waitForText(it) }

    @Then("the channels column lists exactly {string}")
    fun channelsExactly(name: String) {
        world.waitFor(driver.rowMatcher(name))
        driver.awaitCondition("exactly one channel row") { world.nodeCount(hasTestTag("channel-row")) == 1 }
    }

    @Then("the channels column no longer lists {string}")
    fun channelsWithout(name: String) = world.waitForGone(driver.rowMatcher(name))

    private var menuChannelName: String? = null

    private fun quotedNames(raw: String): List<String> =
        Regex("\"([^\"]+)\"").findAll(raw).map { it.groupValues[1] }.toList()

    private companion object {
        const val VISIBLE_ROW_SAMPLE = 5
    }
}
