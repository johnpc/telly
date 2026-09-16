package com.johncorser.telly.features.recording

import com.johncorser.telly.features.history.WatchHistory
import com.johncorser.telly.features.playlist.db.ChannelEntity
import com.johncorser.telly.features.playlist.db.displayName
import com.johncorser.telly.features.recording.db.RecordingEntity
import kotlinx.coroutines.flow.Flow

/** The airing programme a Record action derives its title/end from. */
data class RecordingProgramme(
    val title: String,
    val endMs: Long,
)

/** EPG lookup seam: the programme airing on [tvgId] at [atMs], if known. */
fun interface RecordingProgrammes {
    suspend fun airingOn(
        tvgId: String,
        atMs: Long,
    ): RecordingProgramme?
}

/**
 * The DVR facade every surface talks to: instant record (until the EPG end
 * or the 3 h fallback), scheduled programme/custom recordings, stop /
 * delete, and the storage numbers for the settings pane. A plain class —
 * clock, EPG lookup, files and engine all injected.
 */
class RecordingCenter(
    private val store: RecordingStore,
    private val engine: RecordingEngine,
    private val scheduler: RecordingScheduler,
    private val files: RecordingFiles,
    private val programmes: RecordingProgrammes,
    private val clock: () -> Long,
) {
    /** The library list, newest start first. */
    val recordings: Flow<List<RecordingEntity>> = store.recordings

    /** App start: finalize stale rows, then watch the schedule. */
    suspend fun start() {
        engine.recoverStale()
        scheduler.start()
    }

    /** Record on a channel: start now, or offer Stop while it records. */
    suspend fun toggleInstant(channel: ChannelEntity): RecordingPrompt {
        val active = store.activeFor(keyOf(channel))
        return when {
            !RecordingSupport.isRecordable(channel.source.streamUrl) -> RecordingPrompt.Unsupported(channel.displayName)
            active != null -> RecordingPrompt.StopConfirm(active.id, channel.displayName)
            else -> startInstant(channel)
        }
    }

    private suspend fun startInstant(channel: ChannelEntity): RecordingPrompt {
        val now = clock()
        val airing = channel.epgId?.let { programmes.airingOn(it, now) }
        val endMs = airing?.endMs ?: (now + RecordingSupport.FALLBACK_DURATION_MS)
        store.schedule(newEntry(channel, airing?.title, now, endMs))?.let(engine::start)
        return RecordingPrompt.Done
    }

    /** Guide-cell Record on a future programme: one scheduled entry per slot. */
    suspend fun scheduleProgramme(
        channel: ChannelEntity,
        title: String?,
        startMs: Long,
        endMs: Long,
    ): RecordingPrompt {
        if (!RecordingSupport.isRecordable(channel.source.streamUrl)) {
            return RecordingPrompt.Unsupported(channel.displayName)
        }
        store.schedule(newEntry(channel, title, startMs, endMs))
        return RecordingPrompt.Done
    }

    /** Stops an in-progress capture; the entry stays as DONE. */
    suspend fun stop(recordingId: Long) = engine.stop(recordingId)

    /** Removes an entry: stops it first if live, then drops row + file. */
    suspend fun delete(recordingId: Long) {
        val entry = store.byId(recordingId) ?: return
        engine.stop(recordingId)
        store.delete(recordingId)
        files.delete(entry.filePath)
    }

    /** Settings pane: drops every row and every capture file. */
    suspend fun deleteAll() {
        store.allRecording().forEach { engine.stop(it.id) }
        store.deleteAll()
        files.deleteAllFiles()
    }

    fun storage(): RecordingStorageInfo = files.storage()

    private fun newEntry(
        channel: ChannelEntity,
        title: String?,
        startMs: Long,
        endMs: Long,
    ): RecordingEntity =
        RecordingEntity(
            channelKey = keyOf(channel),
            channelName = channel.displayName,
            streamUrl = channel.source.streamUrl,
            title = title ?: channel.displayName,
            filePath = files.newFile(channel.displayName, startMs).path,
            startMs = startMs,
            plannedEndMs = endMs,
            endMs = null,
            status = RecordingStatus.SCHEDULED.name,
            sizeBytes = 0,
        )

    companion object {
        /** Playlist-refresh-stable channel identity, same key user flags use. */
        fun keyOf(channel: ChannelEntity): String = WatchHistory.identityOf(channel)
    }
}
