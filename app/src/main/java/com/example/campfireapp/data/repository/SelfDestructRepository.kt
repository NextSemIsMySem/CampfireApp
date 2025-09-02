package com.example.campfireapp.data.repository

import com.example.campfireapp.data.model.Group
import com.example.campfireapp.data.model.SelfDestructRule

/**
 * Repository interface for Self-Destruct Rule operations
 */
interface SelfDestructRepository {
    suspend fun checkAndDestroyGroups(): Result<List<String>> // Returns list of destroyed group IDs
    suspend fun shouldDestroyGroup(group: Group, messageCount: Int): Boolean
}

/**
 * Service to handle self-destruct logic for groups and messages
 */
interface SelfDestructService {
    suspend fun checkAllGroups()
    suspend fun checkGroup(groupId: String)
}
