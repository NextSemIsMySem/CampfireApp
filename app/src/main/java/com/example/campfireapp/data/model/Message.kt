package com.example.campfireapp.data.model

import com.google.firebase.firestore.DocumentId

/**
 * Message data model for Firestore
 * Represents a chat message in a group
 */
data class Message(
    @DocumentId
    val id: String = "",
    val groupId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isEdited: Boolean = false,
    val editedAt: Long? = null
) {
    // No-argument constructor for Firestore
    constructor() : this("", "", "", "", "", 0L, false, null)
}
