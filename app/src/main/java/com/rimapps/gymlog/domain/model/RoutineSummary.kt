package com.rimapps.gymlog.domain.model

data class RoutineSummary(
    val routineId     : String?,   // null for quick workouts
    val name          : String,
    val colorHex      : String,
    val timesDone     : Int,       // how many workouts this month
    val lastPerformed : Long       // latest endTime in ms
)
