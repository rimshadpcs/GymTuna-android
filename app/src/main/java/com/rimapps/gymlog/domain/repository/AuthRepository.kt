package com.rimapps.gymlog.domain.repository

import android.content.Intent
import android.content.IntentSender
import com.rimapps.gymlog.domain.model.User

interface AuthRepository {
    suspend fun signInWithGoogle(intentSender: IntentSender): Result<Unit>
    suspend fun signInWithGoogleIntent(intent: Intent): Result<Unit>
    suspend fun signOut()
    fun isUserSignedIn(): Boolean
    suspend fun getCurrentUser(): User?
}