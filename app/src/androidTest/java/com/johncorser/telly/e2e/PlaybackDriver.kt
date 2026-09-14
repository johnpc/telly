package com.johncorser.telly.e2e

import android.os.SystemClock
import android.view.KeyEvent
import android.widget.EditText
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.pressImeActionButton
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.johncorser.telly.core.ServiceLocator
import com.johncorser.telly.e2e.fixtures.FixtureChannel
import com.johncorser.telly.e2e.fixtures.FixturePlan
import com.johncorser.telly.e2e.fixtures.FixtureServer
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.allOf
import org.junit.Assert.assertTrue
import org.junit.Assert.fail

/**
 * Composite flows shared by several step-definition classes: completing the
 * add-playlist wizard, proving fullscreen playback of a channel, opening the
 * channel panel and clearing overlay chrome. Also tracks which fixture
 * channel the app should currently be tuned to.
 */
class PlaybackDriver(
    private val world: TellyWorld,
) {
    var currentChannel: FixtureChannel = FixturePlan.channels.first()

    fun completeWizard(url: String = "${FixtureServer.baseUrl}/playlist.m3u") {
        world.launchApp()
        world.waitForText(WELCOME_HEADLINE)
        world.select("Add playlist")
        world.select("M3U playlist")
        world.select("Enter URL")
        typeIntoEditor(url)
        world.select("Next")
        world.waitForText("Playlist is processed")
        world.select("Next")
        assertPlaybackOn(1, "News One")
    }

    /** Types into the focused editor (wizard EditText) and commits via IME. */
    fun typeIntoEditor(text: String) {
        val editor = allOf(isAssignableFrom(EditText::class.java), isDisplayed())
        awaitCondition("EditText editor visible") {
            runCatching { onView(editor).check { view, _ -> requireNotNull(view) } }.isSuccess
        }
        onView(editor).perform(replaceText(text))
        awaitCondition("editor shows typed text") {
            runCatching { onView(allOf(editor, withText(text))).check { view, _ -> requireNotNull(view) } }.isSuccess
        }
        onView(editor).perform(pressImeActionButton())
        // The IME NEXT action commits and closes the inline editor; wait for
        // the EditText to leave the hierarchy AND the keyboard window to be
        // fully gone, so the next D-pad press cannot be swallowed while view
        // focus settles back on the compose tree (the first IME show/hide of
        // a process is slow enough to eat the following key otherwise).
        awaitCondition("inline editor closed") {
            runCatching { onView(editor).check(doesNotExist()) }.isSuccess
        }
        awaitCondition("IME hidden") { !world.imeVisible() }
        SystemClock.sleep(IME_SETTLE_MS)
        world.compose.waitForIdle()
    }

    /** Proves the app is at fullscreen playback of [number]/[name] via the info overlay. */
    fun assertPlaybackOn(
        number: Int,
        name: String,
    ) {
        world.launchApp()
        awaitCondition("channels imported into Room") {
            runBlocking { ServiceLocator.database(world.targetContext).channelDao().totalCount() } > 0
        }
        awaitCondition("onboarding/wizard dismissed") {
            world.nodeCount(hasText("Next")) == 0 && world.nodeCount(hasText(WELCOME_HEADLINE)) == 0
        }
        currentChannel = FixturePlan.channelNamed(name)
        val label = hasText(channelLabel(number, name), substring = true)
        awaitCondition("info overlay identifies ${channelLabel(number, name)}", timeoutMs = LONG_TIMEOUT_MS) {
            if (world.nodeCount(label) > 0) {
                true
            } else {
                if (chromeMarkers().all { world.nodeCount(it) == 0 }) {
                    world.pressKey(KeyEvent.KEYCODE_DPAD_CENTER)
                }
                false
            }
        }
        dismissChrome()
    }

    fun channelLabel(
        number: Int,
        name: String,
    ): String = "$number  $name"

    /** BACKs out of any overlay until the bare video is chrome-free. */
    fun dismissChrome() {
        repeat(MAX_BACK_PRESSES) {
            if (noChromeNow()) return
            world.pressKey(KeyEvent.KEYCODE_BACK)
            SystemClock.sleep(SETTLE_MS)
        }
        assertNoChrome()
    }

    fun assertNoChrome() {
        awaitCondition("no chrome over the video") { noChromeNow() }
    }

    fun noChromeNow(): Boolean = chromeMarkers().all { world.nodeCount(it) == 0 }

    private fun chromeMarkers(): List<SemanticsMatcher> =
        listOf(
            hasText("TV guide"),
            hasText("Favorites"),
            hasText("Channels list"),
            hasText("Coming soon to telly"),
            hasText(channelLabel(currentChannel.number, currentChannel.name), substring = true),
        )

    /** The channel panel lives behind the quick-bar's Channels list slot. */
    fun openPanel() {
        dismissChrome()
        world.longPressOk()
        world.select("Channels list")
        world.waitForText("All channels")
        world.waitForText("Favorites")
    }

    fun zapBy(delta: Int) {
        val key = if (delta > 0) KeyEvent.KEYCODE_CHANNEL_UP else KeyEvent.KEYCODE_CHANNEL_DOWN
        repeat(if (delta > 0) delta else -delta) { world.pressKey(key) }
    }

    fun assertZapOverlayShows(
        number: Int,
        name: String,
    ) {
        world.waitForText(channelLabel(number, name), substring = true)
        currentChannel = FixturePlan.channelNamed(name)
    }

    fun rowMatcher(name: String): SemanticsMatcher = hasTestTag("channel-row") and hasText(name)

    fun focusRow(name: String) {
        world.waitFor(rowMatcher(name))
        world.compose.onAllNodes(rowMatcher(name)).onFirst().performSemanticsAction(SemanticsActions.RequestFocus)
        world.waitFor(rowMatcher(name) and isFocused())
    }

    /** Long-OK on a panel row; opens its context menu ("Search" first row). */
    fun longPressRow(name: String) {
        focusRow(name)
        world.longPressOk()
        if (world.nodeCount(hasText("Search")) == 0) {
            // Synthetic key repeats do not always carry the long-press flag
            // tv-material inspects; fall back to the row's long-click action.
            world.compose
                .onAllNodes(rowMatcher(name))
                .onFirst()
                .performSemanticsAction(SemanticsActions.OnLongClick)
        }
        world.waitForText("Search")
    }

    fun awaitCondition(
        description: String,
        timeoutMs: Long = TellyWorld.DEFAULT_TIMEOUT_MS,
        condition: () -> Boolean,
    ) {
        val deadline = SystemClock.uptimeMillis() + timeoutMs
        while (SystemClock.uptimeMillis() < deadline) {
            if (runCatching(condition).getOrDefault(false)) return
            SystemClock.sleep(POLL_MS)
        }
        if (!runCatching(condition).getOrDefault(false)) fail("Timed out waiting for: $description")
    }

    fun assertNow(
        description: String,
        condition: Boolean,
    ) = assertTrue(description, condition)

    companion object {
        const val WELCOME_HEADLINE = "telly doesn't provide any sources of TV channels"
        private const val LONG_TIMEOUT_MS = 60_000L
        private const val MAX_BACK_PRESSES = 6
        private const val SETTLE_MS = 400L
        private const val IME_SETTLE_MS = 500L
        private const val POLL_MS = 250L
    }
}
