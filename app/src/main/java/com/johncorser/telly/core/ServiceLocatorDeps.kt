package com.johncorser.telly.core

import android.content.Context
import com.johncorser.telly.core.settings.ParentalControls
import com.johncorser.telly.core.settings.TellySettings
import com.johncorser.telly.features.guide.GuideDeps
import com.johncorser.telly.features.history.WatchHistory
import com.johncorser.telly.features.multiview.MultiviewDeps
import com.johncorser.telly.features.playback.PlaybackDeps
import com.johncorser.telly.features.playback.PlaybackHooks
import com.johncorser.telly.features.playback.PlaybackSources
import com.johncorser.telly.features.player.Media3PlayerEngine
import com.johncorser.telly.features.reminders.remindersHub
import com.johncorser.telly.features.search.SearchDeps
import com.johncorser.telly.features.search.SearchRepository
import com.johncorser.telly.core.settings.SharedPrefsKeyValueStore as SettingsPrefsStore

private const val SEARCH_PREFS_NAME = "telly-search"

/** Playback slice bundle over [ServiceLocator]'s app-scoped singletons. */
fun ServiceLocator.playbackDeps(
    context: Context,
    hooks: PlaybackHooks = PlaybackHooks(),
): PlaybackDeps =
    PlaybackDeps(
        sources =
            PlaybackSources(
                channelDao = database(context).channelDao(),
                epgRepository = epgRepository(context),
                history = WatchHistory(database(context).watchHistoryDao(), clock),
                myList = myListStore(context),
            ),
        keyValueStore = keyValueStore(context),
        engineFactory = { Media3PlayerEngine.create(context.applicationContext) },
        clock = clock,
        parental = ParentalControls(settingsRepository(context)),
        hooks = hooks,
    )

/** Guide slice = the playback bundle + the settings the grid honors. */
fun ServiceLocator.guideDeps(
    context: Context,
    hooks: PlaybackHooks = PlaybackHooks(),
): GuideDeps =
    GuideDeps(
        playback = playbackDeps(context, hooks),
        pastDays = { settingsRepository(context).get(TellySettings.EPG_PAST_DAYS_TO_KEEP) },
        reminders = remindersHub(context).guide,
    )

/**
 * Multiview slice: one independent engine per pane from the factory. The
 * pane engines do NOT handle audio focus (N players grabbing focus pause
 * one another); the focused pane owns audio via mute state instead.
 */
fun ServiceLocator.multiviewDeps(context: Context): MultiviewDeps =
    MultiviewDeps(
        channelDao = database(context).channelDao(),
        epgRepository = epgRepository(context),
        engines = { Media3PlayerEngine.create(context.applicationContext, handleAudioFocus = false) },
        store = keyValueStore(context),
        clock = clock,
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
