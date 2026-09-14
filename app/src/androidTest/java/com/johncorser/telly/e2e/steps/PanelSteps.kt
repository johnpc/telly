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
    fun selectedGroup(name: String) {
        if (world.nodeCount(hasText(name) and isSelected()) > 0) {
            return
        }
        // The guide renders group selection only inside its groups column:
        // peek at the column (LEFT) and close it again (RIGHT).
        world.pressKey(KeyEvent.KEYCODE_DPAD_LEFT)
        world.waitFor(hasText(name) and isSelected())
        world.pressKey(KeyEvent.KEYCODE_DPAD_RIGHT)
        world.waitForGone(hasText("All channels"))
    }

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
    ) {
        // Panel rows are tagged Surfaces; guide rows are plain text columns,
        // so fall back to a same-visual-row check for the number there.
        if (world.nodeCount(hasTestTag("channel-row")) > 0) {
            world.waitFor(driver.rowMatcher(name) and hasText(number.toString()))
        } else {
            driver.awaitCondition("number $number next to $name") {
                world.rowAligned(number.toString(), name)
            }
        }
    }

    @When("I select the row {string}")
    fun selectRow(name: String) {
        driver.focusRow(name)
        world.pressKey(KeyEvent.KEYCODE_DPAD_CENTER)
    }

    @When("I focus the row {string}")
    fun focusRow(name: String) = driver.focusRow(name)

    @When("I long-press ok on the row {string}")
    fun longPressRow(name: String) {
        menuChannelName = name
        driver.longPressRow(name)
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

    @Then("I see the menu row {string}")
    fun menuRow(text: String) = world.waitForText(text)

    @Then("^I see a blue programme section with (.+)$")
    fun programmeSection(rawList: String) {
        // From the panel the sheet belongs to the long-pressed row; from the
        // guide it belongs to the focused row (the tuned channel at open).
        val channel = FixturePlan.channelNamed(menuChannelName ?: driver.currentChannel.name)
        val programme = FixtureServer.nowProgramme(channel.tvgId, System.currentTimeMillis())
        world.waitForText(programme.displayTitle, substring = true)
        quotedNames(rawList).forEach { world.waitForText(it) }
    }

    @Then("I see a blue channel section {string} with {string} and {string}")
    fun channelSection(
        channelName: String,
        first: String,
        second: String,
    ) = listOf(channelName, first, second).forEach { world.waitForText(it) }

    @Then("^I see a blue \"([^\"]+)\" section with (.+)$")
    fun namedSection(
        header: String,
        rawList: String,
    ) {
        world.waitForText(header)
        quotedNames(rawList).forEach { world.waitForText(it) }
    }

    @Then("^the channels column lists exactly (.+)$")
    fun channelsExactly(rawList: String) {
        val names = quotedNames(rawList)
        names.forEach { world.waitForText(it) }
        if (world.nodeCount(hasTestTag("channel-row")) > 0) {
            // Panel rows are tagged Surfaces: count them directly.
            names.forEach { world.waitFor(driver.rowMatcher(it)) }
            driver.awaitCondition("exactly ${names.size} channel row(s)") {
                world.nodeCount(hasTestTag("channel-row")) == names.size
            }
        } else {
            // Guide rows are plain text columns: the listed names render
            // top-to-bottom and no other fixture channel appears at all.
            driver.awaitCondition("channel column ordered ${names.joinToString()}") {
                val tops = names.map { name -> world.boundsOf(hasText(name)).minOfOrNull { it.top } }
                tops.none { it == null } && tops.filterNotNull().zipWithNext().all { (above, below) -> above < below }
            }
            FixturePlan.channels
                .map { it.name }
                .filterNot { it in names }
                .forEach { absent -> driver.assertNow("$absent not listed", world.nodeCount(hasText(absent)) == 0) }
        }
    }

    @Then("the groups column does not list {string}")
    fun groupsColumnWithout(name: String) {
        driver.assertNow("$name not in the groups column", world.nodeCount(hasText(name)) == 0)
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
