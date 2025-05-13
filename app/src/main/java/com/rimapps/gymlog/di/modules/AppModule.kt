package com.rimapps.gymlog.di.modules

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.rimapps.gymlog.data.local.dao.ExerciseDao
import com.rimapps.gymlog.data.repository.AuthRepositoryImpl
import com.rimapps.gymlog.data.repository.WorkoutRepositoryImpl
import com.rimapps.gymlog.domain.repository.AuthRepository
import com.rimapps.gymlog.domain.repository.WorkoutRepository
import com.rimapps.gymlog.utils.GoogleSignInHelper
import com.rimapps.gymlog.utils.UserPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return Firebase.auth
    }

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return Firebase.firestore
    }

    @Provides
    @Singleton
    fun provideGoogleSignInHelper(context: Context): GoogleSignInHelper {
        return GoogleSignInHelper(context)
    }

    @Provides
    @Singleton
    fun provideUserPreferences(context: Context): UserPreferences {
        return UserPreferences(context)
    }

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
        exerciseDao: ExerciseDao
    ): WorkoutRepository {
        return WorkoutRepositoryImpl(firestore, exerciseDao)
    }

}