package com.example.campfireapp.data.repository

import com.example.campfireapp.data.model.Message
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Message CRUD operations
 */
interface MessageRepository {
    suspend fun sendMessage(message: Message): Result<Message>
    suspend fun updateMessage(message: Message): Result<Unit>
    suspend fun deleteMessage(messageId: String): Result<Unit>
    fun getGroupMessages(groupId: String): Flow<List<Message>>
    suspend fun getMessageCount(groupId: String): Int
}
