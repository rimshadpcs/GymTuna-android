package com.rimapps.gymlog.domain.model


data class CompletedSet(
    val setNumber : Int = 0,
    val weight    : Double = 0.0,
    val reps      : Int = 0,
    val bestWeight: Double? = null,
    val bestReps  : Int? = null
)