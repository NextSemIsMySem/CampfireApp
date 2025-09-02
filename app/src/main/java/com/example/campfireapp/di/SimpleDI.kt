package com.example.campfireapp.di

import com.example.campfireapp.data.repository.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Simple manual dependency injection for easier setup
 * Use this instead of Hilt if you want to avoid complex configuration
 */
object SimpleDI {
    
    // Firebase instances
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    
    // Repositories
    val userRepository: UserRepository by lazy { 
        UserRepositoryImpl(auth, firestore) 
    }
    
    val groupRepository: GroupRepository by lazy { 
        GroupRepositoryImpl(firestore) 
    }
    
    val messageRepository: MessageRepository by lazy { 
        MessageRepositoryImpl(firestore) 
    }
    
    val selfDestructRepository: SelfDestructRepository by lazy { 
        SelfDestructRepositoryImpl(firestore) 
    }
    
    val selfDestructService: SelfDestructService by lazy { 
        SelfDestructServiceImpl(selfDestructRepository, groupRepository) 
    }
}

/**
 * Extension function to create ViewModels with manual DI
 */
fun <T> manualViewModel(factory: () -> T): T = factory()
