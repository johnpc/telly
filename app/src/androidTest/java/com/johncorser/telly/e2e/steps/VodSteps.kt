package com.johncorser.telly.e2e.steps

import android.view.KeyEvent
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performSemanticsAction
import com.johncorser.telly.core.ServiceLocator
import com.johncorser.telly.e2e.PlaybackDriver
import com.johncorser.telly.e2e.TellyWorld
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import kotlinx.coroutines.runBlocking

/**
 * Steps for the VOD slice: classification out of the guide, the rail's
 * Movies icon -> browser, seekable playback and the Resume prompt.
 */
class VodSteps(
    private val world: TellyWorld,
    private val driver: PlaybackDriver,
) {
    @Then("the playlist imported {int} live channels and {int} VOD items")
    fun importedCounts(
        channels: Int,
        vodItems: Int,
    ) {
        driver.awaitCondition("$channels channels + $vodItems VOD items in Room") {
            val db = ServiceLocator.database(world.targetContext)
            runBlocking { db.channelDao().totalCount() == channels && db.vodItemDao().totalCount() == vodItems }
        }
    }

    // "the groups column does not list {string}" reuses PanelSteps' step.
    @When("I open the guide's groups column")
    fun openGroupsColumn() = openGuideGroups()

    @When("I open the VOD browser from the guide rail")
    fun openVodBrowser() {
        openGuideGroups()
        world.waitFor(railMovies())
        world.compose.onAllNodes(railMovies()).onFirst().performSemanticsAction(SemanticsActions.RequestFocus)
        world.waitFor(railMovies() and isFocused())
        world.pressKey(KeyEvent.KEYCODE_DPAD_CENTER)
        world.waitFor(hasTestTag("vod-card"))
    }

    @Then("I see the VOD item card {string}")
    fun seeVodCard(name: String) = world.waitFor(vodCard(name))

    @When("I play the VOD item {string}")
    fun playVodItem(name: String) {
        world.waitFor(vodCard(name))
        world.compose.onAllNodes(vodCard(name)).onFirst().performSemanticsAction(SemanticsActions.RequestFocus)
        world.waitFor(vodCard(name) and isFocused())
        world.pressKey(KeyEvent.KEYCODE_DPAD_CENTER)
    }

    @Then("VOD playback starts with the transport visible")
    fun transportVisible() = world.waitFor(hasTestTag("vod-transport"), unmerged = true)

    @Then("the transport shows the title {string}")
    fun transportShowsTitle(title: String) {
        world.waitFor(hasTestTag("vod-transport") and hasAnyDescendant(hasText(title)), unmerged = true)
    }

    @Then("the VOD item card {string} shows watch progress")
    fun cardShowsProgress(name: String) {
        world.waitFor(vodCard(name))
        world.waitFor(hasTestTag("vod-card-progress"), unmerged = true)
    }

    /** BACK to the guide root, then LEFT into the groups column + rail. */
    private fun openGuideGroups() {
        driver.dismissChrome()
        world.pressKey(KeyEvent.KEYCODE_BACK)
        world.waitForText("News One")
        world.pressKey(KeyEvent.KEYCODE_DPAD_LEFT)
        world.waitForText("All channels")
    }

    private fun railMovies(): SemanticsMatcher = hasTestTag("rail-movies")

    private fun vodCard(name: String): SemanticsMatcher = hasTestTag("vod-card") and hasText(name)
}
