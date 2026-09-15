package com.johncorser.telly.features.mylist

import com.johncorser.telly.features.mylist.db.MyListEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** Map-backed [MyListStore] mirroring the DAO's newest-first ordering. */
class InMemoryMyListStore : MyListStore {
    private val saved = MutableStateFlow<List<MyListEntity>>(emptyList())

    override val entries: Flow<List<MyListEntity>> =
        saved.map { list ->
            list.sortedWith(
                compareByDescending(MyListEntity::addedAtMs)
                    .thenBy(MyListEntity::channelKey)
                    .thenBy(MyListEntity::startMs),
            )
        }

    override suspend fun save(entry: MyListEntity) {
        saved.update { list -> list.filterNot { it.matches(entry.channelKey, entry.startMs) } + entry }
    }

    override suspend fun remove(
        channelKey: String,
        startMs: Long,
    ) {
        saved.update { list -> list.filterNot { it.matches(channelKey, startMs) } }
    }

    private fun MyListEntity.matches(
        key: String,
        start: Long,
    ): Boolean = channelKey == key && startMs == start
}
