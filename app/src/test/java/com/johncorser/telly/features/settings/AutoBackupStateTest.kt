package com.johncorser.telly.features.settings

import com.johncorser.telly.testutil.FakeKeyValueStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoBackupStateTest {
    @Test
    fun `seeds the status from the persisted export time`() {
        val kv = FakeKeyValueStore().apply { values[AutoBackupState.KEY_LAST_MS] = 7L }
        assertEquals(7L, AutoBackupState(kv).status.value.lastExportMs)
    }

    @Test
    fun `recordExport persists and publishes time and hash`() {
        val kv = FakeKeyValueStore()
        val state = AutoBackupState(kv)
        assertEquals(null, state.lastHash)
        state.recordExport(atMs = 99L, hash = 123L)
        assertEquals(99L, state.status.value.lastExportMs)
        assertEquals(123L, state.lastHash)
        assertEquals(99L, kv.values[AutoBackupState.KEY_LAST_MS])
    }

    @Test
    fun `the summary names the location before and after the first export`() {
        assertEquals(
            "Backs up to Documents/telly on every change",
            AutoBackupStatus().summary(),
        )
        val exported = AutoBackupStatus(lastExportMs = 1_760_000_000_000L).summary()
        assertTrue(exported, exported.startsWith("Last backup "))
        assertTrue(exported, exported.endsWith("· Documents/telly"))
    }
}
