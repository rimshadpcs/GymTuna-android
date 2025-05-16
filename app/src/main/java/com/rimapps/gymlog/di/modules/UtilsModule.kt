package com.rimapps.gymlog.di

import android.content.Context
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
object UtilsModule {

    @Provides
    @Singleton
    fun provideGoogleSignInHelper(@ApplicationContext context: Context): GoogleSignInHelper {
        return GoogleSignInHelper(context)
    }

    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext context: Context): UserPreferences {
        return UserPreferences(context)
    }
}