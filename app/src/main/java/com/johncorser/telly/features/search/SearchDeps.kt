package com.johncorser.telly.features.search

import com.johncorser.telly.core.kv.KeyValueStore
import java.util.TimeZone

/** Everything the search slice needs from the composition root. */
class SearchDeps(
    val repository: SearchRepository,
    /** Committed-query history (its store honors the save toggle). */
    val history: SearchHistory,
    val lastChannelStore: KeyValueStore,
    val clock: () -> Long,
    val zone: TimeZone = TimeZone.getDefault(),
    /** Dropdown collaborators (guide parity) + the voice-orb seam. */
    val hooks: SearchHooks = SearchHooks(),
)
