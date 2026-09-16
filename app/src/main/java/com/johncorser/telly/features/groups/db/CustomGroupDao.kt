package com.johncorser.telly.features.groups.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/** Persistence for custom groups and their channel membership. */
@Dao
interface CustomGroupDao {
    @Query("SELECT * FROM custom_groups ORDER BY sortIndex")
    fun observeGroups(): Flow<List<CustomGroupEntity>>

    @Query("SELECT * FROM custom_group_members")
    fun observeMembers(): Flow<List<CustomGroupMemberEntity>>

    @Query("SELECT COALESCE(MAX(sortIndex), -1) FROM custom_groups")
    suspend fun maxSortIndex(): Int

    @Insert
    suspend fun insertGroup(group: CustomGroupEntity): Long

    @Query("UPDATE custom_groups SET name = :name WHERE id = :id")
    suspend fun renameGroup(
        id: Long,
        name: String,
    )

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMembers(members: List<CustomGroupMemberEntity>)

    @Query("DELETE FROM custom_groups WHERE id = :id")
    suspend fun deleteGroupRow(id: Long)

    @Query("DELETE FROM custom_group_members WHERE groupId = :id")
    suspend fun deleteMembersOf(id: Long)

    /** Deleting a group removes its membership rows with it. */
    @Transaction
    suspend fun deleteGroup(id: Long) {
        deleteMembersOf(id)
        deleteGroupRow(id)
    }
}
