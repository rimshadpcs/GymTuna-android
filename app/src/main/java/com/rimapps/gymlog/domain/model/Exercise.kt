package com.rimapps.gymlog.domain.model

data class Exercise(
    val id: String = "",
    val name: String,
    val muscleGroup: String = "",
    val equipment: String = "",
    val defaultReps: Int = 15,
    val defaultSets: Int = 3,
    val isBodyweight: Boolean = false,
    val usesWeight: Boolean = true,
    val description: String = ""
) {
    // Optional: Add convenience functions if needed
    fun isValid(): Boolean = name.isNotBlank()
}