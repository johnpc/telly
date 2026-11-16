package com.johncorser.telly.features.recording

// The recording status machine (docs/state-machines.md): SCHEDULED ->
// RECORDING -> DONE | FAILED, plus SCHEDULED -> FAILED for a missed window.
// Every edge is guarded by the row's CURRENT status in SQL and reports
// whether it happened, so a cancel racing a start, a double finish, or a
// finish on a deleted row can never resurrect or rewrite a row.

/** SCHEDULED -> RECORDING; the start stamp becomes the actual start. */
suspend fun RecordingStore.markStarted(id: Long): Boolean =
    dao.markStarted(id, RecordingStatus.RECORDING.name, clock()) > 0

/** RECORDING -> DONE with the final size; also used by the user's Stop. */
suspend fun RecordingStore.complete(
    id: Long,
    sizeBytes: Long,
): Boolean = finish(RecordingStatus.RECORDING, RecordingStatus.DONE, id, sizeBytes)

/** RECORDING -> FAILED (the copy gave up before writing anything). */
suspend fun RecordingStore.fail(
    id: Long,
    sizeBytes: Long = 0,
): Boolean = finish(RecordingStatus.RECORDING, RecordingStatus.FAILED, id, sizeBytes)

/** SCHEDULED -> FAILED (the start window passed while telly was not running). */
suspend fun RecordingStore.failMissed(id: Long): Boolean =
    finish(RecordingStatus.SCHEDULED, RecordingStatus.FAILED, id, 0)

private suspend fun RecordingStore.finish(
    from: RecordingStatus,
    to: RecordingStatus,
    id: Long,
    sizeBytes: Long,
): Boolean = dao.finish(id, from.name, to.name, clock(), sizeBytes) > 0
