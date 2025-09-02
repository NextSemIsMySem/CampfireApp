package com.example.campfireapp.data.repository

import com.example.campfireapp.data.model.Group
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase implementation of SelfDestructRepository
 * Handles automatic group destruction based on configured rules
 */
@Singleton
class SelfDestructRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : SelfDestructRepository {

    private val groupsCollection = firestore.collection("groups")
    private val messagesCollection = firestore.collection("messages")

    override suspend fun checkAndDestroyGroups(): Result<List<String>> {
        return try {
            val destroyedGroups = mutableListOf<String>()
            
            // Get all active groups
            val groupsSnapshot = groupsCollection
                .whereEqualTo("isActive", true)
                .get()
                .await()

            for (groupDoc in groupsSnapshot.documents) {
                val group = groupDoc.toObject(Group::class.java) ?: continue
                
                // Get message count for this group
                val messageCount = getMessageCount(group.id)
                
                if (shouldDestroyGroup(group, messageCount)) {
                    // Destroy the group
                    destroyGroup(group.id)
                    destroyedGroups.add(group.id)
                }
            }
            
            Result.success(destroyedGroups)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun shouldDestroyGroup(group: Group, messageCount: Int): Boolean {
        val rule = group.selfDestructRule
        val currentTime = System.currentTimeMillis()
        
        // Check maximum messages rule
        rule.maxMessages?.let { maxMessages ->
            if (messageCount >= maxMessages) {
                return true
            }
        }
        
        // Check duration rule
        rule.durationMinutes?.let { durationMinutes ->
            val maxTime = group.createdAt + (durationMinutes * 60 * 1000)
            if (currentTime >= maxTime) {
                return true
            }
        }
        
        // Check inactivity timeout rule
        rule.inactivityTimeoutMinutes?.let { inactivityMinutes ->
            val maxInactiveTime = group.lastActivity + (inactivityMinutes * 60 * 1000)
            if (currentTime >= maxInactiveTime) {
                return true
            }
        }
        
        return false
    }

    private suspend fun getMessageCount(groupId: String): Int {
        return try {
            val snapshot = messagesCollection
                .whereEqualTo("groupId", groupId)
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            0
        }
    }

    private suspend fun destroyGroup(groupId: String) {
        try {
            // Mark group as inactive
            groupsCollection.document(groupId)
                .update("isActive", false)
                .await()

            // Delete all messages in the group
            val messagesSnapshot = messagesCollection
                .whereEqualTo("groupId", groupId)
                .get()
                .await()

            val batch = firestore.batch()
            messagesSnapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()
        } catch (e: Exception) {
            // Log error but don't throw - we want to continue with other groups
            println("Error destroying group $groupId: ${e.message}")
        }
    }
}

/**
 * Service implementation for self-destruct functionality
 */
@Singleton
class SelfDestructServiceImpl @Inject constructor(
    private val selfDestructRepository: SelfDestructRepository,
    private val groupRepository: GroupRepository
) : SelfDestructService {

    override suspend fun checkAllGroups() {
        selfDestructRepository.checkAndDestroyGroups()
    }

    override suspend fun checkGroup(groupId: String) {
        val group = groupRepository.getGroup(groupId) ?: return
        
        // Get current message count
        val messageCount = try {
            val firestore = FirebaseFirestore.getInstance()
            val snapshot = firestore.collection("messages")
                .whereEqualTo("groupId", groupId)
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            0
        }
        
        if (selfDestructRepository.shouldDestroyGroup(group, messageCount)) {
            groupRepository.deleteGroup(groupId)
        }
    }
}
