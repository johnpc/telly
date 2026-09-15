package com.johncorser.telly.features.playlist.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** v9 adds the per-channel `blocked` flag behind "Block channel". */
object BlockedMigration {
    val MIGRATION_8_9 =
        object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE channels ADD COLUMN blocked INTEGER NOT NULL DEFAULT 0")
            }
        }
}
