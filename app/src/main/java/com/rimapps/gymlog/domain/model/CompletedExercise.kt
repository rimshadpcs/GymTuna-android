package com.rimapps.gymlog.domain.model

data class CompletedExercise(
    val exerciseId: String,
    val name: String,
    val sets: List<CompletedSet>,
    val notes: String = ""
)
