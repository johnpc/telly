package com.johncorser.telly.features.settings

import java.io.File

/**
 * The raw-file-path half of [MediaStoreBackupDocuments]: works pre-29 with
 * legacy access, on 29+ for files this install contributed, and always
 * under all-files access — the only way to overwrite an orphaned
 * previous-install file. Every method is failure-tolerant by contract.
 */
class BackupDirectFiles(
    /** .../Documents/telly (under the public Documents collection). */
    private val dir: File,
) {
    fun write(json: String): Boolean =
        runCatching {
            val file = candidates().firstOrNull() ?: canonical().apply { parentFile?.mkdirs() }
            file.writeText(json)
        }.isSuccess

    fun read(): String? = runCatching { candidates().firstOrNull()?.readText() }.getOrNull()

    fun deleteAll() {
        runCatching { candidates().forEach(File::delete) }
    }

    fun sighted(): Boolean = runCatching { canonical().exists() || candidates().isNotEmpty() }.getOrDefault(false)

    private fun canonical(): File = File(dir, AutoBackupLocation.FILE_NAME)

    /** Readable backup files on disk, newest first (canonical + renames). */
    private fun candidates(): List<File> =
        dir
            .listFiles { file -> AutoBackupLocation.isBackupName(file.name) && file.canRead() }
            .orEmpty()
            .sortedByDescending(File::lastModified)
}
