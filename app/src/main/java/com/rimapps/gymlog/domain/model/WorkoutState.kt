package com.rimapps.gymlog.domain.model

sealed class WorkoutState {
    object Idle : WorkoutState()
    object Loading : WorkoutState()
    data class Success(val workouts: List<Workout>) : WorkoutState()
    data class Error(val message: String) : WorkoutState()
}