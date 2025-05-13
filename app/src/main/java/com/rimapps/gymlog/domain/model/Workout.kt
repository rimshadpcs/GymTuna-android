package com.rimapps.gymlog.domain.model

data class Workout(
    val id: String = "",
    val name: String = "",
    val exercises: List<Exercise> = emptyList(),
    val userId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)