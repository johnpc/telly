package com.johncorser.telly.features.recording

import android.content.Context
import android.util.Log
import com.johncorser.telly.core.ServiceLocator
import com.johncorser.telly.core.db.TellyDatabase
import com.johncorser.telly.core.streamUserAgentFor
import com.johncorser.telly.features.epg.EpgRepository
import com.johncorser.telly.features.epg.ProgramTitle
import com.johncorser.telly.features.playback.PlaybackTime
import com.johncorser.telly.features.player.Media3PlayerEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

/**
 * The recording slice's composition root, keyed by the app database so a
 * fresh database (the e2e harness resets it per scenario) rebuilds the
 * runtime instead of leaking a closed one. The center is app-scoped: a
 * capture keeps running while the user navigates routes or the activity
 * backgrounds (the foreground service holds the process).
 */
private class RecordingRuntime(
    val db: TellyDatabase,
    val scope: CoroutineScope,
    val center: RecordingCenter,
)

@Volatile
private var runtime: RecordingRuntime? = null
private val runtimeLock = Any()

/** The app-scoped DVR facade, rebuilt when the app database is swapped. */
fun ServiceLocator.recordingCenter(context: Context): RecordingCenter {
    val db = database(context)
    return synchronized(runtimeLock) {
        val existing = runtime?.takeIf { it.db === db }
        existing ?: run {
            runtime?.scope?.cancel()
            buildRecordingRuntime(context.applicationContext, db, epgRepository(context)).also { runtime = it }
        }
    }.center
}

/** The Recordings library screen's dependency bundle. */
fun ServiceLocator.recordingDeps(context: Context): RecordingDeps =
    RecordingDeps(
        center = recordingCenter(context),
        engineFactory = {
            Media3PlayerEngine.create(context.applicationContext, userAgentFor = streamUserAgentFor(context))
        },
        clock = ServiceLocator.clock,
    )

private fun buildRecordingRuntime(
    appContext: Context,
    db: TellyDatabase,
    epg: EpgRepository,
): RecordingRuntime {
    val clock = ServiceLocator.clock
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val store = RecordingStore(db.recordingDao, clock)
    val hlsClient = HlsClient()
    val files =
        RecordingFiles(
            directory = { appContext.getExternalFilesDir(RECORDINGS_DIR) ?: File(appContext.filesDir, RECORDINGS_DIR) },
            container = HlsContainerProbe(hlsClient),
        )
    val engine =
        RecordingEngine(
            store = store,
            recorder =
                RoutingStreamRecorder(
                    hls = HlsStreamRecorder(hlsClient, log = { Log.w(LOG_TAG, it) }),
                    progressive = OkHttpStreamRecorder(),
                ),
            files = files,
            scope = scope,
            clock = clock,
            service = ContextRecordingServiceControl(appContext),
        )
    val scheduler = RecordingScheduler(store, engine, clock, scope, PlaybackTime.minuteBoundaryTicks(clock))
    val center =
        RecordingCenter(
            store = store,
            engine = engine,
            scheduler = scheduler,
            files = files,
            programmes = airingLookup(epg),
            clock = clock,
        )
    scope.launch { center.start() }
    return RecordingRuntime(db, scope, center)
}

/** EPG seam: the airing programme's display title + end for instant record. */
private fun airingLookup(epg: EpgRepository): RecordingProgrammes =
    RecordingProgrammes { tvgId, atMs ->
        epg
            .nowNext(listOf(tvgId), atMs)
            .first()[tvgId]
            ?.now
            ?.let { RecordingProgramme(title = ProgramTitle.of(it.details), endMs = it.endMs) }
    }

private const val RECORDINGS_DIR = "recordings"
private const val LOG_TAG = "HlsStreamRecorder"
