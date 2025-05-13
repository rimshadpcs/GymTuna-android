package com.rimapps.gymlog.presentation.home.previews
import android.content.Intent
import android.content.IntentSender
import com.rimapps.gymlog.domain.model.User
import com.rimapps.gymlog.domain.repository.AuthRepository

class PreviewAuthRepository : AuthRepository {
    override suspend fun signInWithGoogle(intentSender: IntentSender): Result<Unit> = Result.success(Unit)
    override suspend fun signInWithGoogleIntent(intent: Intent): Result<Unit> = Result.success(Unit)
    override suspend fun signOut() {}
    override fun isUserSignedIn(): Boolean = true
    override suspend fun getCurrentUser(): User? = User(
        uid = "preview_user_id",
        email = "preview@example.com",
        displayName = "Preview User",
        isAnonymous = false
    )
}