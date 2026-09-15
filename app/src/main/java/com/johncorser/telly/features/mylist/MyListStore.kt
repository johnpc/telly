package com.johncorser.telly.features.mylist

import com.johncorser.telly.features.mylist.db.MyListDao
import com.johncorser.telly.features.mylist.db.MyListEntity
import kotlinx.coroutines.flow.Flow

/**
 * Storage boundary for saved My-list programmes so the toggle, the sheet
 * labels and the My List screen stay plain-JVM testable (the EpgSourceStore
 * precedent: Room on device, in-memory in tests).
 */
interface MyListStore {
    /** Saved programmes, most recently added first. */
    val entries: Flow<List<MyListEntity>>

    suspend fun save(entry: MyListEntity)

    suspend fun remove(
        channelKey: String,
        startMs: Long,
    )
}

/** Room-backed store over [MyListDao]. */
class RoomMyListStore(
    private val dao: MyListDao,
) : MyListStore {
    override val entries: Flow<List<MyListEntity>> = dao.observeAll()

    override suspend fun save(entry: MyListEntity) = dao.upsert(entry)

    override suspend fun remove(
        channelKey: String,
        startMs: Long,
    ) = dao.delete(channelKey, startMs)
}
