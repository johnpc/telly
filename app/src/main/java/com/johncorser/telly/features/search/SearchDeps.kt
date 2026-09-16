package com.johncorser.telly.features.search

import com.johncorser.telly.core.kv.KeyValueStore
import com.johncorser.telly.features.playback.ClockStyle

/** Everything the search slice needs from the composition root. */
class SearchDeps(
    val repository: SearchRepository,
    /** Committed-query history (its store honors the save toggle). */
    val history: SearchHistory,
    val lastChannelStore: KeyValueStore,
    val clock: () -> Long,
    /** Zone + the persisted 12/24-hour clock choice for air-time strings. */
    val style: ClockStyle = ClockStyle(),
    /** Dropdown collaborators (guide parity) + the voice-orb seam. */
    val hooks: SearchHooks = SearchHooks(),
)
