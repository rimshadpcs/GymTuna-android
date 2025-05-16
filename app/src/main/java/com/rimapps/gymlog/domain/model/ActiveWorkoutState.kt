package com.rimapps.gymlog.domain.model

sealed class ActiveWorkoutState {
    object Initial : ActiveWorkoutState()
    object Loading : ActiveWorkoutState()
    object Success : ActiveWorkoutState()
    data class Error(val message: String) : ActiveWorkoutState()
}