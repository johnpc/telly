package com.johncorser.telly.features.playlist.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v11 adds the per-channel "Channel options" override columns (§41 pane).
 *
 * RENUMBERABLE: written on a parallel slice branch — if another slice claims
 * v11 first, bump FROM/TO (and TellyDatabase.version + the schema JSON) at
 * merge time; the SQL is independent of the version numbers.
 */
object ChannelOptionsMigration {
    val MIGRATION_10_11 =
        object : Migration(FROM, TO) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE channels ADD COLUMN customName TEXT")
                db.execSQL("ALTER TABLE channels ADD COLUMN audioDecoder TEXT")
                db.execSQL("ALTER TABLE channels ADD COLUMN videoDecoder TEXT")
                db.execSQL("ALTER TABLE channels ADD COLUMN epgOffsetMinutes INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE channels ADD COLUMN externalPlayer TEXT")
            }
        }

    private const val FROM = 10
    private const val TO = 11
}
