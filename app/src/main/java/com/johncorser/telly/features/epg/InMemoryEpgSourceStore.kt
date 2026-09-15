package com.johncorser.telly.features.epg

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Process-lifetime EPG source store, kept for fast JVM tests. */
class InMemoryEpgSourceStore : EpgSourceStore {
    private val mutableSources = MutableStateFlow(emptyList<EpgSource>())
    private var nextId = 1L

    override val sources: StateFlow<List<EpgSource>> = mutableSources.asStateFlow()

    override suspend fun forPlaylist(playlistUrl: String): List<EpgSource> =
        mutableSources.value.filter { it.playlistUrl == playlistUrl }

    override suspend fun add(
        playlistUrl: String,
        url: String,
    ) {
        mutableSources.update { current ->
            current.filterNot { it.playlistUrl == playlistUrl && it.url == url } +
                EpgSource(id = nextId++, playlistUrl = playlistUrl, url = url)
        }
    }

    override suspend fun setUrl(
        id: Long,
        url: String,
    ) {
        mutableSources.update { current -> current.map { if (it.id == id) it.copy(url = url) else it } }
    }

    override suspend fun remove(id: Long) {
        mutableSources.update { current -> current.filterNot { it.id == id } }
    }
}
