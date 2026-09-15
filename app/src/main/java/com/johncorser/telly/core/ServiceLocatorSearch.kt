package com.johncorser.telly.core

import android.content.Context
import com.johncorser.telly.features.search.SearchDeps
import com.johncorser.telly.features.search.SearchRepository
import com.johncorser.telly.core.settings.SharedPrefsKeyValueStore as SettingsPrefsStore

private const val SEARCH_PREFS_NAME = "telly-search"

/** Search slice deps; history keeps its own prefs file, out of backups. */
fun ServiceLocator.searchDeps(context: Context): SearchDeps =
    SearchDeps(
        repository =
            SearchRepository(
                searchDao = database(context).searchDao(),
                channelDao = visibleChannelDao(context),
                epgRepository = epgRepository(context),
            ),
        historyStore =
            SettingsPrefsStore(
                context.applicationContext.getSharedPreferences(SEARCH_PREFS_NAME, Context.MODE_PRIVATE),
            ),
        lastChannelStore = keyValueStore(context),
        clock = clock,
    )
