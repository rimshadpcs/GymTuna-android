package com.rimapps.gymlog.domain.model

// In the domain/model/AuthState.kt
sealed class AuthState {
    data object Initial : AuthState()
    data object Loading : AuthState()
    data object Success : AuthState()
    data class Error(val message: String?) : AuthState() {
        override fun toString() = "Error($message)"
    }
}