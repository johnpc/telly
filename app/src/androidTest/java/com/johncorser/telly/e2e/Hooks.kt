package com.johncorser.telly.e2e

import androidx.test.platform.app.InstrumentationRegistry
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
        if (scenario.isFailed || screenshotsForAll()) {
            saveScreenshot(scenario)
        }
        world.closeApp()
    }

    /**
     * Failure screenshots land in AGP's `additionalTestOutputDir`, which the
     * connected-test task pulls into
     * `app/build/outputs/connected_android_test_additional_output/` BEFORE
     * it uninstalls the app. The old location (the app's external files
     * dir) was wiped by that uninstall, so CI never actually got a single
     * screenshot. `-Pandroid.testInstrumentationRunnerArguments.screenshots=all`
     * captures the end state of every scenario (a UX review artifact).
     */
    private fun saveScreenshot(scenario: Scenario) {
        runCatching {
            val arguments = InstrumentationRegistry.getArguments()
            val root =
                arguments.getString("additionalTestOutputDir")?.let(::File)
                    ?: File(world.targetContext.getExternalFilesDir(null), "screenshots")
            val dir = File(root, "screenshots").apply { mkdirs() }
            val status = if (scenario.isFailed) "FAILED" else "passed"
            val name = scenario.name.replace(Regex("[^A-Za-z0-9-]"), "_")
            world.device.takeScreenshot(File(dir, "$status-$name.png"))
        }
    }

    private fun screenshotsForAll(): Boolean = InstrumentationRegistry.getArguments().getString("screenshots") == "all"
}
