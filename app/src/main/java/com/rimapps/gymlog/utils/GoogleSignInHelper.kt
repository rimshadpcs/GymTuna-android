package com.rimapps.gymlog.utils

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject



class GoogleSignInHelper @Inject constructor(
    private val context: Context
) {
    private val webClientId = "856612400371-d11ud795jtpq5nj4h6o3sbt3vr5luqd0.apps.googleusercontent.com"
    private val oneTapClient: SignInClient = Identity.getSignInClient(context)

    init {
        Log.d(TAG, "Initializing GoogleSignInHelper with client ID: $webClientId")
    }

    private val signInRequest = BeginSignInRequest.builder()
        .setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                .setServerClientId(webClientId)
                .setFilterByAuthorizedAccounts(false)
                .build()
        )
        .setAutoSelectEnabled(false)
        .build()

    suspend fun startSignIn(): IntentSender? {
        return try {
            Log.d(TAG, "Attempting to start sign-in")
            val result = oneTapClient.beginSignIn(signInRequest).await()
            Log.d(TAG, "Got sign-in result, returning intent sender")
            result.pendingIntent.intentSender
        } catch (e: Exception) {
            Log.e(TAG, "Error starting sign-in", e)
            null
        }
    }

    suspend fun signInWithIntent(intent: Intent): Result<AuthCredential> {
        return try {
            Log.d(TAG, "Processing sign-in intent")
            val credential = oneTapClient.getSignInCredentialFromIntent(intent)
            val idToken = credential.googleIdToken

            if (idToken == null) {
                Log.e(TAG, "No ID token in credential")
                Result.failure(Exception("Google ID token is null"))
            } else {
                Log.d(TAG, "Got ID token, creating auth credential")
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                Result.success(authCredential)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing sign-in credential", e)
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        try {
            Log.d(TAG, "Signing out of Google")
            oneTapClient.signOut().await()
            Log.d(TAG, "Successfully signed out of Google")
        } catch (e: Exception) {
            Log.e(TAG, "Error signing out of Google", e)
            throw e
        }
    }

    companion object {
        private const val TAG = "GoogleSignInHelper"
    }
}