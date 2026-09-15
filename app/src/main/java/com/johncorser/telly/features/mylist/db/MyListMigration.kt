package com.johncorser.telly.features.mylist.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * The whole v6 schema change in one place so it stays trivial to renumber:
 * the `my_list` table behind "Add to My list" plus the `favoriteOrder`
 * column that persists the Manage-Favorites ordering on channels.
 */
object MyListMigration {
    val MIGRATION_5_6: Migration =
        object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `my_list` " +
                        "(`channelKey` TEXT NOT NULL, `startMs` INTEGER NOT NULL, " +
                        "`endMs` INTEGER NOT NULL, `title` TEXT NOT NULL, " +
                        "`description` TEXT, `addedAtMs` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`channelKey`, `startMs`))",
                )
                db.execSQL(
                    "ALTER TABLE channels ADD COLUMN favoriteOrder INTEGER NOT NULL DEFAULT 0",
                )
            }
        }
}
