package com.example.campfireapp.util

import com.example.campfireapp.data.model.Group
import com.example.campfireapp.data.model.Message
import com.example.campfireapp.data.model.SelfDestructRule
import com.example.campfireapp.data.model.User
import com.example.campfireapp.data.repository.GroupRepository
import com.example.campfireapp.data.repository.MessageRepository
import com.example.campfireapp.data.repository.UserRepository
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class for creating dummy seed data for testing
 * This helps with quick testing and development
 */
@Singleton
class DummyDataSeeder @Inject constructor(
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository,
    private val messageRepository: MessageRepository
) {

    suspend fun seedDummyData() {
        try {
            // Create dummy users
            val user1 = createDummyUser("alice@example.com", "password123", "Alice")
            delay(500) // Small delay to avoid rate limiting
            
            val user2 = createDummyUser("bob@example.com", "password123", "Bob")
            delay(500)
            
            val user3 = createDummyUser("charlie@example.com", "password123", "Charlie")
            delay(500)

            // Create dummy groups with different self-destruct rules
            user1?.let { alice ->
                // Group 1: Message limit
                val group1 = Group(
                    name = "Quick Chat",
                    description = "A group that disappears after 50 messages",
                    createdBy = alice.id,
                    memberIds = listOf(alice.id),
                    selfDestructRule = SelfDestructRule(maxMessages = 50),
                    createdAt = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000), // 2 days ago
                    lastActivity = System.currentTimeMillis() - (1 * 60 * 60 * 1000) // 1 hour ago
                )
                
                groupRepository.createGroup(group1).onSuccess { createdGroup1 ->
                    // Add some messages
                    createDummyMessage(createdGroup1.id, alice.id, alice.displayName, "Hello everyone!")
                    delay(200)
                    createDummyMessage(createdGroup1.id, alice.id, alice.displayName, "This group will disappear after 50 messages")
                }
                
                delay(1000)
                
                // Group 2: Time-based
                val group2 = Group(
                    name = "Daily Standup",
                    description = "24-hour group for daily updates",
                    createdBy = alice.id,
                    memberIds = listOf(alice.id),
                    selfDestructRule = SelfDestructRule(durationMinutes = 24 * 60), // 24 hours
                    createdAt = System.currentTimeMillis() - (12 * 60 * 60 * 1000), // 12 hours ago
                    lastActivity = System.currentTimeMillis() - (30 * 60 * 1000) // 30 minutes ago
                )
                
                groupRepository.createGroup(group2).onSuccess { createdGroup2 ->
                    createDummyMessage(createdGroup2.id, alice.id, alice.displayName, "Good morning team!")
                    delay(200)
                    createDummyMessage(createdGroup2.id, alice.id, alice.displayName, "This group expires in 12 hours")
                }
                
                delay(1000)
                
                // Group 3: Inactivity timeout
                val group3 = Group(
                    name = "Study Group",
                    description = "Disappears after 2 hours of inactivity",
                    createdBy = alice.id,
                    memberIds = listOf(alice.id),
                    selfDestructRule = SelfDestructRule(inactivityTimeoutMinutes = 2 * 60), // 2 hours
                    createdAt = System.currentTimeMillis() - (6 * 60 * 60 * 1000), // 6 hours ago
                    lastActivity = System.currentTimeMillis() - (45 * 60 * 1000) // 45 minutes ago
                )
                
                groupRepository.createGroup(group3).onSuccess { createdGroup3 ->
                    createDummyMessage(createdGroup3.id, alice.id, alice.displayName, "Let's study together!")
                    delay(200)
                    createDummyMessage(createdGroup3.id, alice.id, alice.displayName, "Group disappears after 2 hours of silence")
                }
                
                delay(1000)
                
                // Group 4: No restrictions
                val group4 = Group(
                    name = "Permanent Chat",
                    description = "No self-destruct rules",
                    createdBy = alice.id,
                    memberIds = listOf(alice.id),
                    selfDestructRule = SelfDestructRule(), // No rules
                    createdAt = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000), // 7 days ago
                    lastActivity = System.currentTimeMillis() - (5 * 60 * 1000) // 5 minutes ago
                )
                
                groupRepository.createGroup(group4).onSuccess { createdGroup4 ->
                    createDummyMessage(createdGroup4.id, alice.id, alice.displayName, "This group has no time limits")
                    delay(200)
                    createDummyMessage(createdGroup4.id, alice.id, alice.displayName, "We can chat here indefinitely!")
                }
            }
            
        } catch (e: Exception) {
            println("Error seeding dummy data: ${e.message}")
        }
    }

    private suspend fun createDummyUser(email: String, password: String, displayName: String): User? {
        return try {
            userRepository.registerUser(email, password, displayName).getOrNull()
        } catch (e: Exception) {
            // User might already exist, try to login instead
            try {
                userRepository.loginUser(email, password).getOrNull()
            } catch (e2: Exception) {
                null
            }
        }
    }

    private suspend fun createDummyMessage(groupId: String, senderId: String, senderName: String, content: String) {
        val message = Message(
            groupId = groupId,
            senderId = senderId,
            senderName = senderName,
            content = content,
            timestamp = System.currentTimeMillis() - (Math.random() * 24 * 60 * 60 * 1000).toLong() // Random time in last 24 hours
        )
        messageRepository.sendMessage(message)
    }
}

/**
 * Quick demo credentials for testing
 */
object DemoCredentials {
    const val DEMO_EMAIL = "demo@campfire.com"
    const val DEMO_PASSWORD = "demo123"
    const val DEMO_NAME = "Demo User"
}
