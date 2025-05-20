package com.rimapps.gymlog.utils

import com.rimapps.gymlog.utils.RoutineColors.colorOptions
object RoutineColors {
    val colorOptions = listOf(
        ColorOption("#6B9CD6", "Blue"),
        ColorOption("#D67676", "Red"),
        ColorOption("#76D6A8", "Green"),
        ColorOption("#B576D6", "Purple"),
        ColorOption("#D6CF76", "Yellow"),
        ColorOption("#9976D6", "Violet"),
        ColorOption("#D676B5", "Pink")
    )

    fun byIndex(index: Int): String =
        colorOptions[index % colorOptions.size].hex
    }


data class ColorOption(
    val hex: String,
    val name: String
)