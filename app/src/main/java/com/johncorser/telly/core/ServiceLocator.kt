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
import com.johncorser.telly.features.epg.EpgRetention
import com.johncorser.telly.features.epg.EpgSourceStore
import com.johncorser.telly.features.epg.RefreshScheduler
import com.johncorser.telly.features.epg.RoomEpgSourceStore
import com.johncorser.telly.features.mylist.MyListStore
import com.johncorser.telly.features.mylist.RoomMyListStore
import com.johncorser.telly.features.mylist.db.MyListMigration
import com.johncorser.telly.features.playlist.PlaylistRepository
import com.johncorser.telly.features.playlist.RoomPlaylistRepository
import com.johncorser.telly.features.reminders.db.ReminderMigration
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
            database ?: Room
                .databaseBuilder(context.applicationContext, TellyDatabase::class.java, DATABASE_NAME)
                .addMigrations(
                    TellyDatabase.MIGRATION_1_2,
                    TellyDatabase.MIGRATION_2_3,
                    TellyDatabase.MIGRATION_3_4,
                    ReminderMigration.MIGRATION_4_5,
                    MyListMigration.MIGRATION_5_6,
                )
                .build()
                .also { database = it }
        }

    fun settingsRepository(context: Context): SettingsRepository =
        settings ?: synchronized(this) {
            settings ?: SettingsRepository(
                SettingsPrefsStore(
                    context.applicationContext.getSharedPreferences(SETTINGS_PREFS_NAME, Context.MODE_PRIVATE),
                ),
            ).also { settings = it }
        }

    fun playlistRepository(context: Context): PlaylistRepository = RoomPlaylistRepository(database(context), clock)

    fun epgSourceStore(context: Context): EpgSourceStore = RoomEpgSourceStore(database(context).epgSourceDao(), clock)

    fun myListStore(context: Context): MyListStore = RoomMyListStore(database(context).myListDao())

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
            retention =
                EpgRetention(
                    keepPastMs = { EpgRefresher.daysToMs(prefs.get(TellySettings.EPG_PAST_DAYS_TO_KEEP)) },
                    trim = repository::trimEndedBefore,
                ),
            customSources = { playlistUrl -> epgSourceStore(context).forPlaylist(playlistUrl).map { it.url } },
        )
    }

    fun keyValueStore(context: Context): KeyValueStore =
        SharedPrefsKeyValueStore(context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))

    private const val PREFS_NAME = "telly"

    /** The one wall clock; feature dep bundles (ServiceLocatorDeps) inject it. */
    internal val clock: () -> Long = { System.currentTimeMillis() }
}
