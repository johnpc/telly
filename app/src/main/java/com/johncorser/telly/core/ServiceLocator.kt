package com.johncorser.telly.core

import android.content.Context
import android.util.Xml
import androidx.room.Room
import com.johncorser.telly.core.db.TellyDatabase
import com.johncorser.telly.features.epg.EpgRefresher
import com.johncorser.telly.features.epg.EpgRepository
import com.johncorser.telly.features.epg.RefreshScheduler
import com.johncorser.telly.features.playlist.PlaylistRepository
import com.johncorser.telly.features.playlist.RoomPlaylistRepository

/**
 * Hand-rolled application-scoped composition root (see CLAUDE.md decisions:
 * no DI framework). This is the only place logic meets the wall clock and
 * Android's XmlPullParser; everything it builds takes both injected.
 */
object ServiceLocator {
    private const val DATABASE_NAME = "telly.db"

    @Volatile
    private var database: TellyDatabase? = null

    fun database(context: Context): TellyDatabase =
        database ?: synchronized(this) {
            database ?: buildDatabase(context).also { database = it }
        }

    fun playlistRepository(context: Context): PlaylistRepository = RoomPlaylistRepository(database(context), clock)

    fun epgRepository(context: Context): EpgRepository =
        EpgRepository(
            programDao = database(context).programDao(),
            newParser = { Xml.newPullParser() },
        )

    fun epgRefresher(context: Context): EpgRefresher =
        EpgRefresher(
            playlistDao = database(context).playlistDao(),
            scheduler = RefreshScheduler(),
            clock = clock,
            refresh = epgRepository(context)::refresh,
        )

    private val clock: () -> Long = { System.currentTimeMillis() }

    private fun buildDatabase(context: Context): TellyDatabase =
        Room
            .databaseBuilder(context.applicationContext, TellyDatabase::class.java, DATABASE_NAME)
            .build()
}
