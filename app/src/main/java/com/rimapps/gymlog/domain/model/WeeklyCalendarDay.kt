package com.rimapps.gymlog.domain.model

import java.time.LocalDate

data class WeeklyCalendarDay(
    val date: LocalDate,
    val routineId: String?,
    val isCompleted: Boolean,
    val colorHex: String?
)