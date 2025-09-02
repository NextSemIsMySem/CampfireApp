package com.example.campfireapp.data.repository

import com.example.campfireapp.data.model.Group
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase implementation of GroupRepository
 * Handles group CRUD operations with Firestore
 */
@Singleton
class GroupRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : GroupRepository {

    private val groupsCollection = firestore.collection("groups")

    override suspend fun createGroup(group: Group): Result<Group> {
        return try {
            val docRef = groupsCollection.add(group).await()
            val createdGroup = group.copy(id = docRef.id)
            groupsCollection.document(docRef.id).set(createdGroup).await()
            Result.success(createdGroup)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateGroup(group: Group): Result<Unit> {
        return try {
            groupsCollection.document(group.id).set(group).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteGroup(groupId: String): Result<Unit> {
        return try {
            // Delete the group document
            groupsCollection.document(groupId).delete().await()
            
            // Also delete all messages in this group
            val messagesQuery = firestore.collection("messages")
                .whereEqualTo("groupId", groupId)
                .get()
                .await()
            
            val batch = firestore.batch()
            messagesQuery.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getGroup(groupId: String): Group? {
        return try {
            val doc = groupsCollection.document(groupId).get().await()
            doc.toObject(Group::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getUserGroups(userId: String): Flow<List<Group>> = callbackFlow {
        val listener = groupsCollection
            .whereArrayContains("memberIds", userId)
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, _ ->
                val groups = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Group::class.java)
                } ?: emptyList()
                trySend(groups)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun joinGroup(groupId: String, userId: String): Result<Unit> {
        return try {
            groupsCollection.document(groupId)
                .update("memberIds", FieldValue.arrayUnion(userId))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun leaveGroup(groupId: String, userId: String): Result<Unit> {
        return try {
            groupsCollection.document(groupId)
                .update("memberIds", FieldValue.arrayRemove(userId))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun incrementMessageCount(groupId: String): Result<Unit> {
        return try {
            groupsCollection.document(groupId)
                .update("messageCount", FieldValue.increment(1))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateLastActivity(groupId: String): Result<Unit> {
        return try {
            groupsCollection.document(groupId)
                .update("lastActivity", System.currentTimeMillis())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
