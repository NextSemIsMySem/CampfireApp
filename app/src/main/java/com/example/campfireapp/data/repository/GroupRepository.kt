package com.example.campfireapp.data.repository

import com.example.campfireapp.data.model.Group
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Group CRUD operations
 */
interface GroupRepository {
    suspend fun createGroup(group: Group): Result<Group>
    suspend fun updateGroup(group: Group): Result<Unit>
    suspend fun deleteGroup(groupId: String): Result<Unit>
    suspend fun getGroup(groupId: String): Group?
    suspend fun getUserGroups(userId: String): Flow<List<Group>>
    fun getGroupFlow(groupId: String): Flow<Group?> // <-- ADDED
    suspend fun joinGroup(groupId: String, userId: String): Result<Unit>
    suspend fun leaveGroup(groupId: String, userId: String): Result<Unit>
    suspend fun incrementMessageCount(groupId: String): Result<Unit>
    suspend fun updateLastActivity(groupId: String): Result<Unit>
}
