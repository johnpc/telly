package com.johncorser.telly.core

import android.content.Context
import android.util.Xml
import androidx.room.Room
import com.johncorser.telly.core.db.TellyDatabase
import com.johncorser.telly.core.kv.KeyValueStore
import com.johncorser.telly.core.kv.SharedPrefsKeyValueStore
import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.core.settings.TellySettings
import com.johncorser.telly.features.epg.EpgRefresher
import com.johncorser.telly.features.epg.EpgRepository
import com.johncorser.telly.features.epg.RefreshScheduler
import com.johncorser.telly.features.playback.PlaybackDeps
import com.johncorser.telly.features.player.Media3PlayerEngine
import com.johncorser.telly.features.playlist.PlaylistRepository
import com.johncorser.telly.features.playlist.RoomPlaylistRepository
import com.johncorser.telly.features.search.SearchDeps
import com.johncorser.telly.features.search.SearchRepository
import com.johncorser.telly.core.settings.SharedPrefsKeyValueStore as SettingsPrefsStore

/**
 * Hand-rolled application-scoped composition root (see CLAUDE.md decisions:
 * no DI framework). This is the only place logic meets the wall clock and
 * Android's XmlPullParser; everything it builds takes both injected.
 */
object ServiceLocator {
    private const val DATABASE_NAME = "telly.db"
    private const val SETTINGS_PREFS_NAME = "telly-settings"

    @Volatile
    private var database: TellyDatabase? = null

    @Volatile
    private var settings: SettingsRepository? = null

    fun database(context: Context): TellyDatabase =
        database ?: synchronized(this) {
            database ?: buildDatabase(context).also { database = it }
        }

    fun settingsRepository(context: Context): SettingsRepository =
        settings ?: synchronized(this) {
            settings ?: buildSettings(context).also { settings = it }
        }

    fun playlistRepository(context: Context): PlaylistRepository = RoomPlaylistRepository(database(context), clock)

    fun epgRepository(context: Context): EpgRepository =
        EpgRepository(
            programDao = database(context).programDao(),
            newParser = { Xml.newPullParser() },
            storeDescriptions = {
                settingsRepository(context).get(TellySettings.EPG_STORE_DESCRIPTIONS)
            },
        )

    /** EPG refresh policy driven live by Settings -> EPG. */
    fun epgRefresher(context: Context): EpgRefresher {
        val repository = epgRepository(context)
        val prefs = settingsRepository(context)
        return EpgRefresher(
            playlistDao = database(context).playlistDao(),
            scheduler =
                RefreshScheduler(
                    intervalMs = {
                        RefreshScheduler.hoursToMs(prefs.get(TellySettings.EPG_UPDATE_INTERVAL_HOURS))
                    },
                ),
            clock = clock,
            refresh = repository::refresh,
            keepPastMs = { EpgRefresher.daysToMs(prefs.get(TellySettings.EPG_PAST_DAYS_TO_KEEP)) },
            trim = repository::trimEndedBefore,
        )
    }

    private fun buildSettings(context: Context): SettingsRepository =
        SettingsRepository(
            SettingsPrefsStore(
                context.applicationContext.getSharedPreferences(SETTINGS_PREFS_NAME, Context.MODE_PRIVATE),
            ),
        )

    fun keyValueStore(context: Context): KeyValueStore =
        SharedPrefsKeyValueStore(context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))

    fun playbackDeps(context: Context): PlaybackDeps =
        PlaybackDeps(
            channelDao = database(context).channelDao(),
            epgRepository = epgRepository(context),
            keyValueStore = keyValueStore(context),
            engineFactory = { Media3PlayerEngine.create(context.applicationContext) },
            clock = clock,
        )

    /** Search slice deps; history keeps its own prefs file, out of backups. */
    fun searchDeps(context: Context): SearchDeps =
        SearchDeps(
            repository =
                SearchRepository(
                    searchDao = database(context).searchDao(),
                    channelDao = database(context).channelDao(),
                    epgRepository = epgRepository(context),
                ),
            historyStore =
                SettingsPrefsStore(
                    context.applicationContext.getSharedPreferences(SEARCH_PREFS_NAME, Context.MODE_PRIVATE),
                ),
            lastChannelStore = keyValueStore(context),
            clock = clock,
        )

    private const val PREFS_NAME = "telly"
    private const val SEARCH_PREFS_NAME = "telly-search"

    private val clock: () -> Long = { System.currentTimeMillis() }

    private fun buildDatabase(context: Context): TellyDatabase =
        Room
            .databaseBuilder(context.applicationContext, TellyDatabase::class.java, DATABASE_NAME)
            .addMigrations(TellyDatabase.MIGRATION_1_2)
            .build()
}
