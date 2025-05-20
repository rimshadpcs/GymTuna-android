package com.rimapps.gymlog.domain.model

import java.util.UUID

data class ExerciseSet(
    val id: String = UUID.randomUUID().toString(),
    val setNumber: Int,
    var reps: Int = 0,
    var weight: Double = 0.0,
    var isCompleted: Boolean = false,
    var previousReps: Int? = null,
    var previousWeight: Double? = null,
    val bestReps: Int? = null,
    val bestWeight: Double? = null
) {
    // Helper functions for UI display formatting
    fun hasPrevious(): Boolean {
        return previousWeight != null && previousReps != null
    }
    
    fun getPreviousDisplay(): String {
        return if (previousWeight != null && previousReps != null) {
            "${previousWeight}×${previousReps}"
        } else "-"
    }
    
    fun hasBest(): Boolean {
        return bestWeight != null && bestReps != null
    }
    
    fun getBestDisplay(): String {
        return if (bestWeight != null && bestReps != null) {
            "${bestWeight}×${bestReps}"
        } else "-"
    }
}