package com.johncorser.telly.core

import android.content.Context
import com.johncorser.telly.core.settings.TellySettings
import com.johncorser.telly.features.recording.recordingCenter
import com.johncorser.telly.features.reminders.remindersHub
import com.johncorser.telly.features.search.SearchDeps
import com.johncorser.telly.features.search.SearchHistory
import com.johncorser.telly.features.search.SearchHooks
import com.johncorser.telly.features.search.SearchRepository
import com.johncorser.telly.features.search.VoiceSearch
import com.johncorser.telly.core.settings.KeyValueStore as StringKeyValueStore
import com.johncorser.telly.core.settings.SharedPrefsKeyValueStore as SettingsPrefsStore

private const val SEARCH_PREFS_NAME = "telly-search"

/** Search slice deps; history keeps its own prefs file, out of backups. */
fun ServiceLocator.searchDeps(
    context: Context,
    voice: VoiceSearch = VoiceSearch(),
): SearchDeps =
    SearchDeps(
        repository =
            SearchRepository(
                searchDao = database(context).searchDao(),
                channelDao = visibleChannelDao(context),
                epgRepository = epgRepository(context),
            ),
        history =
            SearchHistory(
                store = searchHistoryStore(context),
                saveEnabled = { settingsRepository(context).get(TellySettings.SEARCH_SAVE_HISTORY) },
            ),
        lastChannelStore = keyValueStore(context),
        clock = clock,
        // The programme dropdown drives the SAME app-scoped stores as the
        // guide's cell dropdown (reminders hub, My list, DVR center).
        hooks =
            SearchHooks(
                reminders = remindersHub(context).guide,
                myList = myListStore(context),
                recording = recordingCenter(context),
                voice = voice,
            ),
    )

/** The search-history prefs file, shared with Settings -> Other -> Search. */
fun ServiceLocator.searchHistoryStore(context: Context): StringKeyValueStore =
    SettingsPrefsStore(
        context.applicationContext.getSharedPreferences(SEARCH_PREFS_NAME, Context.MODE_PRIVATE),
    )
