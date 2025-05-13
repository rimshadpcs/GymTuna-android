package com.rimapps.gymlog.domain.model

data class User(
    val uid: String,
    val email: String? = null,
    val displayName: String? = null,
    val isAnonymous: Boolean = false
)