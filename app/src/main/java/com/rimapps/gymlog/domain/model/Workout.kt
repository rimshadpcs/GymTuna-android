package com.rimapps.gymlog.domain.model

data class Workout(
    val id: String,
    val name: String,
    val userId: String,
    val exercises: List<Exercise>,
    val createdAt: Long,
    val colorHex: String? = null,  // Changed from colorId to colorHex
    val lastPerformed: Long? = null
)