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
import io.cucumber.java.en.Then
import io.cucumber.java.en.When

/**
 * Steps for the My list / favorites-management slice: the My List screen
 * behind the rail bookmark, and the Manage Favorites / Reorder channels
 * editor screens.
 */
class MyListSteps(
    private val world: TellyWorld,
    private val driver: PlaybackDriver,
) {
    @When("I select the My List row {string}")
    fun selectMyListRow(channelName: String) {
        focusAndPressOk(myListRow(channelName))
    }

    @Then("the My List rows are the upcoming programme of {string} then the current programme of {string}")
    fun myListRowsOrdered(
        upcomingChannel: String,
        currentChannel: String,
    ) {
        val upcoming = FixtureServer.nextProgramme(tvgIdOf(upcomingChannel), now()).displayTitle
        val current = FixtureServer.nowProgramme(tvgIdOf(currentChannel), now()).displayTitle
        assertListedAbove(
            myListRow(upcomingChannel) and hasText(upcoming),
            myListRow(currentChannel) and hasText(current),
        )
    }

    @Then("I see the description of the upcoming programme of {string}")
    fun upcomingDescription(channelName: String) {
        world.waitForText(FixtureServer.nextProgramme(tvgIdOf(channelName), now()).description, substring = true)
    }

    @When("I focus the channel row {string}")
    fun focusChannelRow(name: String) {
        focusOn(channelEditRow(name))
    }

    @Then("the channel row {string} is marked favorite")
    fun channelRowFavorite(name: String) {
        world.waitFor(channelEditRow(name) and hasContentDescription("Favorite"))
    }

    @Then("the channel row {string} is listed above {string}")
    fun channelRowAbove(
        above: String,
        below: String,
    ) = assertListedAbove(channelEditRow(above), channelEditRow(below))

    @Then("the channel column lists {string} above {string}")
    fun channelColumnOrder(
        above: String,
        below: String,
    ) = assertListedAbove(hasText(above), hasText(below))

    private fun myListRow(channelName: String): SemanticsMatcher = hasTestTag("mylist-row") and hasText(channelName)

    private fun channelEditRow(name: String): SemanticsMatcher = hasTestTag("channel-edit-row") and hasText(name)

    private fun focusOn(matcher: SemanticsMatcher) {
        world.waitFor(matcher)
        world.compose.onAllNodes(matcher).onFirst().performSemanticsAction(SemanticsActions.RequestFocus)
        world.waitFor(matcher and isFocused())
    }

    private fun focusAndPressOk(matcher: SemanticsMatcher) {
        focusOn(matcher)
        world.pressKey(KeyEvent.KEYCODE_DPAD_CENTER)
    }

    private fun assertListedAbove(
        above: SemanticsMatcher,
        below: SemanticsMatcher,
    ) {
        world.waitFor(above)
        world.waitFor(below)
        driver.awaitCondition("${above.description} above ${below.description}") {
            val aboveTop = world.boundsOf(above).minOfOrNull { it.top }
            val belowTop = world.boundsOf(below).minOfOrNull { it.top }
            aboveTop != null && belowTop != null && aboveTop < belowTop
        }
    }

    private fun tvgIdOf(channelName: String): String = FixturePlan.channelNamed(channelName).tvgId

    private fun now(): Long = System.currentTimeMillis()
}
