package com.rimapps.gymlog.utils

import com.google.firebase.auth.FirebaseUser
import com.rimapps.gymlog.domain.model.UserProfile

fun FirebaseUser.toUserProfile(): UserProfile {
    return UserProfile(
        uid = this.uid,
        displayName = this.displayName ?: "",
        email = this.email ?: "",
        lastLoginAt = System.currentTimeMillis()
    )
}