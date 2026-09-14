package com.johncorser.telly.e2e

import com.johncorser.telly.e2e.fixtures.FixtureServer
import io.cucumber.java.After
import io.cucumber.java.Before
import io.cucumber.java.Scenario
import java.io.File

/**
 * Per-scenario lifecycle: every scenario starts from a wiped app state
 * (fresh Room database, fresh preferences, fresh ServiceLocator) with the
 * fixture server re-anchored to "now"; failures leave a screenshot in the
 * app's external files dir for the CI artifact upload.
 */
class Hooks(
    private val world: TellyWorld,
) {
    @Before
    fun beforeScenario() {
        FixtureServer.reset()
        world.resetAppState()
    }

    @After
    fun afterScenario(scenario: Scenario) {
        if (scenario.isFailed) {
            saveScreenshot(scenario)
        }
        world.closeApp()
    }

    private fun saveScreenshot(scenario: Scenario) {
        runCatching {
            val dir = File(world.targetContext.getExternalFilesDir(null), "screenshots").apply { mkdirs() }
            val name = scenario.name.replace(Regex("[^A-Za-z0-9-]"), "_")
            world.device.takeScreenshot(File(dir, "$name.png"))
        }
    }
}
