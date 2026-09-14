package com.johncorser.telly.features.search

import com.johncorser.telly.core.kv.KeyValueStore
import java.util.TimeZone
import com.johncorser.telly.core.settings.KeyValueStore as StringStore

/** Everything the search slice needs from the composition root. */
class SearchDeps(
    val repository: SearchRepository,
    val historyStore: StringStore,
    val lastChannelStore: KeyValueStore,
    val clock: () -> Long,
    val zone: TimeZone = TimeZone.getDefault(),
)
