package com.rimapps.gymlog.presentation.auth

import android.content.Intent
import android.content.IntentSender
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rimapps.gymlog.domain.model.AuthState
import com.rimapps.gymlog.domain.repository.AuthRepository
import com.rimapps.gymlog.utils.GoogleSignInHelper
import com.rimapps.gymlog.utils.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val googleSignInHelper: GoogleSignInHelper,
    private val userPreferences: UserPreferences
) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState = _authState.asStateFlow()

    private val _navigationEvent = Channel<Unit>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    init {
        checkAuthState()
    }

    suspend fun startGoogleSignIn(): IntentSender? {
        return try {
            Log.d(TAG, "Starting Google sign in")
            googleSignInHelper.startSignIn()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start Google sign in", e)
            null
        }
    }

    fun signInWithGoogleIntent(intent: Intent) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                Log.d(TAG, "Processing sign in intent")

                authRepository.signInWithGoogleIntent(intent)
                    .onSuccess {
                        Log.d(TAG, "Sign in successful")
                        _authState.value = AuthState.Success
                        _navigationEvent.send(Unit)
                    }
                    .onFailure { e ->
                        Log.e(TAG, "Sign in failed", e)
                        _authState.value = AuthState.Error(e.message ?: "Sign in failed")
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error during sign in", e)
                _authState.value = AuthState.Error(e.message ?: "Sign in failed")
            }
        }
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Checking initial auth state")
                val isFirebaseSignedIn = authRepository.isUserSignedIn()
                val isSignedInPref = userPreferences.isUserSignedIn.first()

                if (isFirebaseSignedIn && isSignedInPref) {
                    Log.d(TAG, "User is authenticated, navigating to home")
                    _authState.value = AuthState.Success
                    _navigationEvent.send(Unit)
                } else {
                    Log.d(TAG, "User is not authenticated, showing sign in")
                    if (isFirebaseSignedIn || isSignedInPref) {
                        signOut()
                    }
                    _authState.value = AuthState.Initial
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking auth state", e)
                _authState.value = AuthState.Error(e.message)
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting sign out")
                authRepository.signOut()
                userPreferences.clearPreferences()
                _authState.value = AuthState.Initial
                Log.d(TAG, "Sign out completed")
            } catch (e: Exception) {
                Log.e(TAG, "Error during sign out", e)
                _authState.value = AuthState.Error(e.message)
            }
        }
    }

    companion object {
        private const val TAG = "AuthViewModel"
    }
}