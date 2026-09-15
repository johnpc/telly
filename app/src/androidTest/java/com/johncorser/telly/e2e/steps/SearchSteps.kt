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
import com.johncorser.telly.e2e.fixtures.FixtureChannel
import com.johncorser.telly.e2e.fixtures.FixturePlan
import com.johncorser.telly.e2e.fixtures.FixtureProgramme
import com.johncorser.telly.e2e.fixtures.FixtureServer
import com.johncorser.telly.e2e.fixtures.rangeText
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.junit.Assert.assertEquals
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
    /** The Programs master-lane card whose airings the rows pane shows. */
    private var selectedCardName: String? = null
    private var airingsTitle: String = "Newsroom Live"

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
        // as Programs master-lane cards, so compare card positions within
        // the shelf band only — the visual row of the first card's name.
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

    @Then("the Programs lane lists one card per channel airing {string}, in name order")
    fun programsLane(title: String) {
        world.waitForText("Programs")
        val names = channelsAiring(title).map { it.name }
        airingsTitle = title
        selectedCardName = names.first()
        // Order check on the two above-the-fold cards first (a programme-only
        // query renders channel names nowhere else), then reveal the rest by
        // scrolling the lane.
        world.waitForText(names[0])
        world.waitForText(names[1])
        driver.awaitCondition("master lane cards in name order") {
            val first = world.boundsOf(hasText(names[0])).minOfOrNull { it.top } ?: return@awaitCondition false
            val second = world.boundsOf(hasText(names[1])).minOfOrNull { it.top } ?: return@awaitCondition false
            first < second
        }
        names.drop(2).forEach { world.waitForText(it) }
    }

    @Then("the airings pane lists only the selected channel's {string} airings chronologically, repeats included")
    fun airingsPaneShowsSelected(title: String) {
        airingsTitle = title
        val channel = selectedChannel(title)
        val expected = airingsOn(channel, title)
        assertTrue("fixtures give ${channel.name} at least two upcoming '$title'", expected.size >= 2)
        // Two rows with the same title = repeats are never deduped; check
        // chronology on the first pair before scrolling uncomposes them.
        world.waitForText(airTime(expected[0]), substring = true)
        world.waitForText(airTime(expected[1]), substring = true)
        driver.awaitCondition("airings render chronologically") {
            val first = world.boundsOf(hasText(airTime(expected[0]), substring = true)).minOfOrNull { it.top }
            val second = world.boundsOf(hasText(airTime(expected[1]), substring = true)).minOfOrNull { it.top }
            first != null && second != null && first <= second
        }
        expected.drop(2).forEach { world.waitForText(airTime(it), substring = true) }
        assertNoForeignAiring(channel, title)
    }

    @Then("each airing row shows its reference air time")
    fun airingRowsShowReferenceTimes() {
        airingsOn(selectedChannel(airingsTitle), airingsTitle).forEach {
            world.waitForText(airTime(it), substring = true)
        }
    }

    @Then("the detail card pre-renders the selected channel's first airing")
    fun detailCardPreRenders() {
        // Pre-rendered = visible without any airing row ever taking focus;
        // the description renders only inside the detail card.
        val first = airingsOn(selectedChannel(airingsTitle), airingsTitle).first()
        world.waitForText(first.displayTitle, substring = true)
        world.waitForText(airTime(first), substring = true)
        world.waitForText(first.description, substring = true)
    }

    @When("I focus the Programs channel card {string}")
    fun focusProgramsCard(name: String) {
        world.focus(name)
        selectedCardName = name
    }

    @Then("the first channel card {string} is focused")
    fun firstChannelCardFocused(name: String) = world.waitFor(hasText(name) and isFocused())

    @Then("the first airing row of the selected channel is focused")
    fun firstAiringRowFocused() {
        val first = airingsOn(selectedChannel(airingsTitle), airingsTitle).first()
        world.waitFor(hasText(first.displayTitle, substring = true) and isFocused())
    }

    @When("I press ok on the Programs channel card {string}")
    fun okOnProgramsCard(name: String) = world.select(name)

    @When("I press ok on the channel card {string}")
    fun okOnChannelCard(name: String) = world.select(name)

    /**
     * OK on the selected channel's first airing at least ~30 min out, so a
     * Remind set here can never fire (default 5-min lead) mid-scenario —
     * the same guard the guide's reminders steps use.
     */
    @When("I press ok on a later programme row")
    fun okOnLaterProgrammeRow() {
        val later =
            airingsOn(selectedChannel(airingsTitle), airingsTitle)
                .first { it.startMs > now() + LATER_MARGIN_MS }
        // A synthetic DPAD_CENTER pair can leak its UP into the dropdown
        // that the row's click opens (its first row takes focus while the
        // key is in flight); the semantics click IS the row's OK action.
        focusFirstProgrammeRow(hasText(airTime(later), substring = true))
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

    /** The Programs master lane: channels with a matching airing, name order (ref-round6 §D). */
    private fun channelsAiring(title: String): List<FixtureChannel> {
        val tvgIds = FixtureServer.programmes.filter { it.title == title && it.endMs > now() }.map { it.channelTvgId }
        return FixturePlan.channels
            .filter { it.tvgId in tvgIds.toSet() }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
    }

    /** One channel's still-airing/upcoming matches, chronological, repeats kept. */
    private fun airingsOn(
        channel: FixtureChannel,
        title: String,
    ): List<FixtureProgramme> =
        FixtureServer.programmes
            .filter { it.channelTvgId == channel.tvgId && it.title == title && it.endMs > now() }
            .sortedBy { it.startMs }

    private fun selectedChannel(title: String): FixtureChannel =
        FixturePlan.channelNamed(selectedCardName ?: channelsAiring(title).first().name)

    /** A non-selected channel's airing time must never render in the rows pane. */
    private fun assertNoForeignAiring(
        selected: FixtureChannel,
        title: String,
    ) {
        val mine = airingsOn(selected, title).map { airTime(it) }.toSet()
        val foreign =
            channelsAiring(title)
                .filter { it.tvgId != selected.tvgId }
                .flatMap { airingsOn(it, title) }
                .map { airTime(it) }
                .firstOrNull { it !in mine } ?: return
        assertEquals(
            "only the selected channel's airings render",
            0,
            world.nodeCount(hasText(foreign, substring = true)),
        )
    }

    private fun now() = System.currentTimeMillis()

    /** Mirrors the reference air-time format (bare today, date-prefixed else). */
    private fun airTime(programme: FixtureProgramme): String {
        val range = programme.rangeText()
        val day = SimpleDateFormat("yyyy-DDD", Locale.US)
        if (day.format(Date(programme.startMs)) == day.format(Date())) return range
        return "${SimpleDateFormat("EEE, MMM d", Locale.US).format(Date(programme.startMs))}, $range"
    }

    private companion object {
        const val BAND_TOLERANCE_PX = 5f
        const val LATER_MARGIN_MS = 30 * 60_000L
    }
}
