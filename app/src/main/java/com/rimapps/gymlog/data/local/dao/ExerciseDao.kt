package com.rimapps.gymlog.data.local.dao

import androidx.room.*
import com.rimapps.gymlog.data.local.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow
@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE LOWER(name) LIKE '%' || LOWER(:query) || '%' ORDER BY name ASC")
    fun searchExercises(query: String): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE name = :name LIMIT 1")
    suspend fun getExerciseByName(name: String): ExerciseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)

    @Query("DELETE FROM exercises")
    suspend fun clearExercises()

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun getExerciseCount(): Int

    // Debug queries
    @Query("SELECT * FROM exercises LIMIT 1")
    suspend fun getFirstExercise(): ExerciseEntity?

    @Query("SELECT name FROM exercises")
    suspend fun getAllExerciseNames(): List<String>
}