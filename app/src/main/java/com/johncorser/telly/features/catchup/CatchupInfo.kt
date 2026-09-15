package com.johncorser.telly.features.catchup

import com.johncorser.telly.features.playback.PlaybackInfoData
import com.johncorser.telly.features.playback.ProgramTimes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.TimeZone

/**
 * Minimal honest adaptation of the info overlay during catch-up: the
 * archived programme's title and air times with a position readout instead
 * of the live now/next progress. Channel identity, logo and badges stay.
 */
object CatchupInfo {
    fun merged(
        live: Flow<PlaybackInfoData?>,
        state: Flow<CatchupState?>,
        position: Flow<Long>,
        zone: TimeZone,
        scope: CoroutineScope,
    ): StateFlow<PlaybackInfoData?> =
        combine(live, state, position) { data, catchup, positionMs ->
            if (catchup == null) data else data?.let { adapt(it, catchup.request, positionMs, zone) }
        }.stateIn(scope, SharingStarted.Eagerly, null)

    private fun adapt(
        data: PlaybackInfoData,
        request: CatchupRequest,
        positionMs: Long,
        zone: TimeZone,
    ): PlaybackInfoData =
        data.copy(
            title = request.title ?: data.title,
            timeRange = ProgramTimes.range(request.startMs, request.endMs, zone),
            remaining = null,
            progressPermille = ProgramTimes.progressPermille(0L, request.durationMs, positionMs),
            description = null,
            nextLine = null,
            elapsed = ProgramTimes.span(positionMs),
            duration = ProgramTimes.span(request.durationMs),
        )
}
