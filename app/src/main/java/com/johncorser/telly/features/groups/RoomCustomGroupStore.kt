package com.johncorser.telly.features.groups

import com.johncorser.telly.features.groups.db.CustomGroupDao
import com.johncorser.telly.features.groups.db.CustomGroupEntity
import com.johncorser.telly.features.groups.db.CustomGroupMemberEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** Room-backed [CustomGroupStore] over [CustomGroupDao]. */
class RoomCustomGroupStore(
    private val dao: CustomGroupDao,
) : CustomGroupStore {
    override fun observe(): Flow<List<CustomGroup>> =
        combine(dao.observeGroups(), dao.observeMembers()) { groups, members ->
            val byGroup = members.groupBy({ it.groupId }, { it.channelKey })
            groups.map { CustomGroup(id = it.id, name = it.name, members = byGroup[it.id].orEmpty().toSet()) }
        }

    override suspend fun create(name: String): Long =
        dao.insertGroup(CustomGroupEntity(name = name, sortIndex = dao.maxSortIndex() + 1))

    override suspend fun rename(
        id: Long,
        name: String,
    ) = dao.renameGroup(id, name)

    override suspend fun delete(id: Long) = dao.deleteGroup(id)

    override suspend fun addMembers(
        id: Long,
        channelKeys: Collection<String>,
    ) = dao.insertMembers(channelKeys.map { CustomGroupMemberEntity(groupId = id, channelKey = it) })
}
