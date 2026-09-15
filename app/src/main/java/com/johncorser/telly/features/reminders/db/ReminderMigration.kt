package com.johncorser.telly.features.reminders.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** v5 adds the reminders table (guide dropdown "Remind"). */
object ReminderMigration {
    val MIGRATION_4_5 =
        object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `reminders` " +
                        "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`channelId` INTEGER NOT NULL, `title` TEXT NOT NULL, " +
                        "`startMs` INTEGER NOT NULL, `stopMs` INTEGER NOT NULL)",
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_reminders_channelId_startMs_title` " +
                        "ON `reminders` (`channelId`, `startMs`, `title`)",
                )
            }
        }
}
