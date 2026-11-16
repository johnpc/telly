package com.johncorser.telly.features.recording

import com.johncorser.telly.core.db.TellyDatabase
import kotlinx.coroutines.CoroutineScope

/**
 * The recording slice's composition root, keyed by the app database so a
 * fresh database (the e2e harness resets it per scenario) rebuilds the
 * runtime instead of leaking a closed one. The center is app-scoped: a
 * capture keeps running while the user navigates routes or the activity
 * backgrounds (the foreground service holds the process).
 */
internal class RecordingRuntime(
    val db: TellyDatabase,
    val scope: CoroutineScope,
    val center: RecordingCenter,
)
