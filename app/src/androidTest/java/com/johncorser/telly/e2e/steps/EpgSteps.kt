package com.johncorser.telly.e2e.steps

import com.johncorser.telly.core.ServiceLocator
import com.johncorser.telly.core.db.TellyDatabase
import com.johncorser.telly.e2e.PlaybackDriver
import com.johncorser.telly.e2e.TellyWorld
import com.johncorser.telly.e2e.fixtures.FixturePlan
import com.johncorser.telly.e2e.fixtures.FixtureProgramme
import com.johncorser.telly.e2e.fixtures.FixtureServer
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

/**
 * Steps for the EPG data foundation. These scenarios specify the persisted
 * guide DATA (Room), so assertions query the app's real database while the
 * app itself does the fetching/parsing/storing.
 */
class EpgSteps(
    private val world: TellyWorld,
    private val driver: PlaybackDriver,
) {
    private val database: TellyDatabase get() = ServiceLocator.database(world.targetContext)

    @When("the EPG update completes")
    fun epgUpdateCompletes() {
        driver.awaitCondition("EPG programmes stored", timeoutMs = 60_000L) {
            runBlocking { database.programDao().count() } > 0
        }
    }

    @Then("the guide data contains program titles from {string} for channel {string}")
    fun guideDataHasTitles(
        source: String,
        channelName: String,
    ) {
        val fixture =
            when (source) {
                "epg.xml" -> FixtureServer.programmes
                "epg-alt.xml" -> FixtureServer.altProgrammes
                else -> error("unexpected EPG source $source")
            }
        val channel = FixturePlan.channelNamed(channelName)
        val expected = fixture.filter { it.channelTvgId == channel.tvgId }
        // Refreshes triggered from settings run asynchronously; await the
        // stored schedule matching the served fixture instead of asserting
        // one racy snapshot.
        driver.awaitCondition("$channelName programmes match $source", timeoutMs = 60_000L) {
            storedWindow(channel.tvgId, expected) == expected.map { Triple(it.title, it.startMs, it.endMs) }
        }
    }

    private fun storedWindow(
        tvgId: String,
        expected: List<FixtureProgramme>,
    ): List<Triple<String, Long, Long>> =
        runBlocking {
            database
                .programDao()
                .observeWindow(listOf(tvgId), expected.first().startMs, expected.last().endMs)
                .first()
        }.map { Triple(it.details.title, it.startMs, it.endMs) }

    @Then("the program for {string} airing now has a start time, an end time and a description")
    fun airingNowComplete(channelName: String) {
        val channel = FixturePlan.channelNamed(channelName)
        val now = System.currentTimeMillis()
        val airing =
            runBlocking {
                database.programDao().observeWindow(listOf(channel.tvgId), now, now + 1).first()
            }.single()
        assertTrue("start before now", airing.startMs <= now)
        assertTrue("end after now", airing.endMs > now)
        assertTrue("description stored", !airing.details.description.isNullOrBlank())
    }

    @Then("channel {string} has number {int}")
    fun channelHasNumber(
        name: String,
        number: Int,
    ) {
        assertEquals(number, storedChannel(name).number)
    }

    @Given("I marked channel {string} as a favorite")
    fun markFavorite(name: String) {
        driver.openPanel()
        driver.longPressRow(name)
        world.select("Add to Favorites")
        driver.awaitCondition("favorite flag persisted") { storedChannel(name).flags.favorite }
        driver.dismissChrome()
    }

    @When("the playlist is refreshed from the same URL")
    fun refreshPlaylist() {
        val before = runBlocking { database.playlistDao().observeAll().first() }.single().lastUpdatedMs
        driver.openPanel()
        driver.longPressRow(driver.currentChannel.name)
        world.select("Settings")
        world.select("Playlists")
        world.select("127.0.0.1")
        world.select("Update playlist")
        driver.awaitCondition("playlist refresh completed") {
            runBlocking { database.playlistDao().observeAll().first() }.single().lastUpdatedMs > before
        }
    }

    @Then("channel {string} is still a favorite")
    fun stillFavorite(name: String) {
        driver.awaitCondition("channel $name still favorite") { storedChannel(name).flags.favorite }
    }

    private fun storedChannel(name: String) =
        runBlocking { database.channelDao().observeVisible().first() }.first { it.source.name == name }
}
