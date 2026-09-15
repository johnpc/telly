package com.johncorser.telly.features.recording

import com.johncorser.telly.features.recording.db.RecordingEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/**
 * The in-app scheduler (GuideNow ticker pattern): re-sweeps the SCHEDULED
 * rows on every tick of the injected flow AND on every list change, starting
 * captures whose time has come and failing entries whose whole window was
 * missed. telly has no alarm-manager wakeups — scheduled recordings only
 * start while the app is running, which the settings pane says out loud.
 */
class RecordingScheduler(
    private val store: RecordingStore,
    private val engine: RecordingEngine,
    private val clock: () -> Long,
    private val scope: CoroutineScope,
    private val ticks: Flow<Unit>,
) {
    fun start() {
        scope.launch {
            combine(store.scheduled, ticks.onStart { emit(Unit) }) { list, _ -> list }
                .collect(::sweep)
        }
    }

    private suspend fun sweep(due: List<RecordingEntity>) {
        val now = clock()
        due.forEach { entry ->
            when {
                entry.plannedEndMs <= now -> store.fail(entry.id)
                entry.startMs <= now && !engine.isActive(entry.id) -> engine.start(entry)
            }
        }
    }
}
