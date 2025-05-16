package com.rimapps.gymlog.domain.repository

import com.rimapps.gymlog.domain.model.WorkoutHistory
import kotlinx.coroutines.flow.Flow
interface WorkoutHistoryRepository {
    suspend fun saveWorkoutHistory(workoutHistory: WorkoutHistory)
    suspend fun getWorkoutHistory(userId: String): Flow<List<WorkoutHistory>>
}