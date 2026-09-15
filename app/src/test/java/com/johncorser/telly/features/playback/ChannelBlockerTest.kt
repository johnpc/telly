package com.johncorser.telly.features.playback

import com.johncorser.telly.core.settings.InMemoryKeyValueStore
import com.johncorser.telly.core.settings.ParentalControls
import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.testutil.FakeChannelDao
import com.johncorser.telly.testutil.testChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Block/Unblock is PIN-gated in BOTH directions (set one when missing). */
@OptIn(ExperimentalCoroutinesApi::class)
class ChannelBlockerTest {
    private val dao = FakeChannelDao(listOf(testChannel(1, 1, "News One")))
    private val parental = ParentalControls(SettingsRepository(InMemoryKeyValueStore()))

    private fun TestScope.blocker(withParental: Boolean = true): ChannelBlocker =
        ChannelBlocker(
            actions = ChannelActions(dao, CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))),
            parental = if (withParental) parental else null,
        )

    @Test
    fun `mode is setup until a pin exists then confirm`() {
        runTest {
            val blocker = blocker()
            assertEquals(BlockPinMode.SETUP, blocker.mode())
            parental.setPin("2468")
            assertEquals(BlockPinMode.CONFIRM, blocker.mode())
        }
    }

    @Test
    fun `setup stores the fresh pin and blocks the channel`() {
        runTest {
            val blocker = blocker()

            assertTrue(blocker.submit("2468", BlockPinMode.SETUP, dao.channels.value.single()))

            assertTrue(parental.verifyPin("2468"))
            assertTrue(dao.channels.value.single().flags.blocked)
        }
    }

    @Test
    fun `confirm verifies before flipping and rejects a wrong pin`() {
        runTest {
            parental.setPin("2468")
            val blocker = blocker()
            val channel = dao.channels.value.single()

            assertFalse(blocker.submit("1111", BlockPinMode.CONFIRM, channel))
            assertFalse(dao.channels.value.single().flags.blocked)

            assertTrue(blocker.submit("2468", BlockPinMode.CONFIRM, channel))
            assertTrue(dao.channels.value.single().flags.blocked)
        }
    }

    @Test
    fun `submitting on a blocked channel unblocks it`() {
        runTest {
            parental.setPin("2468")
            val blocker = blocker()
            val channel = dao.channels.value.single().let { it.copy(flags = it.flags.copy(blocked = true)) }
            dao.channels.value = listOf(channel)

            assertTrue(blocker.submit("2468", BlockPinMode.CONFIRM, channel))

            assertFalse(dao.channels.value.single().flags.blocked)
        }
    }

    @Test
    fun `without parental machinery nothing is accepted`() {
        runTest {
            val blocker = blocker(withParental = false)
            assertFalse(blocker.submit("2468", BlockPinMode.SETUP, dao.channels.value.single()))
            assertFalse(blocker.submit("2468", BlockPinMode.CONFIRM, dao.channels.value.single()))
            assertFalse(dao.channels.value.single().flags.blocked)
        }
    }
}
