package com.johncorser.telly.features.groups.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v12: the custom-groups slice — the `custom_groups` + `custom_group_members`
 * tables behind Create group / Copy channels / Group options, plus the
 * per-channel `epgOverride` column (Assign EPG) on `channels` (inside the
 * ChannelOverrides embed). The slice's original `channels.blocked` column is
 * NOT added here: main already ships it (BlockedMigration, v9) and the bulk
 * Manage-blocking editor reuses that flag.
 */
object CustomGroupsMigration {
    val MIGRATION_11_12 =
        object : Migration(11, 12) {
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
                db.execSQL("ALTER TABLE channels ADD COLUMN epgOverride TEXT")
            }
        }
}
