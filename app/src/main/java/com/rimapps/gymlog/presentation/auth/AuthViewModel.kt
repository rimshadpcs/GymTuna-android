package com.rimapps.gymlog.presentation.auth

import android.content.ContentValues.TAG
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
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val googleSignInHelper: GoogleSignInHelper,
    private val userPreferences: UserPreferences
) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState = _authState.asStateFlow()

    init {
        checkAuthState()
    }

    private val _navigationEvent = Channel<Unit>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private fun checkAuthState() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Checking initial auth state")

                val isFirebaseSignedIn = authRepository.isUserSignedIn()
                Log.d(TAG, "Firebase signed in: $isFirebaseSignedIn")

                val isSignedInPref = userPreferences.isUserSignedIn.first()
                Log.d(TAG, "Preferences signed in: $isSignedInPref")

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

    suspend fun startGoogleSignIn(): IntentSender? {
        return try {
            Log.d(TAG, "Starting Google sign in")
            val intentSender = googleSignInHelper.startSignIn()
            // Note: We don't call authRepository.signInWithGoogle here anymore
            intentSender
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start Google sign in", e)
            null
        }
    }

    fun signInWithGoogleIntent(intent: Intent) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Processing sign in intent")
                _authState.value = AuthState.Loading

                authRepository.signInWithGoogleIntent(intent)
                    .onSuccess {
                        Log.d(TAG, "Sign in successful, triggering navigation")
                        _authState.value = AuthState.Success
                        _navigationEvent.send(Unit)
                    }
                    .onFailure { e ->
                        Log.e(TAG, "Sign in failed", e)
                        _authState.value = AuthState.Error(e.message)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Sign in exception", e)
                _authState.value = AuthState.Error(e.message)
            }
        }
    }


    companion object {
        private const val TAG = "AuthViewModel"
    }
}