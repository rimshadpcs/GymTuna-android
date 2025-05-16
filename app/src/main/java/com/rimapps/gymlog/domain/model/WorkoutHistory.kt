package com.rimapps.gymlog.domain.model

import java.util.UUID

data class WorkoutHistory(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val userId: String,
    val startTime: Long,
    val endTime: Long,
    val exercises: List<CompletedExercise>,
    val totalVolume: Double,
    val totalSets: Int
)