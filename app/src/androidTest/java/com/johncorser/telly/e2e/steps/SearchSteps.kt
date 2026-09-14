package com.johncorser.telly.e2e.steps

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextClearance
import com.johncorser.telly.e2e.PlaybackDriver
import com.johncorser.telly.e2e.TellyWorld
import com.johncorser.telly.e2e.fixtures.FixturePlan
import com.johncorser.telly.e2e.fixtures.FixtureProgramme
import com.johncorser.telly.e2e.fixtures.FixtureServer
import com.johncorser.telly.e2e.fixtures.rangeText
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.junit.Assert.assertTrue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/** Steps for the search screen (Route.Search, quick-bar Search slot). */
class SearchSteps(
    private val world: TellyWorld,
    private val driver: PlaybackDriver,
) {
    @When("I press ok on the quick-bar {string} slot")
    fun okOnQuickBarSlot(slot: String) = world.select(slot)

    @Then("the voice orb is focused")
    fun voiceOrbFocused() = world.waitFor(hasContentDescription("Voice search") and isFocused())

    @Then("the query bar shows the hint {string}")
    fun queryHint(hint: String) = world.waitForText(hint)

    @Then("I see the {string} header with a trash icon")
    fun historyHeader(header: String) {
        world.waitForText(header)
        world.waitFor(hasContentDescription("Clear search history"))
    }

    @Then("the empty state reads {string}")
    fun emptyState(text: String) = world.waitForText(text)

    @When("I focus the query bar")
    fun focusQueryBar() {
        world.waitFor(queryField())
        world.compose.onAllNodes(queryField()).onFirst().performSemanticsAction(SemanticsActions.RequestFocus)
        world.waitFor(queryField() and isFocused())
    }

    @Then("^the \"Channels\" shelf lists (.+)$")
    fun channelsShelfLists(rawList: String) {
        val names = Regex("\"([^\"]+)\"").findAll(rawList).map { it.groupValues[1] }.toList()
        names.forEach { world.waitForText(it) }
        // Name order (live tm-02). The same channel names repeat lower down
        // as programme-row cards, so compare card positions within the
        // shelf band only — the visual row of the first card's name.
        driver.awaitCondition("name-ordered shelf") {
            val bandTop = world.boundsOf(hasText(names.first())).minOfOrNull { it.top } ?: return@awaitCondition false
            val lefts =
                names.map { name ->
                    world
                        .boundsOf(hasText(name))
                        .filter { abs(it.top - bandTop) < BAND_TOLERANCE_PX }
                        .minOfOrNull { it.left } ?: return@awaitCondition false
                }
            lefts.zipWithNext().all { (left, right) -> left < right }
        }
    }

    @Then("every channel card shows its logo, name, current programme and progress")
    fun channelCardsComplete() {
        val now = System.currentTimeMillis()
        newsChannels().forEach { channel ->
            world.waitForText(channel.name)
            world.waitFor(hasContentDescription("${channel.name} logo"), unmerged = true)
            world.waitForText(FixtureServer.nowProgramme(channel.tvgId, now).displayTitle, substring = true)
        }
        world.waitFor(hasTestTagProgress(), unmerged = true, atLeast = newsChannels().size)
    }

    @Then("the {string} shelf includes channel {int} {string}")
    fun shelfIncludes(
        shelf: String,
        number: Int,
        name: String,
    ) {
        world.waitForText(shelf)
        world.waitForText(name)
    }

    @Then("the {string} list shows {string} rows ordered by start time")
    fun programmesOrdered(
        header: String,
        title: String,
    ) {
        world.waitForText(header)
        val hits = upcomingTitled(title)
        assertTrue("fixtures contain at least two upcoming '$title'", hits.size >= 2)
        world.waitFor(hasText(title, substring = true), atLeast = 2)
        world.waitForText(airTime(hits[0]), substring = true)
        world.waitForText(airTime(hits[1]), substring = true)
        val first = world.boundsOf(hasText(airTime(hits[0]), substring = true)).minOfOrNull { it.top }
        val second = world.boundsOf(hasText(airTime(hits[1]), substring = true)).minOfOrNull { it.top }
        if (first != null && second != null) {
            assertTrue("rows render chronologically", first <= second)
        }
    }

    @Then("rows airing today show a time range like {string}")
    fun todayRows(example: String) {
        world.waitFor(world.hasTextMatching(Regex("\\d{2}:\\d{2}( [AP]M)? — \\d{2}:\\d{2} [AP]M")))
    }

    @Then("rows airing another day are prefixed like {string}")
    fun otherDayRows(example: String) {
        world.waitFor(
            world.hasTextMatching(
                Regex("[A-Z][a-z]{2}, [A-Z][a-z]{2} \\d{1,2}, \\d{2}:\\d{2}( [AP]M)? — \\d{2}:\\d{2} [AP]M"),
            ),
        )
    }

    @Then("the focused row shows a detail card with title, times and description")
    fun focusedRowDetailCard() {
        // The app preselects its first hit into the card; several fixture
        // programmes can share that startMs on different channels, so accept
        // any tied candidate's description (the DAO's tie order is free).
        val candidates = upcomingTitled("Newsroom Live")
        val first = candidates.first()
        val ties = candidates.filter { it.startMs == first.startMs }
        focusFirstProgrammeRow(hasText(airTime(first), substring = true))
        world.waitForText(first.displayTitle, substring = true)
        world.waitForText(airTime(first), substring = true)
        driver.awaitCondition("detail card shows the focused row's description") {
            ties.any { world.nodeCount(hasText(it.description, substring = true)) > 0 }
        }
    }

    @When("I press ok on the channel card {string}")
    fun okOnChannelCard(name: String) = world.select(name)

    @When("I press ok on the first programme row")
    fun okOnFirstProgrammeRow() {
        // A synthetic DPAD_CENTER pair can leak its UP into the dropdown
        // that the row's click opens (its first row takes focus while the
        // key is in flight); the semantics click IS the row's OK action.
        focusFirstProgrammeRow(hasText("Newsroom Live", substring = true))
            .performSemanticsAction(SemanticsActions.OnClick)
    }

    private fun focusFirstProgrammeRow(text: SemanticsMatcher): SemanticsNodeInteraction {
        val focusable = text and SemanticsMatcher.keyIsDefined(SemanticsActions.RequestFocus)
        world.waitFor(focusable)
        val row = world.compose.onAllNodes(focusable).onFirst()
        row.performSemanticsAction(SemanticsActions.RequestFocus)
        world.waitFor(focusable and isFocused())
        return row
    }

    @Then("I see the dropdown rows {string}, {string}, {string}, {string}, {string}")
    fun dropdownRows(
        a: String,
        b: String,
        c: String,
        d: String,
        e: String,
    ) = listOf(a, b, c, d, e).forEach { world.waitForText(it) }

    @When("I press the IME search action")
    fun pressImeSearch() {
        world.compose.onAllNodes(queryField()).onFirst().performImeAction()
        world.compose.waitForIdle()
    }

    @When("I clear the query")
    fun clearQuery() {
        world.compose.onAllNodes(queryField()).onFirst().performTextClearance()
        world.compose.waitForIdle()
    }

    @Then("the history lists {string}")
    fun historyLists(entry: String) = world.waitForText(entry)

    @When("I press ok on the trash icon")
    fun okOnTrash() = world.select("Clear search history")

    private fun queryField(): SemanticsMatcher = SemanticsMatcher.keyIsDefined(SemanticsActions.SetText)

    private fun hasTestTagProgress(): SemanticsMatcher = hasTestTag("progress-bar")

    private fun newsChannels() = FixturePlan.channels.filter { it.group == "News" }

    private fun upcomingTitled(title: String): List<FixtureProgramme> {
        val now = System.currentTimeMillis()
        return FixtureServer.programmes
            .filter { it.title == title && it.endMs > now }
            .sortedBy { it.startMs }
    }

    /** Mirrors the reference air-time format (bare today, date-prefixed else). */
    private fun airTime(programme: FixtureProgramme): String {
        val range = programme.rangeText()
        val day = SimpleDateFormat("yyyy-DDD", Locale.US)
        if (day.format(Date(programme.startMs)) == day.format(Date())) return range
        return "${SimpleDateFormat("EEE, MMM d", Locale.US).format(Date(programme.startMs))}, $range"
    }

    private companion object {
        const val BAND_TOLERANCE_PX = 5f
    }
}
