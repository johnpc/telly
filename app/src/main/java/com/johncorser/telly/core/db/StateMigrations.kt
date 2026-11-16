package com.johncorser.telly.core.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v13 (2026-11-16, state-machine pass): the two persisted stamps that turn
 * in-memory one-shots into real state — a playlist's failed EPG attempt
 * (EpgFreshness backoff survives a restart) and a reminder's claimed popup
 * (a reminder can never fire twice across a crash).
 */
object StateMigrations {
    val MIGRATION_12_13 =
        object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE playlists ADD COLUMN epgFailedAtMs INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE reminders ADD COLUMN firedAtMs INTEGER NOT NULL DEFAULT 0")
            }
        }
}
