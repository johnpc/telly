package com.johncorser.telly.e2e.steps

import android.view.KeyEvent
import com.johncorser.telly.e2e.TellyWorld
import com.johncorser.telly.e2e.fixtures.FixturePlan
import com.johncorser.telly.e2e.fixtures.FixtureProgramme
import com.johncorser.telly.e2e.fixtures.FixtureServer
import com.johncorser.telly.features.playback.ProgramTimes
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import java.util.TimeZone

/** Steps for programme reminders (guide dropdown + settings pane). */
class ReminderSteps(
    private val world: TellyWorld,
) {
    /** The future programme the scenario set its reminder on. */
    private var remembered: FixtureProgramme? = null

    /**
     * Two RIGHTs from the airing cell land on a programme at least ~30 min
     * out, so the reminder can never fire (default 5-min lead) mid-scenario.
     */
    @When("I open the dropdown on a later programme of channel {int}")
    fun openDropdownOnLaterProgramme(number: Int) {
        remembered = futureProgramme(number, skip = 2)
        world.pressKey(KeyEvent.KEYCODE_DPAD_RIGHT, times = 2)
        world.pressKey(KeyEvent.KEYCODE_DPAD_CENTER)
        // "Record" is label-stable; the Remind row relabels with state.
        world.waitForText("Record")
    }

    @Then("the reminders pane lists the remembered programme on {string}")
    fun paneListsRemembered(channelName: String) {
        val programme = requireNotNull(remembered) { "no reminder was set in this scenario" }
        world.waitForText(programme.title, substring = true)
        world.waitForText(channelName, substring = true)
        world.waitForText(ProgramTimes.clock(programme.startMs, TimeZone.getDefault()), substring = true)
    }

    @When("I activate the remembered reminder")
    fun activateRemembered() {
        world.select(requireNotNull(remembered).title)
    }

    /** The programme [skip] future cells right of the airing one. */
    private fun futureProgramme(
        channelNumber: Int,
        skip: Int,
    ): FixtureProgramme {
        val tvgId = FixturePlan.channels[channelNumber - 1].tvgId
        val now = System.currentTimeMillis()
        return FixtureServer.programmes
            .filter { it.channelTvgId == tvgId && it.startMs > now }
            .sortedBy { it.startMs }[skip - 1]
    }
}
