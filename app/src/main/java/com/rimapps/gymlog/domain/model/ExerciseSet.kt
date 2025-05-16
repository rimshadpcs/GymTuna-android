package com.rimapps.gymlog.domain.model

data class ExerciseSet(
    val setNumber: Int,
    var reps: Int = 0,
    var weight: Double = 0.0,
    var isCompleted: Boolean = false,
    var previousReps: Int? = null,
    var previousWeight: Double? = null
)