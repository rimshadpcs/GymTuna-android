package com.rimapps.gymlog.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rimapps.gymlog.data.local.dao.ExerciseDao
import com.rimapps.gymlog.data.repository.AuthRepositoryImpl
import com.rimapps.gymlog.data.repository.WorkoutHistoryRepositoryImpl
import com.rimapps.gymlog.data.repository.WorkoutRepositoryImpl
import com.rimapps.gymlog.domain.repository.AuthRepository
import com.rimapps.gymlog.domain.repository.WorkoutHistoryRepository
import com.rimapps.gymlog.domain.repository.WorkoutRepository
import com.rimapps.gymlog.utils.GoogleSignInHelper
import com.rimapps.gymlog.utils.UserPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        userPreferences: UserPreferences,
        googleSignInHelper: GoogleSignInHelper
    ): AuthRepository {
        return AuthRepositoryImpl(auth, userPreferences, googleSignInHelper)
    }

    @Provides
    @Singleton
    fun provideWorkoutRepository(
        firestore: FirebaseFirestore,
        exerciseDao: ExerciseDao,
        authRepository: AuthRepository
    ): WorkoutRepository {
        return WorkoutRepositoryImpl(firestore, exerciseDao,authRepository)
    }

    @Provides
    @Singleton
    fun provideWorkoutHistoryRepository(
        firestore: FirebaseFirestore
    ): WorkoutHistoryRepository {
        return WorkoutHistoryRepositoryImpl(firestore)
    }
}