package com.johncorser.telly.features.vod

import com.johncorser.telly.features.vod.db.VodItemDao
import com.johncorser.telly.features.vod.db.VodItemEntity
import com.johncorser.telly.features.vod.db.VodPositionDao
import com.johncorser.telly.features.vod.db.VodPositionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory DAO fakes for plain-JVM VOD tests. */

class FakeVodItemDao : VodItemDao {
    val items = MutableStateFlow<List<VodItemEntity>>(emptyList())

    override fun observeAll(): Flow<List<VodItemEntity>> = items

    override suspend fun byKey(itemKey: String): VodItemEntity? = items.value.firstOrNull { it.itemKey == itemKey }

    override suspend fun totalCount(): Int = items.value.size

    override suspend fun deleteForPlaylist(playlistId: Long) {
        items.value = items.value.filterNot { it.playlistId == playlistId }
    }

    override suspend fun insertAll(items: List<VodItemEntity>) {
        this.items.value = this.items.value + items
    }
}

class FakeVodPositionDao : VodPositionDao {
    val rows = MutableStateFlow<List<VodPositionEntity>>(emptyList())

    override fun observeAll(): Flow<List<VodPositionEntity>> = rows

    override suspend fun byKey(itemKey: String): VodPositionEntity? = rows.value.firstOrNull { it.itemKey == itemKey }

    override suspend fun upsert(position: VodPositionEntity) {
        rows.value = rows.value.filterNot { it.itemKey == position.itemKey } + position
    }

    override suspend fun delete(itemKey: String) {
        rows.value = rows.value.filterNot { it.itemKey == itemKey }
    }

    override suspend fun clearAll() {
        rows.value = emptyList()
    }
}
