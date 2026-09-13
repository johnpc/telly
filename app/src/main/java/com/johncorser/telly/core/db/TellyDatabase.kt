package com.johncorser.telly.core.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.johncorser.telly.features.epg.db.ProgramDao
import com.johncorser.telly.features.epg.db.ProgramEntity
import com.johncorser.telly.features.playlist.db.ChannelDao
import com.johncorser.telly.features.playlist.db.ChannelEntity
import com.johncorser.telly.features.playlist.db.PlaylistDao
import com.johncorser.telly.features.playlist.db.PlaylistEntity

/** The single app database; schema JSON is exported to app/schemas. */
@Database(
    entities = [PlaylistEntity::class, ChannelEntity::class, ProgramEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class TellyDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao

    abstract fun channelDao(): ChannelDao

    abstract fun programDao(): ProgramDao
}
