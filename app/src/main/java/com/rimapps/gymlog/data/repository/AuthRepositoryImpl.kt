package com.rimapps.gymlog.data.repository

import android.content.Intent
import android.content.IntentSender
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.rimapps.gymlog.domain.model.User
import com.rimapps.gymlog.domain.repository.AuthRepository
import com.rimapps.gymlog.utils.GoogleSignInHelper
import com.rimapps.gymlog.utils.UserPreferences
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val userPreferences: UserPreferences,
    private val googleSignInHelper: GoogleSignInHelper
) : AuthRepository {
    override suspend fun signInWithGoogle(intentSender: IntentSender): Result<Unit> {
        // This method shouldn't start a new sign-in flow
        // It should just return success since the intentSender will be used by the Activity
        return Result.success(Unit)
    }

    override suspend fun signInWithGoogleIntent(intent: Intent): Result<Unit> {
        return try {
            Log.d(TAG, "Starting Google sign in process in repository")
            // Get credential from Google Sign In
            googleSignInHelper.signInWithIntent(intent)
                .onSuccess { credential ->
                    Log.d(TAG, "Got Google credential, signing in with Firebase")
                    // Wait for Firebase sign in to complete
                    auth.signInWithCredential(credential).await()
                    Log.d(TAG, "Firebase sign in completed")
                    // Verify Firebase auth state
                    if (auth.currentUser != null) {
                        Log.d(TAG, "Firebase user verified: ${auth.currentUser?.email}")
                        userPreferences.setUserSignedIn(true)
                    } else {
                        throw Exception("Firebase sign in failed to create user")
                    }
                }
                .onFailure { e ->
                    Log.e(TAG, "Failed to get Google credential", e)
                    throw e
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Sign in failed in repository", e)
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        try {
            Log.d(TAG, "Starting sign out process")
            // Sign out from Google first
            googleSignInHelper.signOut()
            // Then clear Firebase auth
            auth.signOut()
            Log.d(TAG, "Sign out completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error during sign out", e)
            throw e
        }
    }

    override fun isUserSignedIn(): Boolean {
        val isSignedIn = auth.currentUser != null
        Log.d(TAG, "Checking Firebase auth state: $isSignedIn")
        return isSignedIn
    }

    override suspend fun getCurrentUser(): User? {
        return auth.currentUser?.let { firebaseUser ->
            User(
                uid = firebaseUser.uid,
                email = firebaseUser.email,
                displayName = firebaseUser.displayName,
                isAnonymous = firebaseUser.isAnonymous
            )
        }
    }

    companion object {
        private const val TAG = "AuthRepository"
    }
}