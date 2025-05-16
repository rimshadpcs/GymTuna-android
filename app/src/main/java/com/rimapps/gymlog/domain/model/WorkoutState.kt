package com.rimapps.gymlog.domain.model


sealed class WorkoutState {
    object Initial : WorkoutState()
    object Loading : WorkoutState()
    object Idle : WorkoutState()
    data class Success(val workouts: List<Workout>) : WorkoutState()
    data class Error(val message: String) : WorkoutState()
}