package com.johncorser.telly.features.recording

import java.io.File

/**
 * One HTTP copy attempt: streams [url] into [sink] until the server closes
 * the connection or [shouldStop] turns true; returns the bytes written by
 * THIS attempt. [RecordingEngine] loops attempts (live TS sources drop and
 * resume), so implementations never retry themselves.
 */
fun interface StreamRecorder {
    suspend fun copy(
        url: String,
        sink: File,
        shouldStop: () -> Boolean,
    ): Long
}
