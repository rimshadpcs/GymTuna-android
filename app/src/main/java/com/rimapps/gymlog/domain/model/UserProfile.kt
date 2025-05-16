package com.rimapps.gymlog.domain.model

data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis(),
    val totalWorkouts: Int = 0,
    val totalVolume: Double = 0.0,
    val preferredMuscleGroups: List<String> = emptyList()
)