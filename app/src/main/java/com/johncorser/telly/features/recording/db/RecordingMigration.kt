package com.johncorser.telly.features.recording.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** v8 adds the recordings table (DVR slice). */
object RecordingMigration {
    val MIGRATION_7_8 =
        object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `recordings` " +
                        "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`channelKey` TEXT NOT NULL, `channelName` TEXT NOT NULL, " +
                        "`streamUrl` TEXT NOT NULL, `title` TEXT NOT NULL, " +
                        "`filePath` TEXT NOT NULL, `startMs` INTEGER NOT NULL, " +
                        "`plannedEndMs` INTEGER NOT NULL, `endMs` INTEGER, " +
                        "`status` TEXT NOT NULL, `sizeBytes` INTEGER NOT NULL)",
                )
            }
        }
}
