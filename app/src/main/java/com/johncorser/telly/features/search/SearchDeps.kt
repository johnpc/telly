package com.johncorser.telly.features.search

import com.johncorser.telly.core.kv.KeyValueStore
import com.johncorser.telly.features.playback.ClockStyle
import com.johncorser.telly.core.settings.KeyValueStore as StringStore

/** Everything the search slice needs from the composition root. */
class SearchDeps(
    val repository: SearchRepository,
    val historyStore: StringStore,
    val lastChannelStore: KeyValueStore,
    val clock: () -> Long,
    /** Zone + the persisted 12/24-hour clock choice for air-time strings. */
    val style: ClockStyle = ClockStyle(),
    /** Settings -> Other -> Search "Save search history" (default on). */
    val saveHistory: () -> Boolean = { true },
)
