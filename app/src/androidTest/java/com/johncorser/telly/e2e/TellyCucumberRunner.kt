package com.johncorser.telly.e2e

import io.cucumber.android.runner.CucumberAndroidJUnitRunner
import io.cucumber.junit.CucumberOptions

/**
 * Instrumentation entry point for the Gherkin acceptance harness. Feature
 * files are staged from e2e/features into the androidTest assets (see the
 * syncE2eAssets gradle task); step definitions live in this package.
 *
 * Filter by feature area with the standard cucumber `tags` argument, e.g.
 * `-Pandroid.testInstrumentationRunnerArguments.tags=@watch-and-zap`.
 */
@CucumberOptions(
    features = ["features"],
    glue = ["com.johncorser.telly.e2e"],
)
class TellyCucumberRunner : CucumberAndroidJUnitRunner()
