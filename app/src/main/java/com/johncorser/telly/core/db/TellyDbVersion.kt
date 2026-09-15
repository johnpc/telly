package com.johncorser.telly.core.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// ⚠️ SCHEMA VERSION CLAIM — RENUMBER ON MERGE ⚠️
// This branch claims database version 5 (blocked-channel flag). Every
// version-specific bit lives in THIS file: the version constant below (the
// @Database annotation reads it) and the migration's start/end derive from
// it, so the merger only edits this one number when parallel branches also
// bump the schema. The exported schema JSON (app/schemas/.../<version>.json)
// must be renamed to match.

/** The current Room schema version. */
const val TELLY_DB_VERSION = 5

/** v5 adds the per-channel `blocked` flag behind "Block channel". */
val MIGRATION_TO_BLOCKED_CHANNELS: Migration =
    object : Migration(TELLY_DB_VERSION - 1, TELLY_DB_VERSION) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE channels ADD COLUMN blocked INTEGER NOT NULL DEFAULT 0")
        }
    }
