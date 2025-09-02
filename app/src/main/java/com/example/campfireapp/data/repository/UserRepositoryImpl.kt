package com.example.campfireapp.data.repository

import com.example.campfireapp.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase implementation of UserRepository
 * Handles user authentication and profile management
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : UserRepository {

    private val usersCollection = firestore.collection("users")

    override suspend fun registerUser(email: String, password: String, displayName: String): Result<User> {
        return try {
            // Create Firebase Auth user
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("User creation failed")

            // Create user document in Firestore
            val user = User(
                id = firebaseUser.uid,
                email = email,
                displayName = displayName,
                createdAt = System.currentTimeMillis(),
                lastActive = System.currentTimeMillis()
            )

            usersCollection.document(firebaseUser.uid).set(user).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginUser(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Login failed")

            // Get user data from Firestore
            val userDoc = usersCollection.document(firebaseUser.uid).get().await()
            val user = userDoc.toObject(User::class.java) ?: throw Exception("User data not found")

            // Update last active timestamp
            val updatedUser = user.copy(lastActive = System.currentTimeMillis())
            usersCollection.document(firebaseUser.uid).update("lastActive", updatedUser.lastActive).await()

            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logoutUser(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null
        return try {
            val userDoc = usersCollection.document(firebaseUser.uid).get().await()
            userDoc.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            usersCollection.document(user.id).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteUserAccount(): Result<Unit> {
        return try {
            val firebaseUser = auth.currentUser ?: throw Exception("No authenticated user")
            
            // Delete user document from Firestore
            usersCollection.document(firebaseUser.uid).delete().await()
            
            // Delete Firebase Auth user
            firebaseUser.delete().await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUserFlow(): Flow<User?> = callbackFlow {
        var userDocListener: com.google.firebase.firestore.ListenerRegistration? = null
        
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            
            // Remove any existing user document listener
            userDocListener?.remove()
            userDocListener = null
            
            if (firebaseUser != null) {
                // Listen to user document changes
                userDocListener = usersCollection.document(firebaseUser.uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            // Handle error but don't close the flow
                            trySend(null)
                            return@addSnapshotListener
                        }
                        val user = snapshot?.toObject(User::class.java)
                        trySend(user)
                    }
            } else {
                trySend(null)
            }
        }

        auth.addAuthStateListener(authStateListener)

        awaitClose {
            userDocListener?.remove()
            auth.removeAuthStateListener(authStateListener)
        }
    }
}
