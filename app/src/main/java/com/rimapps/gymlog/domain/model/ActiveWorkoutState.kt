package com.rimapps.gymlog.domain.model

sealed class ActiveWorkoutState {
    data object Initial : ActiveWorkoutState()
    data object Loading : ActiveWorkoutState()
    data object Success : ActiveWorkoutState()
    data class Error(val message: String) : ActiveWorkoutState()
}