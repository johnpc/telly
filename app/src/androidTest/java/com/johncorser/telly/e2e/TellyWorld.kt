package com.johncorser.telly.e2e

import android.content.Context
import android.os.SystemClock
import android.view.KeyEvent
import android.view.WindowInsets
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.lifecycle.Lifecycle
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
        listOf("telly-settings", "telly", "telly-search").forEach { name ->
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
        if (nodeCount(matcher, unmerged) < atLeast) scrollTowards(matcher)
        compose.waitUntil("expected >=$atLeast of ${matcher.description}", timeoutMs) {
            nodeCount(matcher, unmerged) >= atLeast
        }
    }

    /** Lazy lists on a 540 dp TV only compose visible rows: scroll to reveal. */
    private fun scrollTowards(matcher: SemanticsMatcher) {
        val scrollable = SemanticsMatcher.keyIsDefined(SemanticsActions.ScrollToIndex)
        repeat(nodeCount(scrollable)) { index ->
            val found =
                runCatching {
                    compose.onAllNodes(scrollable)[index].performScrollToNode(matcher)
                }.isSuccess
            if (found) return
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

    /** [select], scoped under the node tagged [tag] (overlaid sheets reuse row texts). */
    fun selectWithin(
        tag: String,
        text: String,
    ) {
        focusMatching(hasText(text) and hasAnyAncestor(hasTestTag(tag)))
        pressKey(KeyEvent.KEYCODE_DPAD_CENTER)
    }

    fun focus(text: String) = focusMatching(hasText(text).or(hasContentDescription(text)))

    private fun focusMatching(labelled: SemanticsMatcher) {
        val focusable = labelled and SemanticsMatcher.keyIsDefined(SemanticsActions.RequestFocus)
        waitFor(focusable)
        requestFocusOn(focusable)
        // Screen transitions can bounce focus right after it lands (e.g. the
        // wizard's editor detach); let that settle and re-take focus once.
        SystemClock.sleep(FOCUS_SETTLE_MS)
        if (nodeCount(focusable and isFocused()) == 0) {
            requestFocusOn(focusable)
            SystemClock.sleep(FOCUS_SETTLE_MS)
        }
        waitFor(focusable and isFocused())
    }

    private fun requestFocusOn(matcher: SemanticsMatcher) {
        compose.onAllNodes(matcher).onFirst().performSemanticsAction(SemanticsActions.RequestFocus)
        waitFor(matcher and isFocused())
    }

    /** True when two texts render vertically aligned (same visual row). */
    fun rowAligned(
        textA: String,
        textB: String,
    ): Boolean = rowAlignedMatching(hasText(textA), hasText(textB))

    fun rowAlignedMatching(
        matcherA: SemanticsMatcher,
        matcherB: SemanticsMatcher,
    ): Boolean {
        val aBounds = boundsOf(matcherA)
        val bBounds = boundsOf(matcherB)
        return aBounds.any { a -> bBounds.any { b -> a.top < b.bottom && b.top < a.bottom } }
    }

    fun boundsOf(matcher: SemanticsMatcher): List<Rect> =
        compose.onAllNodes(matcher).fetchSemanticsNodes(false).map { it.boundsInRoot }

    /** Matches nodes whose (any) text matches [regex] exactly. */
    fun hasTextMatching(regex: Regex): SemanticsMatcher =
        SemanticsMatcher("text matches $regex") { node ->
            node.config.getOrNull(SemanticsProperties.Text)?.any { regex.matches(it.text) } == true
        }

    /** True once the activity finished (BACK at the guide root exits). */
    fun appDestroyed(): Boolean = scenario?.state == Lifecycle.State.DESTROYED

    /** True while the soft keyboard covers the activity (first IME show lags). */
    fun imeVisible(): Boolean {
        var visible = false
        scenario?.onActivity { activity ->
            visible = activity.window.decorView.rootWindowInsets?.isVisible(WindowInsets.Type.ime()) == true
        }
        return visible
    }

    /**
     * Hides the soft keyboard in-process. Never dispatch BACK for this: the
     * visibility check races the IME's own hide animation, and a BACK that
     * lands after the keyboard is gone reaches the app instead — popping the
     * settings pane whose content the next step asserts on (the epg-data
     * add-source scenario failed exactly this way on tv34).
     */
    fun hideIme() {
        scenario?.onActivity { activity ->
            activity.window.decorView.windowInsetsController?.hide(WindowInsets.Type.ime())
        }
    }

    /** Rewrites dev-server fixture URLs/hosts to the embedded server's. */
    fun mapFixtureText(text: String): String = FixtureServer.mapHost(text)

    companion object {
        const val DEFAULT_TIMEOUT_MS = 30_000L
        private const val LONG_PRESS_REPEATS = 6
        private const val LONG_PRESS_STEP_MS = 150L
        private const val FOCUS_SETTLE_MS = 350L
    }
}
