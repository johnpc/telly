package com.johncorser.telly.features.groups

import kotlinx.coroutines.flow.Flow

/**
 * One custom group with its membership, as consumed by the group columns:
 * [members] holds playlist-refresh-stable channel keys
 * ([com.johncorser.telly.features.playlist.ChannelImporter.keyOf]).
 */
data class CustomGroup(
    val id: Long,
    val name: String,
    val members: Set<String> = emptySet(),
)

/**
 * User-created channel groups (Create group / Copy channels / Group
 * options). Room-backed in production ([RoomCustomGroupStore]); tests and
 * default wiring use [InMemoryCustomGroupStore].
 */
interface CustomGroupStore {
    /** All custom groups in their column order, membership attached. */
    fun observe(): Flow<List<CustomGroup>>

    suspend fun create(name: String): Long

    suspend fun rename(
        id: Long,
        name: String,
    )

    suspend fun delete(id: Long)

    /** Copies channels in by key; already-present keys are no-ops. */
    suspend fun addMembers(
        id: Long,
        channelKeys: Collection<String>,
    )
}
