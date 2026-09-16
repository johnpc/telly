package com.johncorser.telly.features.groups

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/** Map-backed [CustomGroupStore] for tests and default wiring. */
class InMemoryCustomGroupStore(
    initial: List<CustomGroup> = emptyList(),
) : CustomGroupStore {
    val groups = MutableStateFlow(initial)
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0) + 1

    override fun observe(): Flow<List<CustomGroup>> = groups

    override suspend fun create(name: String): Long {
        val id = nextId++
        groups.update { it + CustomGroup(id = id, name = name) }
        return id
    }

    override suspend fun rename(
        id: Long,
        name: String,
    ) {
        groups.update { list -> list.map { if (it.id == id) it.copy(name = name) else it } }
    }

    override suspend fun delete(id: Long) {
        groups.update { list -> list.filterNot { it.id == id } }
    }

    override suspend fun addMembers(
        id: Long,
        channelKeys: Collection<String>,
    ) {
        groups.update { list -> list.map { if (it.id == id) it.copy(members = it.members + channelKeys) else it } }
    }
}
