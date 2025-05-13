package com.rimapps.gymlog.di

import android.content.Context
import androidx.room.Room
import com.rimapps.gymlog.data.local.GymLogDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideGymLogDatabase(
        @ApplicationContext context: Context
    ): GymLogDatabase {
        return Room.databaseBuilder(
            context,
            GymLogDatabase::class.java,
            "gymlog.db"
        )
            .fallbackToDestructiveMigration() // This will recreate tables if schema changed
            .build()
    }

    @Provides
    @Singleton
    fun provideExerciseDao(database: GymLogDatabase) = database.exerciseDao
}