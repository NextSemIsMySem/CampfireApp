package com.example.campfireapp.data.model

import com.google.firebase.firestore.DocumentId

/**
 * Self-destruct rule configuration
 */
data class SelfDestructRule(
    val maxMessages: Int? = null, // null = no limit
    val durationMinutes: Long? = null, // null = no time limit
    val inactivityTimeoutMinutes: Long? = null // null = no inactivity timeout
) {
    constructor() : this(null, null, null)
}

/**
 * Group data model for Firestore
 * Represents a chat group with self-destruct capabilities
 */
data class Group(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val createdBy: String = "", // User ID
    val memberIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val lastActivity: Long = System.currentTimeMillis(),
    val messageCount: Int = 0,
    val selfDestructRule: SelfDestructRule = SelfDestructRule(),
    val isActive: Boolean = true
) {
    // No-argument constructor for Firestore
    constructor() : this("", "", "", "", emptyList(), 0L, 0L, 0, SelfDestructRule(), true)
}
