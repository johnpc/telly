package com.johncorser.telly.core

import android.content.Context
import com.johncorser.telly.core.settings.ParentalControls
import com.johncorser.telly.core.settings.TellySettings
import com.johncorser.telly.features.guide.GuideDeps
import com.johncorser.telly.features.history.WatchHistory
import com.johncorser.telly.features.playback.PlaybackDeps
import com.johncorser.telly.features.playback.PlaybackSources
import com.johncorser.telly.features.player.Media3PlayerEngine
import com.johncorser.telly.features.search.SearchDeps
import com.johncorser.telly.features.search.SearchRepository
import com.johncorser.telly.core.settings.SharedPrefsKeyValueStore as SettingsPrefsStore

private const val SEARCH_PREFS_NAME = "telly-search"

/** Playback slice bundle over [ServiceLocator]'s app-scoped singletons. */
fun ServiceLocator.playbackDeps(context: Context): PlaybackDeps =
    PlaybackDeps(
        sources =
            PlaybackSources(
                channelDao = database(context).channelDao(),
                epgRepository = epgRepository(context),
                history = WatchHistory(database(context).watchHistoryDao(), clock),
            ),
        keyValueStore = keyValueStore(context),
        engineFactory = { Media3PlayerEngine.create(context.applicationContext) },
        clock = clock,
        parental = ParentalControls(settingsRepository(context)),
    )

/** Guide slice = the playback bundle + the settings the grid honors. */
fun ServiceLocator.guideDeps(context: Context): GuideDeps =
    GuideDeps(
        playback = playbackDeps(context),
        pastDays = { settingsRepository(context).get(TellySettings.EPG_PAST_DAYS_TO_KEEP) },
    )

/** Search slice deps; history keeps its own prefs file, out of backups. */
fun ServiceLocator.searchDeps(context: Context): SearchDeps =
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
