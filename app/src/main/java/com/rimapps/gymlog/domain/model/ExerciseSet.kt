package com.rimapps.gymlog.domain.model

data class ExerciseSet(
    val setNumber: Int = 0,
    val weight: Double = 0.0,
    val reps: Int = 0,
    val isCompleted: Boolean = false,
    val previousWeight: Double? = null,
    val previousReps: Int? = null
)