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
 * VOD schema migration. Integrated at v7: v5 = reminders, v6 = my_list +
 * favoriteOrder landed first, so the slice's original 4->5 claim was
 * renumbered here plus the version in TellyDatabase, the addMigrations line
 * in ServiceLocator, and the exported app/schemas 7.json — nothing else
 * is version-specific.
 */
object VodMigrations {
    /** v7 adds the vod_items and vod_positions tables (VOD slice). */
    val MIGRATION_6_7 =
        object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(CREATE_VOD_ITEMS)
                db.execSQL(CREATE_VOD_ITEMS_INDEX)
                db.execSQL(CREATE_VOD_POSITIONS)
            }
        }
}
