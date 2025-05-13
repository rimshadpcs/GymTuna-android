package com.rimapps.gymlog.domain.model

data class Exercise(
    val name: String,
    val equipment: String = "",
    val muscleGroup: String = "",
    val defaultReps: Int = 15,
    val defaultSets: Int = 3,
    val isBodyweight: Boolean = false,
    val usesWeight: Boolean = true
)