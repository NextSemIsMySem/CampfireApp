package com.example.campfireapp.data.repository

import com.example.campfireapp.data.model.Message
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase implementation of MessageRepository
 * Handles message CRUD operations with Firestore
 */
@Singleton
class MessageRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : MessageRepository {

    private val messagesCollection = firestore.collection("messages")

    override suspend fun sendMessage(message: Message): Result<Message> {
        return try {
            val docRef = messagesCollection.add(message).await()
            val sentMessage = message.copy(id = docRef.id)
            messagesCollection.document(docRef.id).set(sentMessage).await()
            Result.success(sentMessage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateMessage(message: Message): Result<Unit> {
        return try {
            val updatedMessage = message.copy(
                isEdited = true,
                editedAt = System.currentTimeMillis()
            )
            messagesCollection.document(message.id).set(updatedMessage).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteMessage(messageId: String): Result<Unit> {
        return try {
            messagesCollection.document(messageId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getGroupMessages(groupId: String): Flow<List<Message>> = callbackFlow {
        val listener = messagesCollection
            .whereEqualTo("groupId", groupId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Message::class.java)
                } ?: emptyList()
                trySend(messages)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun getMessageCount(groupId: String): Int {
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
}
