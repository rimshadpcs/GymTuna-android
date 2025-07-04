/*  com.rimapps.gymlog.presentation.navigation.Screen  */

package com.rimapps.gymlog.presentation.navigation

sealed class Screen(val route: String) {

    data object Auth          : Screen("auth")

    data object Home          : Screen("home")
    data object ActiveWorkout : Screen("active_workout")
    data object CreateRoutine : Screen("create_routine")
    data object ExerciseSearch: Screen("exercise_search")
    data object Settings      : Screen("settings")

    data object History       : Screen("history")

    fun withRoutineId(routineId: String): String =
        when (this) {
            is ActiveWorkout -> "$route?routineId=$routineId"
            is CreateRoutine -> "$route?routineId=$routineId"
            else             -> route
        }
}
