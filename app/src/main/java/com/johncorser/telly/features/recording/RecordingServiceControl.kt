package com.johncorser.telly.features.recording

/** What the foreground-service notification shows for one active capture. */
data class RecordingSession(
    val channelName: String,
    val startedAtMs: Long,
)

/**
 * Seam between the record engine and the Android foreground service: the
 * engine calls [sync] on every active-set change; the production impl
 * starts the service while sessions exist and stops it when none do.
 * JVM tests inject a recording fake.
 */
fun interface RecordingServiceControl {
    fun sync(sessions: List<RecordingSession>)
}
