package com.johncorser.telly.features.recording

import com.johncorser.telly.features.recording.db.RecordingEntity

/** Lifecycle of a DVR entry; persisted by name in the recordings table. */
enum class RecordingStatus {
    /** Waiting for its start time (custom/programme recordings). */
    SCHEDULED,

    /** The stream is being copied to disk right now. */
    RECORDING,

    /** Finished (planned end reached or stopped by the user); playable. */
    DONE,

    /** The copy gave up (stream unreachable) or the start was missed. */
    FAILED,

    ;

    companion object {
        /** Unknown raw values read back as FAILED, never crash the library. */
        fun of(raw: String): RecordingStatus = entries.firstOrNull { it.name == raw } ?: FAILED
    }
}

/** The entry's parsed [RecordingStatus]. */
val RecordingEntity.recordingStatus: RecordingStatus get() = RecordingStatus.of(status)
