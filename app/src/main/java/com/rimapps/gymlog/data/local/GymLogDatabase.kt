package com.rimapps.gymlog.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rimapps.gymlog.data.local.dao.ExerciseDao
import com.rimapps.gymlog.data.local.entity.ExerciseEntity

@Database(
    entities = [ExerciseEntity::class],
    version = 2,  // Increased version number
    exportSchema = false
)
abstract class GymLogDatabase : RoomDatabase() {
    abstract val exerciseDao: ExerciseDao
}