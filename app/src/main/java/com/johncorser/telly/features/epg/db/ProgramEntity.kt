package com.johncorser.telly.features.epg.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Descriptive programme fields shared by the XMLTV parser and the DB row. */
data class ProgramDetails(
    val title: String,
    val description: String? = null,
    val category: String? = null,
    val episode: String? = null,
)

/**
 * One guide programme. Rows are keyed to channels by tvg-id (the XMLTV
 * `channel` attribute); the unique (channelTvgId, startMs) index gives
 * refreshes upsert-replace semantics and serves the guide window query.
 */
@Entity(
    tableName = "programs",
    indices = [Index(value = ["channelTvgId", "startMs"], unique = true)],
)
data class ProgramEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val channelTvgId: String,
    val startMs: Long,
    val endMs: Long,
    @Embedded val details: ProgramDetails,
)
