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
import com.johncorser.telly.e2e.PlaybackDriver
import com.johncorser.telly.e2e.TellyWorld
import com.johncorser.telly.e2e.fixtures.FixturePlan
import com.johncorser.telly.e2e.fixtures.FixtureServer
import com.johncorser.telly.e2e.fixtures.rangeText
import io.cucumber.java.en.Then
import io.cucumber.java.en.When

/**
 * Steps for the corrected History model (history-round2): the info
 * overlay's recent-channel cards + Clear, and the full-screen History list.
 */
class HistorySteps(
    private val world: TellyWorld,
    private val driver: PlaybackDriver,
) {
    @Then("I see the {string} card")
    fun seeCard(label: String) = world.waitForText(label)

    @Then("I see {int} recent-channel cards with the current programmes of {string} and {string}")
    fun seeRecentCards(
        count: Int,
        first: String,
        second: String,
    ) {
        driver.awaitCondition("$count recent-channel cards") {
            world.nodeCount(hasTestTag("recent-card")) == count
        }
        listOf(first, second).forEach { name -> world.waitForText(nowProgrammeOf(name).displayTitle) }
    }

    @When("I focus the first recent-channel card")
    fun focusFirstRecentCard() {
        world.waitFor(recentCard())
        world.compose.onAllNodes(recentCard()).onFirst().performSemanticsAction(SemanticsActions.RequestFocus)
        world.waitFor(recentCard() and isFocused())
    }

    @When("I select the first recent-channel card")
    fun selectFirstRecentCard() {
        focusFirstRecentCard()
        world.pressKey(KeyEvent.KEYCODE_DPAD_CENTER)
    }

    @Then("the focused recent card shows the air time of {string}")
    fun focusedRecentCardLine(name: String) {
        val programme = nowProgrammeOf(name)
        world.waitForText("${programme.rangeText()}   ${programme.displayTitle}")
    }

    @Then("no recent-channel cards are visible")
    fun noRecentCards() {
        driver.awaitCondition("recent-channel cards gone") { world.nodeCount(recentCard()) == 0 }
    }

    @Then("the History screen opens")
    fun historyScreenOpens() {
        world.waitFor(hasContentDescription(CLEAR_ALL))
    }

    @Then("^the History screen lists exactly (.+)$")
    fun historyListsExactly(rawList: String) {
        val names = Regex("\"([^\"]+)\"").findAll(rawList).map { it.groupValues[1] }.toList()
        names.forEach { world.waitFor(historyRow(it)) }
        driver.awaitCondition("exactly ${names.size} history row(s)") {
            world.nodeCount(hasTestTag("history-row")) == names.size
        }
        driver.awaitCondition("history rows ordered ${names.joinToString()}") {
            val tops = names.map { name -> world.boundsOf(historyRow(name)).minOfOrNull { it.top } }
            tops.none { it == null } && tops.filterNotNull().zipWithNext().all { (above, below) -> above < below }
        }
    }

    @When("I select the History row {string}")
    fun selectHistoryRow(name: String) {
        world.waitFor(historyRow(name))
        world.compose.onAllNodes(historyRow(name)).onFirst().performSemanticsAction(SemanticsActions.RequestFocus)
        world.waitFor(historyRow(name) and isFocused())
        world.pressKey(KeyEvent.KEYCODE_DPAD_CENTER)
    }

    @When("I select the History clear-all icon")
    fun selectClearAll() = world.select(CLEAR_ALL)

    private fun recentCard(): SemanticsMatcher = hasTestTag("recent-card")

    private fun historyRow(name: String): SemanticsMatcher = hasTestTag("history-row") and hasText(name)

    private fun nowProgrammeOf(name: String) =
        FixtureServer.nowProgramme(FixturePlan.channelNamed(name).tvgId, System.currentTimeMillis())

    private companion object {
        const val CLEAR_ALL = "Clear history"
    }
}
