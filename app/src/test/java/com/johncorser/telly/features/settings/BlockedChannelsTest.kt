package com.johncorser.telly.features.settings

import com.johncorser.telly.testutil.FakeChannelDao
import com.johncorser.telly.testutil.testChannel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The parental "Blocked channels" pane's data seam. */
class BlockedChannelsTest {
    private val blockedChannel =
        testChannel(2, 2, "Sports Arena").let { it.copy(flags = it.flags.copy(blocked = true)) }
    private val dao = FakeChannelDao(listOf(testChannel(1, 1, "News One"), blockedChannel))
    private val store = BlockedChannels(dao)

    @Test
    fun `lists only blocked channels`() =
        runTest {
            assertEquals(listOf("Sports Arena"), store.channels.first().map { it.source.name })
        }

    @Test
    fun `unblock clears the flag and empties the list`() =
        runTest {
            store.unblock(blockedChannel)

            assertTrue(store.channels.first().isEmpty())
            assertTrue(dao.channels.value.none { it.flags.blocked })
        }
}
