package com.johncorser.telly.features.groups.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v5: the custom-groups slice — the `custom_groups` + `custom_group_members`
 * tables behind Create group / Copy channels / Group options, plus the
 * per-channel `blocked` flag (Manage blocking) and `epgOverride` column
 * (Assign EPG) on `channels`.
 *
 * NOTE for parallel feature branches: this is the ONE migration this slice
 * adds. If another branch claims version 5 first, renumber here (4→5 to
 * N→N+1), in [com.johncorser.telly.core.db.TellyDatabase]'s `version`, and
 * in ServiceLocator's `addMigrations` — nothing else references the number.
 */
object CustomGroupsMigration {
    val MIGRATION_4_5 =
        object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `custom_groups` " +
                        "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`name` TEXT NOT NULL, `sortIndex` INTEGER NOT NULL)",
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `custom_group_members` " +
                        "(`groupId` INTEGER NOT NULL, `channelKey` TEXT NOT NULL, " +
                        "PRIMARY KEY(`groupId`, `channelKey`))",
                )
                db.execSQL("ALTER TABLE channels ADD COLUMN blocked INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE channels ADD COLUMN epgOverride TEXT")
            }
        }
}
