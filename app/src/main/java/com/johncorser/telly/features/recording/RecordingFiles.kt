package com.johncorser.telly.features.recording

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Storage-used / free-space snapshot for the settings pane. */
data class RecordingStorageInfo(
    val usedBytes: Long,
    val freeBytes: Long,
)

/** Picks the capture file extension a stream's container needs. */
fun interface StreamContainer {
    suspend fun extensionOf(streamUrl: String): String
}

/**
 * Owns the capture files under the app's external "recordings" dir:
 * derives capture filenames from channel + start stamp (extension per the
 * injected [StreamContainer] — `.ts` for byte copies and TS-segment HLS,
 * `.mp4` for fMP4 HLS), deletes captures and sums storage for the settings
 * pane. The directory comes injected (production:
 * `context.getExternalFilesDir("recordings")`), so everything here is
 * plain-JVM testable against a temp dir.
 */
class RecordingFiles(
    private val directory: () -> File,
    private val zone: TimeZone = TimeZone.getDefault(),
    private val container: StreamContainer = StreamContainer { DEFAULT_EXTENSION },
) {
    private fun dir(): File = directory().apply { mkdirs() }

    /** [newFile] with the extension [streamUrl]'s container calls for. */
    suspend fun newFileFor(
        channelName: String,
        startMs: Long,
        streamUrl: String,
    ): File = newFile(channelName, startMs, container.extensionOf(streamUrl))

    /** A fresh capture file "News_One-20260915-2135.ts" (deduped suffix). */
    fun newFile(
        channelName: String,
        startMs: Long,
        extension: String = DEFAULT_EXTENSION,
    ): File {
        val stamp =
            SimpleDateFormat("yyyyMMdd-HHmm", Locale.US)
                .apply { timeZone = zone }
                .format(Date(startMs))
        val base = "${sanitize(channelName)}-$stamp"
        var candidate = File(dir(), "$base.$extension")
        var suffix = 1
        while (candidate.exists()) {
            candidate = File(dir(), "$base-$suffix.$extension")
            suffix += 1
        }
        return candidate
    }

    fun sizeOf(path: String): Long = File(path).length()

    fun exists(path: String): Boolean = File(path).isFile

    fun delete(path: String) {
        if (path.isNotBlank()) File(path).delete()
    }

    /** Settings "Delete all recordings": clears the whole capture dir. */
    fun deleteAllFiles() {
        dir().listFiles().orEmpty().filter { it.isFile }.forEach { it.delete() }
    }

    /** Bytes of every capture plus the volume's free space. */
    fun storage(): RecordingStorageInfo {
        val root = dir()
        val used = root.listFiles().orEmpty().filter { it.isFile }.sumOf { it.length() }
        return RecordingStorageInfo(usedBytes = used, freeBytes = root.usableSpace)
    }

    private fun sanitize(name: String): String =
        name
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
            .ifBlank { "recording" }

    companion object {
        private const val DEFAULT_EXTENSION = "ts"
    }
}
