package com.johncorser.telly.e2e

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import io.cucumber.junit.WithJunitRule
import org.junit.Rule

/**
 * Hosts the Compose test rule for cucumber scenarios. `@WithJunitRule` makes
 * cucumber-android run every scenario inside this rule; step-definition
 * classes receive the holder via constructor injection (picocontainer).
 * An *empty* rule attaches to whatever activity the steps launch, which is
 * what lets scenarios relaunch MainActivity mid-scenario.
 */
@WithJunitRule
class ComposeRuleHolder {
    @get:Rule
    val composeRule: ComposeTestRule = createEmptyComposeRule()
}
