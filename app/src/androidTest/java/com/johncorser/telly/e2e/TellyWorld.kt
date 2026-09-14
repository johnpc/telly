package com.johncorser.telly.e2e

import android.content.Context
import android.os.SystemClock
import android.view.KeyEvent
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.johncorser.telly.MainActivity
import com.johncorser.telly.core.ServiceLocator
import com.johncorser.telly.core.db.TellyDatabase
import com.johncorser.telly.e2e.fixtures.FixtureServer

/**
 * Shared per-scenario state + the primitive gestures every step builds on:
 * launching the real MainActivity, injecting D-pad key events, and querying
 * the rendered Compose semantics. One instance per scenario (picocontainer).
 */
class TellyWorld(
    val composeHolder: ComposeRuleHolder,
) {
    val compose: ComposeTestRule get() = composeHolder.composeRule
    val device: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    val targetContext: Context = InstrumentationRegistry.getInstrumentation().targetContext

    private var scenario: ActivityScenario<MainActivity>? = null

    fun launchApp() {
        if (scenario == null) {
            scenario = ActivityScenario.launch(MainActivity::class.java)
        }
    }

    fun relaunchApp() {
        scenario?.close()
        scenario = null
        launchApp()
    }

    fun closeApp() {
        scenario?.close()
        scenario = null
    }

    /** Wipes Room + prefs and drops the ServiceLocator singletons. */
    fun resetAppState() {
        closeApp()
        resetServiceLocatorField("database") { (it as? TellyDatabase)?.close() }
        resetServiceLocatorField("settings") {}
        targetContext.deleteDatabase("telly.db")
        listOf("telly-settings", "telly").forEach { name ->
            targetContext.getSharedPreferences(name, Context.MODE_PRIVATE).edit().clear().commit()
        }
    }

    private fun resetServiceLocatorField(
        name: String,
        onValue: (Any?) -> Unit,
    ) {
        val field = ServiceLocator::class.java.getDeclaredField(name)
        field.isAccessible = true
        onValue(field.get(ServiceLocator))
        field.set(ServiceLocator, null)
    }

    // ---- keys ---------------------------------------------------------------

    fun pressKey(
        keyCode: Int,
        times: Int = 1,
    ) {
        repeat(times) {
            compose.waitForIdle()
            device.pressKeyCode(keyCode)
        }
        compose.waitForIdle()
    }

    /** Hold DPAD-center: down, auto-repeats while held, then release. */
    fun longPressOk() {
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val downTime = SystemClock.uptimeMillis()
        instrumentation.sendKeySync(centerEvent(downTime, KeyEvent.ACTION_DOWN, repeat = 0))
        repeat(LONG_PRESS_REPEATS) { index ->
            SystemClock.sleep(LONG_PRESS_STEP_MS)
            instrumentation.sendKeySync(centerEvent(downTime, KeyEvent.ACTION_DOWN, repeat = index + 1))
        }
        SystemClock.sleep(LONG_PRESS_STEP_MS)
        instrumentation.sendKeySync(centerEvent(downTime, KeyEvent.ACTION_UP, repeat = 0))
        compose.waitForIdle()
    }

    private fun centerEvent(
        downTime: Long,
        action: Int,
        repeat: Int,
    ): KeyEvent = KeyEvent(downTime, SystemClock.uptimeMillis(), action, KeyEvent.KEYCODE_DPAD_CENTER, repeat)

    // ---- semantics ----------------------------------------------------------

    fun nodeCount(
        matcher: SemanticsMatcher,
        unmerged: Boolean = false,
    ): Int = compose.onAllNodes(matcher, useUnmergedTree = unmerged).fetchSemanticsNodes(false).size

    fun waitFor(
        matcher: SemanticsMatcher,
        timeoutMs: Long = DEFAULT_TIMEOUT_MS,
        atLeast: Int = 1,
        unmerged: Boolean = false,
    ) {
        compose.waitUntil("expected >=$atLeast of ${matcher.description}", timeoutMs) {
            nodeCount(matcher, unmerged) >= atLeast
        }
    }

    fun waitForGone(
        matcher: SemanticsMatcher,
        timeoutMs: Long = DEFAULT_TIMEOUT_MS,
    ) {
        compose.waitUntil("expected none of ${matcher.description}", timeoutMs) { nodeCount(matcher) == 0 }
    }

    fun waitForText(
        text: String,
        substring: Boolean = false,
        timeoutMs: Long = DEFAULT_TIMEOUT_MS,
    ) = waitFor(hasText(text, substring = substring), timeoutMs)

    /** Moves real focus to the node showing [text], then presses OK. */
    fun select(text: String) {
        focus(text)
        pressKey(KeyEvent.KEYCODE_DPAD_CENTER)
    }

    fun focus(text: String) {
        val focusable = hasText(text) and SemanticsMatcher.keyIsDefined(SemanticsActions.RequestFocus)
        waitFor(focusable)
        focusedNodeOrNull(focusable)?.let { return }
        compose.onAllNodes(focusable).onFirst().performSemanticsAction(SemanticsActions.RequestFocus)
        waitFor(focusable and isFocused())
    }

    private fun focusedNodeOrNull(matcher: SemanticsMatcher): SemanticsNodeInteraction? =
        (matcher and isFocused()).takeIf { nodeCount(it) > 0 }?.let { compose.onAllNodes(it).onFirst() }

    /** Rewrites dev-server fixture URLs/hosts to the embedded server's. */
    fun mapFixtureText(text: String): String = FixtureServer.mapHost(text)

    companion object {
        const val DEFAULT_TIMEOUT_MS = 30_000L
        private const val LONG_PRESS_REPEATS = 6
        private const val LONG_PRESS_STEP_MS = 150L
    }
}
