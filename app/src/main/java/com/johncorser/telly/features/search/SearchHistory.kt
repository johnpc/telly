package com.johncorser.telly.features.search

import com.johncorser.telly.core.settings.KeyValueStore

/**
 * The landing screen's "Search history" list (capture 49): committed queries,
 * most recent first, case-insensitively deduplicated and capped. Persisted
 * newline-joined in a string [KeyValueStore] — one list does not justify a
 * Room table (same reasoning as the lastChannelId decision).
 */
class SearchHistory(
    private val store: KeyValueStore,
    /** Settings -> Other -> Search "Save search history"; off = no recording. */
    private val saveEnabled: () -> Boolean = { true },
) {
    fun list(): List<String> = store.read(KEY)?.split(SEPARATOR)?.filter { it.isNotBlank() } ?: emptyList()

    /** Records a committed query; re-searching moves it back to the front. */
    fun record(raw: String) {
        if (!saveEnabled()) return
        val query = SearchQuery.normalize(raw)
        if (query.isEmpty()) return
        val entries = listOf(query) + list().filterNot { it.equals(query, ignoreCase = true) }
        store.write(KEY, entries.take(MAX_ENTRIES).joinToString(SEPARATOR.toString()))
    }

    /** The header's trash icon (capture 49). */
    fun clear() = store.write(KEY, null)

    companion object {
        const val KEY = "search.history"
        const val MAX_ENTRIES = 20
        private const val SEPARATOR = '\n'
    }
}
