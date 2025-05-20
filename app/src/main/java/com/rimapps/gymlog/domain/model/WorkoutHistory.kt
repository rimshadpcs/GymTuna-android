package com.rimapps.gymlog.domain.model

import java.util.UUID

data class WorkoutHistory(
    val id: String = "",
    val name: String = "",
    val startTime: Long = 0,
    val endTime: Long = 0,
    val exercises: List<CompletedExercise> = emptyList(),
    val totalVolume: Double = 0.0,
    val totalSets: Int = 0,
    val colorHex: String = "",
    val routineId: String? = null,
    val userId: String = "",
    val exerciseIds: List<String> = emptyList()
)
