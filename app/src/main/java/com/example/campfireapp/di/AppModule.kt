package com.example.campfireapp.di

import com.example.campfireapp.data.repository.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for providing Firebase and Repository dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideUserRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): UserRepository = UserRepositoryImpl(auth, firestore)

    @Provides
    @Singleton
    fun provideGroupRepository(
        firestore: FirebaseFirestore
    ): GroupRepository = GroupRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun provideMessageRepository(
        firestore: FirebaseFirestore
    ): MessageRepository = MessageRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun provideSelfDestructRepository(
        firestore: FirebaseFirestore
    ): SelfDestructRepository = SelfDestructRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun provideSelfDestructService(
        selfDestructRepository: SelfDestructRepository,
        groupRepository: GroupRepository
    ): SelfDestructService = SelfDestructServiceImpl(selfDestructRepository, groupRepository)
}
