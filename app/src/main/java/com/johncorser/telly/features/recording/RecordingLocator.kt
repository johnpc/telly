package com.johncorser.telly.features.recording

import android.content.Context
import android.util.Log
import com.johncorser.telly.core.ServiceLocator
import com.johncorser.telly.core.db.TellyDatabase
import com.johncorser.telly.core.streamUserAgentFor
import com.johncorser.telly.core.tunedEngine
import com.johncorser.telly.features.epg.EpgRepository
import com.johncorser.telly.features.epg.ProgramTitle
import com.johncorser.telly.features.playback.PlaybackTime
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

@Volatile
internal var runtime: RecordingRuntime? = null
internal val runtimeLock = Any()

/** The app-scoped DVR facade, rebuilt when the app database is swapped. */
fun ServiceLocator.recordingCenter(context: Context): RecordingCenter {
    val db = database(context)
    return synchronized(runtimeLock) {
        val existing = runtime?.takeIf { it.db === db }
        existing ?: run {
            runtime?.scope?.cancel()
            buildRecordingRuntime(
                appContext = context.applicationContext,
                db = db,
                epg = epgRepository(context),
                userAgentFor = streamUserAgentFor(context),
            ).also { runtime = it }
        }
    }.center
}

/** The Recordings library screen's dependency bundle. */
fun ServiceLocator.recordingDeps(context: Context): RecordingDeps =
    RecordingDeps(
        center = recordingCenter(context),
        engineFactory = { tunedEngine(context) },
        clock = ServiceLocator.clock,
    )

private fun buildRecordingRuntime(
    appContext: Context,
    db: TellyDatabase,
    epg: EpgRepository,
    userAgentFor: (streamUrl: String) -> String,
): RecordingRuntime {
    val clock = ServiceLocator.clock
    // A failed recovery sweep or scheduler tick is logged, never a crash.
    val failures = CoroutineExceptionHandler { _, e -> Log.w(LOG_TAG, "recording runtime failed", e) }
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO + failures)
    val store = RecordingStore(db.recordingDao, clock)
    val hlsClient = HlsClient(userAgentFor = userAgentFor)
    val files =
        RecordingFiles(
            directory = { appContext.getExternalFilesDir(RECORDINGS_DIR) ?: File(appContext.filesDir, RECORDINGS_DIR) },
            container = HlsContainerProbe(hlsClient),
        )
    val engine =
        RecordingEngine(
            store = store,
            // Captures send the same per-playlist > global > default stream
            // UA the player resolves for the identical URLs — both the HLS
            // recorder (playlist + segment fetches via hlsClient) and the
            // byte-copying recorder resolve it per URL.
            recorder =
                RoutingStreamRecorder(
                    hls = HlsStreamRecorder(hlsClient, log = { Log.w(LOG_TAG, it) }),
                    progressive = OkHttpStreamRecorder(userAgentFor = userAgentFor),
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
