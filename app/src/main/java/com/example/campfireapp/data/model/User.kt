package com.example.campfireapp.data.model

import com.google.firebase.firestore.DocumentId

/**
 * User data model for Firestore
 * Represents a user in the Campfire app
 */
data class User(
    @DocumentId
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val profileImageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastActive: Long = System.currentTimeMillis()
) {
    // No-argument constructor for Firestore
    constructor() : this("", "", "", "", 0L, 0L)
}
