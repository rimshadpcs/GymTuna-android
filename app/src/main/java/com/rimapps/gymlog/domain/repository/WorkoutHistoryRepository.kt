package com.rimapps.gymlog.domain.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.rimapps.gymlog.domain.model.WorkoutHistory
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId

interface WorkoutHistoryRepository {
    suspend fun saveWorkoutHistory(workoutHistory: WorkoutHistory)
    suspend fun getWorkoutHistory(userId: String): Flow<List<WorkoutHistory>>
    @RequiresApi(Build.VERSION_CODES.O)
    fun getMonthlyHistory(
        userId: String,
        monthStart: LocalDate,
        zone: ZoneId = ZoneId.systemDefault()
    ): Flow<List<WorkoutHistory>>

}