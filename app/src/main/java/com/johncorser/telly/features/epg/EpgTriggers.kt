package com.johncorser.telly.features.epg

import com.johncorser.telly.features.playlist.db.PlaylistEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * The playlists emissions "Update EPG on playlists change" should react to.
 * Room re-emits the whole table on ANY write to it — including the EPG
 * refresher's own `markEpgUpdated` / `markEpgFailed` stamps — so reacting to
 * raw emissions made every EPG refresh trigger the next one: on the Shield a
 * full 20k-programme download + rewrite ran every ~6 s, forever (Nov 2026),
 * and playback rebuffered and froze under it. Only a change to the set of
 * playlists or their EPG URLs (each emission = (playlist URL, EPG URL)
 * pairs) counts; the first value is the start state.
 */
fun Flow<List<PlaylistEntity>>.epgRelevantChanges(): Flow<List<Pair<String, String?>>> =
    map { rows -> rows.map { it.url to it.epgUrl } }.distinctUntilChanged()
