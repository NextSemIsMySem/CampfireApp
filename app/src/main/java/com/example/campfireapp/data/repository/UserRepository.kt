package com.example.campfireapp.data.repository

import com.example.campfireapp.data.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for User CRUD operations
 */
interface UserRepository {
    suspend fun registerUser(email: String, password: String, displayName: String): Result<User>
    suspend fun loginUser(email: String, password: String): Result<User>
    suspend fun logoutUser(): Result<Unit>
    suspend fun getCurrentUser(): User?
    suspend fun updateUserProfile(user: User): Result<Unit>
    suspend fun deleteUserAccount(): Result<Unit>
    fun getCurrentUserFlow(): Flow<User?>
}
