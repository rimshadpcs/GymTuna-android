package com.rimapps.gymlog.domain.model

data class CompletedExercise(
    val exerciseId: String = "",
    val name: String = "",
    val notes: String = "",
    val sets: List<CompletedSet> = emptyList()
)
