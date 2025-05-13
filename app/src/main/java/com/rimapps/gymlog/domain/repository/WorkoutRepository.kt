package com.rimapps.gymlog.domain.repository

import com.rimapps.gymlog.domain.model.Exercise
import com.rimapps.gymlog.domain.model.Workout
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    suspend fun getWorkouts(userId: String): Flow<List<Workout>>
    suspend fun createWorkout(workout: Workout)
    suspend fun updateWorkout(workout: Workout)
    suspend fun deleteWorkout(workoutId: String)
    fun searchExercises(query: String): Flow<List<Exercise>>
    suspend fun getExercises(): Flow<List<Exercise>>
}