package com.johncorser.telly.features.catchup.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** v10 adds the catch-up attribute columns on channels (catch-up slice). */
object CatchupMigration {
    val MIGRATION_9_10 =
        object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE channels ADD COLUMN catchupType TEXT")
                db.execSQL("ALTER TABLE channels ADD COLUMN catchupSource TEXT")
                db.execSQL("ALTER TABLE channels ADD COLUMN catchupDays INTEGER")
            }
        }
}
