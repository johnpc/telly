package com.johncorser.telly.features.recording

import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import okhttp3.Call
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordingCallsTest {
    private val calls = RecordingCalls()
    private val call = mockk<Call>(relaxed = true)

    @Test
    fun `cancel cancels the tracked in-flight call`() {
        calls.track(call)

        calls.cancel()

        verify { call.cancel() }
    }

    @Test
    fun `calls tracked after a cancel are cancelled immediately`() {
        calls.cancel()

        calls.track(call)

        verify { call.cancel() }
    }

    @Test
    fun `tracked calls are left alone until a stop lands`() {
        calls.track(call)

        verify(exactly = 0) { call.cancel() }
    }

    @Test
    fun `cancellingOnStop cancels the in-flight call once stop turns true`() =
        runTest {
            calls.track(call)
            var stopped = false

            val result =
                cancellingOnStop(calls, { stopped }) {
                    delay(1_000)
                    stopped = true
                    // The blocked "read" outlives the stop by more watcher
                    // slices; the cancel lands while it is still parked.
                    delay(1_000)
                    "done"
                }

            assertEquals("done", result)
            verify { call.cancel() }
        }

    @Test
    fun `cancellingOnStop leaves the call alone when the block finishes unstopped`() =
        runTest {
            calls.track(call)

            val result = cancellingOnStop(calls, { false }) { "ok" }

            assertEquals("ok", result)
            verify(exactly = 0) { call.cancel() }
        }

    @Test
    fun `the recording client bounds connect and read waits and follows redirects`() {
        val client = recordingHttpClient()

        assertEquals(RECORDING_CONNECT_TIMEOUT_S * MILLIS_PER_SECOND, client.connectTimeoutMillis.toLong())
        assertEquals(RECORDING_READ_TIMEOUT_S * MILLIS_PER_SECOND, client.readTimeoutMillis.toLong())
        assertTrue(client.followRedirects)
        assertTrue(client.followSslRedirects)
    }

    private companion object {
        const val MILLIS_PER_SECOND = 1_000L
    }
}
