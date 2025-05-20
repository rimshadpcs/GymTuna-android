package com.rimapps.gymlog.domain.model

import java.time.LocalDate

data class MonthlyCalendarDay(
    val date     : LocalDate,
    val colorHex : String?
)