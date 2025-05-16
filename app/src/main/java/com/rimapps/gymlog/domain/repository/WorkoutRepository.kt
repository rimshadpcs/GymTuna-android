package com.rimapps.gymlog.domain.repository

import com.rimapps.gymlog.domain.model.Exercise
import com.rimapps.gymlog.domain.model.Workout
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    suspend fun createWorkout(workout: Workout)
    suspend fun getWorkouts(userId: String): Flow<List<Workout>>
    suspend fun getWorkoutById(workoutId: String): Workout?
    suspend fun deleteWorkout(workoutId: String)
    suspend fun updateWorkout(workout: Workout)
    fun searchExercises(query: String): Flow<List<Exercise>>
    suspend fun getExercises(): Flow<List<Exercise>>

}