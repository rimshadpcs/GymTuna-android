package com.rimapps.gymlog.domain.model

data class Workout(
    val id: String,
    val name: String,
    val userId: String,
    val exercises: List<Exercise> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)