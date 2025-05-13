package com.rimapps.gymlog.utils

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class GoogleSignInHelper @Inject constructor(
    private val context: Context
) {
    private val webClientId = "856612400371-d11ud795jtpq5nj4h6o3sbt3vr5luqd0.apps.googleusercontent.com"

    init {
        Log.d(TAG, "Initializing with web client ID: $webClientId")
    }

    private val oneTapClient: SignInClient = Identity.getSignInClient(context)

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
            Log.d(TAG, "Starting sign-in process")
            val result = oneTapClient.beginSignIn(signInRequest).await()
            Log.d(TAG, "Sign-in result: $result")
            result.pendingIntent.intentSender
        } catch (e: Exception) {
            Log.e(TAG, "Error starting sign-in", e)
            when (e) {
                is ApiException -> Log.e(TAG, "ApiException status code: ${e.statusCode}")
                else -> Log.e(TAG, "Unknown exception type: ${e.javaClass.simpleName}")
            }
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
            oneTapClient.signOut().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error signing out", e)
            throw e
        }
    }

    companion object {
        private const val TAG = "GoogleSignInHelper"
    }
}