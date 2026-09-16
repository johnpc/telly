package com.johncorser.telly.features.settings

/**
 * The shared-storage home of the automatic backup JSON
 * ([AutoBackupLocation]). Implementations must be uninstall-proof on the
 * write side (shared storage outlives the app) and honest on the read side
 * (null = nothing readable right now, which on API 30+ may mean "an old
 * install's file exists but needs the all-files-access grant").
 */
interface BackupDocumentsStore {
    /** Writes/overwrites the backup JSON; throws when it cannot. */
    fun write(json: String)

    /** The backup JSON, or null when absent or not readable. */
    fun read(): String?

    /** Removes the backup file (test seam + post-restore cleanup). */
    fun delete()

    /** True when a backup file is at least VISIBLE (maybe not readable). */
    fun sighted(): Boolean = read() != null
}
