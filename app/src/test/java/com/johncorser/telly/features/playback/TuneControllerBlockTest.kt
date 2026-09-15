package com.johncorser.telly.features.playback

import com.johncorser.telly.core.settings.InMemoryKeyValueStore
import com.johncorser.telly.core.settings.ParentalControls
import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.features.history.WatchHistory
import com.johncorser.telly.testutil.FakeChannelDao
import com.johncorser.telly.testutil.FakeKeyValueStore
import com.johncorser.telly.testutil.FakePlayerEngine
import com.johncorser.telly.testutil.FakeWatchHistoryDao
import com.johncorser.telly.testutil.testChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Every tune path funnels through [TuneController.tune], so the block gate
 * covers guide OK, panel OK, zap, recents and the search/restore paths.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TuneControllerBlockTest {
    private val engine = FakePlayerEngine()
    private val store = FakeKeyValueStore()
    private val parental = ParentalControls(SettingsRepository(InMemoryKeyValueStore()))
    private val blocked = testChannel(2, 2, "Sports Arena").let { it.copy(flags = it.flags.copy(blocked = true)) }
    private val dao = FakeChannelDao(listOf(testChannel(1, 1, "News One"), blocked))

    private fun TestScope.tuner(): TuneController {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        return TuneController(
            engine = engine,
            store = store,
            scope = scope,
            channelDao = dao,
            history = WatchHistory(FakeWatchHistoryDao()) { 0L },
            gate = BlockGate(parental),
        )
    }

    @Test
    fun `tuning a blocked channel prompts instead of loading the stream`() {
        runTest {
            parental.setPin("2468")
            val tuner = tuner()

            tuner.tune(blocked)

            assertEquals(blocked, tuner.gate.pinPrompt.value)
            assertNull(tuner.current.value)
            assertTrue(engine.loaded.isEmpty())
        }
    }

    @Test
    fun `a wrong pin stays put and a right one tunes`() {
        runTest {
            parental.setPin("2468")
            val tuner = tuner()
            tuner.tune(blocked)

            TuneBlockPrompt(tuner).submit("1111")
            assertNull(tuner.current.value)
            assertEquals(blocked, tuner.gate.pinPrompt.value)

            TuneBlockPrompt(tuner).submit("2468")
            assertEquals(blocked, tuner.current.value)
            assertEquals(listOf(blocked.source.streamUrl), engine.loaded)
            assertEquals(blocked.id, store.values[TuneController.LAST_CHANNEL_KEY])
        }
    }

    @Test
    fun `cancelling the prompt never tunes`() {
        runTest {
            parental.setPin("2468")
            val tuner = tuner()
            tuner.tune(blocked)

            TuneBlockPrompt(tuner).dismiss()

            assertNull(tuner.gate.pinPrompt.value)
            assertNull(tuner.current.value)
            assertTrue(engine.loaded.isEmpty())
        }
    }

    @Test
    fun `zapping onto a blocked channel is gated too`() {
        runTest {
            parental.setPin("2468")
            val tuner = tuner()
            tuner.tune(dao.channels.value.first())
            engine.loaded.clear()

            // Gated: the neighbour exists but did not tune, so zap reports false
            // (no zap overlay) and the prompt owns the next step.
            assertFalse(tuner.zap(+1))

            assertEquals(blocked, tuner.gate.pinPrompt.value)
            assertEquals("News One", tuner.current.value?.source?.name)
            assertTrue(engine.loaded.isEmpty())
        }
    }

    @Test
    fun `restoring a stored blocked channel prompts on start`() {
        runTest {
            parental.setPin("2468")
            store.values[TuneController.LAST_CHANNEL_KEY] = blocked.id
            val tuner = tuner()

            tuner.start()

            assertEquals(blocked, tuner.gate.pinPrompt.value)
            assertNull(tuner.current.value)
        }
    }
}
