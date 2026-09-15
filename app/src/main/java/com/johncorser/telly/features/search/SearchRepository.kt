package com.johncorser.telly.features.search

import com.johncorser.telly.features.epg.EpgRepository
import com.johncorser.telly.features.playlist.db.ChannelDao
import com.johncorser.telly.features.search.db.SearchDao
import kotlinx.coroutines.flow.first
import java.util.TimeZone

/**
 * Runs one search over Room: channels by name substring or number prefix
 * (name order, live tm-02), programmes by title substring still airing or
 * upcoming (soonest first). Escaping, filtering and formatting live in the
 * pure [SearchQuery]/[SearchResultsBuilder]; this class only orchestrates.
 */
class SearchRepository(
    private val searchDao: SearchDao,
    private val channelDao: ChannelDao,
    private val epgRepository: EpgRepository,
) {
    suspend fun search(
        raw: String,
        atMs: Long,
        zone: TimeZone,
    ): SearchResults {
        val query = SearchQuery.normalize(raw)
        if (query.isEmpty()) return SearchResults()
        // The DAO wrapper applies Manage groups; keep name matches inside it.
        val visible = channelDao.observeVisible().first()
        val visibleIds = visible.mapTo(HashSet()) { it.id }
        val channels =
            searchDao
                .channels(SearchQuery.nameLike(query), SearchQuery.numberLike(query))
                .filter { it.id in visibleIds }
        val guide = epgRepository.nowNext(channels.mapNotNull { it.source.tvgId }, atMs).first()
        val programs = searchDao.programs(SearchQuery.nameLike(query), atMs, PROGRAM_LIMIT)
        return SearchResults(
            query = query,
            channels = SearchResultsBuilder.channels(channels, guide, atMs),
            programs = SearchResultsBuilder.programs(programs, visible, atMs, zone),
        )
    }

    companion object {
        /**
         * Result cap for the programme list. Not capturable — the reference
         * list scrolls past the fold — so bounded only for query sanity.
         */
        const val PROGRAM_LIMIT = 100
    }
}
