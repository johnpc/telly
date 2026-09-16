package com.johncorser.telly.e2e.steps

import android.view.KeyEvent
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.isRoot
import com.johncorser.telly.e2e.PlaybackDriver
import com.johncorser.telly.e2e.TellyWorld
import com.johncorser.telly.e2e.fixtures.FixturePlan
import com.johncorser.telly.e2e.fixtures.FixtureProgramme
import com.johncorser.telly.e2e.fixtures.FixtureServer
import com.johncorser.telly.e2e.fixtures.rangeText
import com.johncorser.telly.features.guide.GuideGeometry
import com.johncorser.telly.features.guide.GuideTimeline
import com.johncorser.telly.features.playback.ClockStyle
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.junit.Assert.assertFalse
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Steps for the TV-guide grid (Route.Guide, the app's root screen). */
class GuideSteps(
    private val world: TellyWorld,
    private val driver: PlaybackDriver,
) {
    /** The time anchor guide focus keeps while navigating (starts at now). */
    private var anchorMs: Long = 0L

    /** Scenarios that never moved focus anchor at "now" on first use. */
    private fun anchorOrNow(): Long {
        if (anchorMs == 0L) anchorMs = System.currentTimeMillis()
        return anchorMs
    }

    private fun programmeAt(
        channelNumber: Int,
        atMs: Long,
    ): FixtureProgramme = FixtureServer.nowProgramme(FixturePlan.channels[channelNumber - 1].tvgId, atMs)

    @Then("the TV guide opens with the programme grid")
    fun guideOpen() {
        world.waitFor(world.hasTextMatching(TICK_LABEL))
        world.waitForText("News One")
    }

    @Then("I see the preview window playing channel {int} {string}")
    fun previewPlays(
        number: Int,
        name: String,
    ) {
        guideOpen()
        driver.awaitCondition("play marker on the $name row") { world.rowAligned("▶", name) }
        driver.currentChannel = FixturePlan.channelNamed(name)
    }

    @Then("the info pane shows the focused programme title, time range and description")
    fun infoPaneComplete() {
        val programme = programmeAt(1, System.currentTimeMillis())
        anchorMs = System.currentTimeMillis()
        world.waitForText(programme.displayTitle, substring = true)
        world.waitForText(programme.rangeText(), substring = true)
        world.waitForText(programme.description, substring = true)
    }

    @Then("the channel column lists number, logo and name for {string} and {string}")
    fun channelColumn(
        first: String,
        second: String,
    ) {
        listOf(first, second).forEach { name ->
            val channel = FixturePlan.channelNamed(name)
            world.waitForText(name)
            world.waitFor(hasContentDescription("$name logo"), unmerged = true)
            driver.awaitCondition("number ${channel.number} next to $name") {
                world.rowAligned(channel.number.toString(), name)
            }
        }
    }

    @Then("the grid shows the current and next programme cells of {string}")
    fun gridShowsNowAndNext(name: String) {
        val tvgId = FixturePlan.channelNamed(name).tvgId
        val now = System.currentTimeMillis()
        world.waitForText(FixtureServer.nowProgramme(tvgId, now).displayTitle, substring = true)
        world.waitForText(FixtureServer.nextProgramme(tvgId, now).displayTitle, substring = true)
    }

    @Then("channels without EPG show {string} cells")
    fun noEpgCells(placeholder: String) {
        world.waitForText(placeholder, substring = true)
    }

    /** After Assign EPG: the named row's cells come from the picked id. */
    @Then("the guide row of {string} shows the current programme of EPG id {string}")
    fun guideRowShowsEpgProgramme(
        name: String,
        tvgId: String,
    ) {
        val programme = FixtureServer.nowProgramme(tvgId, System.currentTimeMillis())
        val cell = world.hasTextMatching(Regex(".*" + Regex.escape(programme.title) + ".*"))
        driver.awaitCondition("the $name row shows ${programme.title}") {
            world.rowAlignedMatching(hasText(name), cell)
        }
    }

    @Then("the header clock shows today's date and time")
    fun headerClock() {
        // "Sun, Sep 14, 2:45 PM" — assert the date part (minutes drift) and
        // that a time follows it.
        val datePart = SimpleDateFormat("EEE, MMM d", Locale.US).format(Date())
        world.waitForText(datePart, substring = true)
        world.waitFor(world.hasTextMatching(Regex("$datePart, \\d{1,2}:\\d{2} [AP]M")))
    }

    @Then("the timeline shows labels every 30 minutes")
    fun timelineTicks() {
        world.waitFor(world.hasTextMatching(TICK_LABEL), atLeast = 2)
    }

    @Then("the now-line marks the current time in the grid")
    fun nowLine() {
        world.waitFor(hasTestTag("now-line"), unmerged = true)
    }

    @Then("the next programme cell of channel {int} is focused")
    fun nextCellFocused(number: Int) {
        val tvgId = FixturePlan.channels[number - 1].tvgId
        // Recomputed per poll so the expectation tracks a programme
        // boundary passing mid-wait; the range text renders ONLY in the
        // info pane, so seeing the AIRING range means the RIGHT that led
        // here fired before the grid's key anchor held focus — self-heal
        // with a bounded re-press (the wizard hand-off precedent) instead
        // of timing out.
        var represses = 0
        driver.awaitCondition("the next programme cell of channel $number focused") {
            val now = System.currentTimeMillis()
            val next = FixtureServer.nextProgramme(tvgId, now)
            if (world.nodeCount(hasText(next.rangeText(), substring = true)) > 0) {
                anchorMs = next.startMs
                true
            } else {
                val airing = FixtureServer.nowProgramme(tvgId, now)
                val stuckOnAiring = world.nodeCount(hasText(airing.rangeText(), substring = true)) > 0
                if (stuckOnAiring && represses < MAX_REPRESSES) {
                    represses++
                    world.pressKey(KeyEvent.KEYCODE_DPAD_RIGHT)
                }
                false
            }
        }
    }

    @Then("the info pane shows that programme's title")
    fun infoPaneShowsFocusedTitle() {
        val next = FixtureServer.nowProgramme(FixturePlan.channels.first().tvgId, anchorMs)
        // The title renders twice: once in the cell, once in the info pane.
        world.waitFor(hasText(next.displayTitle), atLeast = 2)
    }

    @Then("the focused cell is on channel {int} at roughly the same time")
    fun focusedCellOnChannel(number: Int) {
        world.waitForText(programmeAt(number, anchorOrNow()).rangeText(), substring = true)
    }

    @Then("the timeline header has scrolled forward with the cells")
    fun timelineScrolled() {
        val zone = TimeZone.getDefault()
        val originLabel =
            GuideTimeline.timeLabel(GuideGeometry.halfHourFloor(System.currentTimeMillis(), zone), ClockStyle(zone))
        world.waitFor(world.hasTextMatching(TICK_LABEL))
        driver.awaitCondition("origin tick label scrolled away") {
            world.nodeCount(hasText(originLabel)) == 0
        }
    }

    @When("I press ok on the airing programme")
    fun okOnAiringProgramme() {
        // Only airing cells put the remaining-minutes pill in the info pane.
        world.waitFor(world.hasTextMatching(REMAINING_LABEL))
        world.pressKey(KeyEvent.KEYCODE_DPAD_CENTER)
    }

    @Then("the preview window plays channel {int} {string}")
    fun previewSwitches(
        number: Int,
        name: String,
    ) {
        driver.awaitCondition("play marker moves to $name") { world.rowAligned("▶", name) }
        driver.currentChannel = FixturePlan.channelNamed(name)
    }

    @Then("the channel name of row {int} renders in accent blue with a play marker")
    fun playingRowMarker(number: Int) {
        driver.awaitCondition("play marker on row $number") {
            world.rowAligned("▶", FixturePlan.channels[number - 1].name)
        }
    }

    @Then("playback goes fullscreen on channel {int} {string}")
    fun fullscreenOn(
        number: Int,
        name: String,
    ) = driver.assertPlaybackOn(number, name)

    @Then("a dropdown anchored under the cell lists exactly {string}, {string}, {string}, {string}, {string}")
    fun dropdownLists(
        a: String,
        b: String,
        c: String,
        d: String,
        e: String,
    ) = listOf(a, b, c, d, e).forEach { world.waitForText(it) }

    @Then("I see the {string} screen")
    fun seeScreen(title: String) = world.waitForText(title)

    @Then("the programme grid is focused again")
    fun gridFocusedAgain() {
        world.waitForGone(hasText("Coming soon to telly"))
        world.waitForGone(hasText("Remind"))
        // "Channel options" only exists on the row context sheet; its
        // absence proves the sheet fully closed over the still-visible grid.
        world.waitForGone(hasText("Channel options"))
        world.waitFor(world.hasTextMatching(TICK_LABEL))
    }

    @Then("a right-side sheet opens with the guide grid still visible behind it")
    fun sheetOverGrid() {
        world.waitForText("Channel options")
        world.waitFor(world.hasTextMatching(TICK_LABEL))
        driver.awaitCondition("sheet anchored to the right half") {
            val bounds = world.boundsOf(hasText("Channel options"))
            bounds.isNotEmpty() && bounds.all { it.left > rootCenterX() }
        }
    }

    @Then("the channel column no longer lists {string}")
    fun channelColumnWithout(name: String) = world.waitForGone(hasText(name))

    @Then("the description layer shows the focused programme's title and synopsis")
    fun descriptionLayer() {
        // The sheet opens on the focused row = the tuned channel's airing
        // cell; the synopsis is that programme's fixture description (a
        // fixed string would rot as the airing programme rotates with the
        // wall clock).
        val programme = FixtureServer.nowProgramme(driver.currentChannel.tvgId, System.currentTimeMillis())
        world.waitForText(programme.displayTitle, substring = true)
        world.waitForText(programme.description, substring = true)
    }

    @Then("a right pane titled {string} opens")
    fun rightPaneTitled(title: String) {
        // The channel column lists the same name at the left edge, so the
        // pane's copy must sit in the right half of the screen.
        driver.awaitCondition("right pane titled $title") {
            world.boundsOf(hasText(title)).any { it.left > rootCenterX() }
        }
    }

    @Then("^the pane lists the rows (.+)$")
    fun paneRowsList(rawList: String) {
        Regex("\"([^\"]+)\"")
            .findAll(rawList)
            .map { it.groupValues[1] }
            .forEach { world.waitFor(hasText(it) and isEnabled()) }
    }

    private fun rootCenterX(): Float = world.boundsOf(isRoot()).maxOf { it.right } / 2

    @Then("the groups column is dismissed")
    fun groupsDismissed() = world.waitForGone(hasText("Favorites"))

    @Then("the {string} rail icon has focus")
    fun railIconFocused(description: String) {
        // The icon's semantics don't merge into the Surface, so match the
        // focused button by its (unmerged) content description child.
        world.waitFor(hasAnyDescendant(hasContentDescription(description)) and isFocused(), unmerged = true)
    }

    @Then("focus returns to the groups column")
    fun groupsColumnFocused() {
        driver.awaitCondition("a groups-column row focused") {
            listOf("Favorites", "All channels").any { world.nodeCount(hasText(it) and isFocused()) > 0 }
        }
    }

    @Then("telly exits to the launcher")
    fun appExits() {
        driver.awaitCondition("activity destroyed") { world.appDestroyed() }
    }

    @Then("telly is still running")
    fun appStillRunning() {
        world.compose.waitForIdle()
        assertFalse("the exit-confirm warning must not exit", world.appDestroyed())
    }

    @Then("the header clock shows today's date and a 24-hour time")
    fun headerClock24h() {
        val datePart = SimpleDateFormat("EEE, MMM d", Locale.US).format(Date())
        world.waitFor(world.hasTextMatching(Regex("$datePart, \\d{2}:\\d{2}")))
    }

    @Then("the timeline shows 24-hour labels every 30 minutes")
    fun timelineTicks24h() {
        world.waitFor(world.hasTextMatching(TICK_LABEL_24H), atLeast = 2)
    }

    private companion object {
        val TICK_LABEL = Regex("\\d{2}:(00|30) [AP]M")
        val TICK_LABEL_24H = Regex("\\d{2}:(00|30)")
        val REMAINING_LABEL = Regex("\\d+ min")
        const val MAX_REPRESSES = 3
    }
}
