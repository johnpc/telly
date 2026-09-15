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

/**
 * Owns the .ts files under the app's external "recordings" dir: derives
 * capture filenames from channel + start stamp, deletes captures and sums
 * storage for the settings pane. The directory comes injected (production:
 * `context.getExternalFilesDir("recordings")`), so everything here is
 * plain-JVM testable against a temp dir.
 */
class RecordingFiles(
    private val directory: () -> File,
    private val zone: TimeZone = TimeZone.getDefault(),
) {
    private fun dir(): File = directory().apply { mkdirs() }

    /** A fresh capture file "News_One-20260915-2135.ts" (deduped suffix). */
    fun newFile(
        channelName: String,
        startMs: Long,
    ): File {
        val stamp =
            SimpleDateFormat("yyyyMMdd-HHmm", Locale.US)
                .apply { timeZone = zone }
                .format(Date(startMs))
        val base = "${sanitize(channelName)}-$stamp"
        var candidate = File(dir(), "$base.ts")
        var suffix = 1
        while (candidate.exists()) {
            candidate = File(dir(), "$base-$suffix.ts")
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
}
