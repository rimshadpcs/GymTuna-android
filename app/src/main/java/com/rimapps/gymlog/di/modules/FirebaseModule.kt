package com.rimapps.gymlog.di

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.rimapps.gymlog.data.repository.FirestoreRepository
import com.rimapps.gymlog.domain.repository.WorkoutHistoryRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FirebaseModule {
    // Remove the WorkoutHistoryRepository binding
    /*@Binds
    @Singleton
    abstract fun bindWorkoutHistoryRepository(
        firestoreRepository: FirestoreRepository
    ): WorkoutHistoryRepository*/

    companion object {
        @Provides
        @Singleton
        fun provideFirestore(): FirebaseFirestore {
            return Firebase.firestore
        }

        @Provides
        @Singleton
        fun provideFirebaseAuth(): FirebaseAuth {
            return Firebase.auth
        }

        @Provides
        @Singleton
        fun provideFirestoreRepository(
            firestore: FirebaseFirestore,
            auth: FirebaseAuth
        ): FirestoreRepository {
            return FirestoreRepository(firestore, auth)
        }
    }
}