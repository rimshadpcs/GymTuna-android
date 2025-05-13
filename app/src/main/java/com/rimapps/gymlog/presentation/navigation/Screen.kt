package com.rimapps.gymlog.presentation.navigation

sealed class Screen(val route: String) {
    data object Auth : Screen("auth")
    data object Home : Screen("home")
    data object ExerciseSearch : Screen("exercise_search")
    data object CreateRoutine : Screen("create_routine")
    data object ActiveWorkout : Screen("active_workout")
}