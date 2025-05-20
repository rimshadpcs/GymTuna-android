package com.rimapps.gymlog.presentation.workoutScreen
// WorkoutScreenRoute.kt

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.rimapps.gymlog.presentation.navigation.Screen
import com.rimapps.gymlog.presentation.workoutScreen.WorkoutScreen
import com.rimapps.gymlog.presentation.workoutScreen.WorkoutViewModel

@Composable
fun WorkoutScreenRoute(
    routineId: String? = null,
    navController: NavController
) {
    val viewModel: WorkoutViewModel = hiltViewModel()
    val routineName by viewModel.routineName.collectAsStateWithLifecycle()

    WorkoutScreen(
        routineId = routineId,
        routineName = routineName,
        onBack = { navController.popBackStack() },
        onFinish = {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
            }
        },
        onAddExercise = {
            navController.navigate(Screen.ExerciseSearch.route)
        }
    )
}