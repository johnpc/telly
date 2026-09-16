package com.johncorser.telly.features.settings

import com.johncorser.telly.testutil.FakeKeyValueStore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfigAutoBackupTest {
    private val kv = FakeKeyValueStore()
    private val state = AutoBackupState(kv)
    private val written = mutableListOf<String>()
    private val warnings = mutableListOf<String>()
    private var json: String? = "payload-1"
    private var enabled = true
    private var failWrites = false

    private fun engine(nowMs: Long = 42L) =
        ConfigAutoBackup(
            enabled = { enabled },
            buildJson = { json },
            write = {
                check(!failWrites) { "disk full" }
                written += it
            },
            state = state,
            clock = { nowMs },
            warn = { message, _ -> warnings += message },
        )

    @Test
    fun `a trigger burst debounces into a single export`() =
        runTest {
            val triggers = MutableSharedFlow<Unit>()
            val job = engine().start(this, triggers, debounceMs = 5_000)
            runCurrent() // let the collector subscribe before triggering
            triggers.emit(Unit)
            advanceTimeBy(1_000)
            triggers.emit(Unit)
            advanceTimeBy(4_999)
            assertEquals(0, written.size)
            advanceTimeBy(2)
            assertEquals(listOf("payload-1"), written)
            job.cancel()
        }

    @Test
    fun `an unchanged payload is not re-written, a changed one is`() =
        runTest {
            val triggers = MutableSharedFlow<Unit>()
            val job = engine().start(this, triggers, debounceMs = 5_000)
            runCurrent() // let the collector subscribe before triggering
            triggers.emit(Unit)
            advanceTimeBy(5_001)
            triggers.emit(Unit)
            advanceTimeBy(5_001)
            assertEquals(listOf("payload-1"), written)
            json = "payload-2"
            triggers.emit(Unit)
            advanceTimeBy(5_001)
            assertEquals(listOf("payload-1", "payload-2"), written)
            job.cancel()
        }

    @Test
    fun `an export records the time on the live status`() =
        runTest {
            engine(nowMs = 1_234L).exportNow()
            assertEquals(1_234L, state.status.value.lastExportMs)
            assertEquals(1_234L, kv.values[AutoBackupState.KEY_LAST_MS])
        }

    @Test
    fun `disabled or empty states export nothing`() =
        runTest {
            enabled = false
            engine().exportNow()
            enabled = true
            json = null
            engine().exportNow()
            assertTrue(written.isEmpty())
            assertTrue(warnings.isEmpty())
        }

    @Test
    fun `a failed write warns, keeps the engine alive and retries later`() =
        runTest {
            val triggers = MutableSharedFlow<Unit>()
            val job = engine().start(this, triggers, debounceMs = 5_000)
            runCurrent() // let the collector subscribe before triggering
            failWrites = true
            triggers.emit(Unit)
            advanceTimeBy(5_001)
            assertEquals(listOf("config auto-backup export failed"), warnings)
            assertEquals(null, state.status.value.lastExportMs)
            failWrites = false
            triggers.emit(Unit)
            advanceTimeBy(5_001)
            assertEquals(listOf("payload-1"), written)
            job.cancel()
        }

    @Test
    fun `a dying trigger flow is only a warning`() =
        runTest {
            val job = engine().start(this, flow<Unit> { error("db closed") }, debounceMs = 5_000)
            advanceTimeBy(1)
            assertEquals(listOf("config auto-backup triggers died"), warnings)
            job.cancel()
        }
}
