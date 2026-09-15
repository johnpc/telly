package com.johncorser.telly.features.recording

import com.johncorser.telly.features.playlist.db.ChannelEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * State of the "Custom recording" form: the channel comes prefilled from
 * the invoking context, the start defaults to the next five-minute
 * boundary (always in the future, never a start-immediately race) and the
 * duration to an hour. LEFT/RIGHT on the form rows step through both.
 */
class CustomRecordingForm(
    val channel: ChannelEntity,
    nowMs: Long,
) {
    private val floorMs = (nowMs / START_STEP_MS + 1) * START_STEP_MS

    private val mutableStart = MutableStateFlow(floorMs)
    private val mutableDuration = MutableStateFlow(DEFAULT_DURATION_MINUTES)

    val startMs: StateFlow<Long> = mutableStart.asStateFlow()
    val durationMinutes: StateFlow<Int> = mutableDuration.asStateFlow()

    /** Planned end derived from the two fields. */
    val endMs: Long get() = mutableStart.value + mutableDuration.value * MINUTE_MS

    /** ±5 min per step, never before the form's opening boundary. */
    fun adjustStart(steps: Int) {
        mutableStart.update { (it + steps * START_STEP_MS).coerceAtLeast(floorMs) }
    }

    /** ±15 min per step within 15 min .. 6 h. */
    fun adjustDuration(steps: Int) {
        mutableDuration.update {
            (it + steps * DURATION_STEP_MINUTES).coerceIn(MIN_DURATION_MINUTES, MAX_DURATION_MINUTES)
        }
    }

    companion object {
        private const val MINUTE_MS = 60_000L
        const val START_STEP_MINUTES = 5
        private const val START_STEP_MS = START_STEP_MINUTES * MINUTE_MS
        const val DEFAULT_DURATION_MINUTES = 60
        const val DURATION_STEP_MINUTES = 15
        const val MIN_DURATION_MINUTES = 15
        const val MAX_DURATION_MINUTES = 360
    }
}
