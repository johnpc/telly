package com.johncorser.telly.features.playback

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
import org.junit.Test

/** The UDP-proxy resolution seam: every engine load goes through it. */
@OptIn(ExperimentalCoroutinesApi::class)
class TuneControllerTest {
    private val engine = FakePlayerEngine()
    private val store = FakeKeyValueStore()
    private val history = WatchHistory(FakeWatchHistoryDao()) { 1L }
    private val multicast =
        testChannel(1, 1, "Multicast One").let {
            it.copy(source = it.source.copy(streamUrl = "udp://@239.1.2.3:1234"))
        }

    private fun TestScope.controller() =
        TuneController(
            engine = engine,
            store = store,
            scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler)),
            channelDao = FakeChannelDao(listOf(multicast, testChannel(2, 2, "News One"))),
            history = history,
            policies = TunePolicies(resolveUrl = { UdpProxy.resolve("192.168.1.10:4022", it) }),
        )

    @Test
    fun `tune loads the proxy-rewritten multicast url`() =
        runTest {
            val tuner = controller()

            tuner.tune(multicast)

            assertEquals(listOf("http://192.168.1.10:4022/udp/239.1.2.3:1234"), engine.loaded)
        }

    @Test
    fun `http streams tune untouched and retune resolves again`() =
        runTest {
            val tuner = controller()

            tuner.tune(multicast)
            tuner.zap(+1)
            tuner.retune()

            assertEquals(
                listOf(
                    "http://192.168.1.10:4022/udp/239.1.2.3:1234",
                    "http://s/2.ts",
                    "http://s/2.ts",
                ),
                engine.loaded,
            )
        }
}
