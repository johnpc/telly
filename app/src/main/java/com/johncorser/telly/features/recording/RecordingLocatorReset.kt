package com.johncorser.telly.features.recording

import com.johncorser.telly.core.ServiceLocator
import kotlinx.coroutines.cancel

/**
 * Tears the runtime down (tests that swap the database behind the
 * locator's back): the recovery/scheduler coroutines must not outlive the
 * database they read, or their failure surfaces in an unrelated test.
 */
fun ServiceLocator.resetRecordingRuntime() {
    synchronized(runtimeLock) {
        runtime?.scope?.cancel()
        runtime = null
    }
}
