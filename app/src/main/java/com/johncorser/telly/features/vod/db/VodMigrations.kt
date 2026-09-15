package com.johncorser.telly.features.vod.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

private const val CREATE_VOD_ITEMS =
    "CREATE TABLE IF NOT EXISTS `vod_items` " +
        "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
        "`playlistId` INTEGER NOT NULL, `sortIndex` INTEGER NOT NULL, " +
        "`itemKey` TEXT NOT NULL, `name` TEXT NOT NULL, `groupTitle` TEXT, " +
        "`logoUrl` TEXT, `streamUrl` TEXT NOT NULL, " +
        "FOREIGN KEY(`playlistId`) REFERENCES `playlists`(`id`) " +
        "ON UPDATE NO ACTION ON DELETE CASCADE)"

private const val CREATE_VOD_ITEMS_INDEX =
    "CREATE INDEX IF NOT EXISTS `index_vod_items_playlistId` ON `vod_items` (`playlistId`)"

private const val CREATE_VOD_POSITIONS =
    "CREATE TABLE IF NOT EXISTS `vod_positions` " +
        "(`itemKey` TEXT NOT NULL, `positionMs` INTEGER NOT NULL, " +
        "`durationMs` INTEGER NOT NULL, `updatedAtMs` INTEGER NOT NULL, " +
        "PRIMARY KEY(`itemKey`))"

/**
 * VOD schema migration. NOTE FOR INTEGRATION: this slice claims schema
 * version 5 (v4 was current in this tree). If another slice lands first,
 * renumber HERE plus the version in TellyDatabase, the addMigrations line
 * in ServiceLocator, and the exported app/schemas 5.json — nothing else
 * is version-specific.
 */
object VodMigrations {
    /** v5 adds the vod_items and vod_positions tables (VOD slice). */
    val MIGRATION_4_5 =
        object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(CREATE_VOD_ITEMS)
                db.execSQL(CREATE_VOD_ITEMS_INDEX)
                db.execSQL(CREATE_VOD_POSITIONS)
            }
        }
}
