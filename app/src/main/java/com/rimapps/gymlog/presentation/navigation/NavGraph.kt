package com.rimapps.gymlog.presentation.navigation

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rimapps.gymlog.presentation.auth.AuthViewModel
import com.rimapps.gymlog.presentation.home.HomeScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rimapps.gymlog.presentation.auth.AuthScreen
import com.rimapps.gymlog.presentation.exercise.ExerciseSearchScreen

import com.rimapps.gymlog.presentation.home.HomeViewModel
import com.rimapps.gymlog.presentation.routine.CreateRoutineScreen
import com.rimapps.gymlog.presentation.routine.CreateRoutineViewModel
import com.rimapps.gymlog.presentation.workout.WorkoutScreen
import com.rimapps.gymlog.presentation.workout.WorkoutViewModel

@SuppressLint("UnrememberedGetBackStackEntry", "RestrictedApi")
@Composable
fun NavGraph(
    authViewModel: AuthViewModel,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Auth.route,
    onGoogleSignInClick: () -> Unit
) {
    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { controller, destination, arguments ->
            Log.d("NavGraph", "Navigation to ${destination.route}, current backstack: ${controller.currentBackStack.value.map { it.destination.route }}")
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Auth.route) {
            Log.d("NavGraph", "Entering Auth composable")
           // val viewModel: AuthViewModel = hiltViewModel()
            AuthScreen(
                viewModel = authViewModel,
                onNavigateToHome = {
                    Log.d("NavGraph", "Attempting navigation to Home")
                    try {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                        Log.d("NavGraph", "Navigation to Home successful")
                    } catch (e: Exception) {
                        Log.e("NavGraph", "Navigation failed", e)
                    }
                },
                onGoogleSignInClick = onGoogleSignInClick
            )
        }


        composable(Screen.Home.route) {
            Log.d("NavGraph", "Entering Home composable")
            val viewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                onSignOut = {
                    viewModel.signOut()
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                viewModel = viewModel,
                onStartWorkout = { _ ->
                    navController.navigate(Screen.ActiveWorkout.route)
                },
                onNavigateToSearch = {
                    navController.navigate(Screen.ExerciseSearch.route)
                }
            )
        }
        composable(Screen.ActiveWorkout.route) {
            val workoutViewModel: WorkoutViewModel = hiltViewModel()

            WorkoutScreen(
                onBack = { navController.popBackStack() },
                onFinish = {
                    // TODO: Save workout and navigate back
                    navController.popBackStack()
                },
                onAddExercise = {
                    // Navigate to exercise search from active workout
                    navController.navigate(Screen.ExerciseSearch.route)
                }
            )
        }
        composable(Screen.CreateRoutine.route) {
            val viewModel: CreateRoutineViewModel = hiltViewModel()
            CreateRoutineScreen(
                onBack = { navController.popBackStack() },
                onRoutineCreated = { navController.popBackStack() },
                onAddExercise = {
                    // When adding exercise, navigate to exercise search
                    navController.navigate(Screen.ExerciseSearch.route)
                }
            )
        }

        // In NavGraph.kt, update the ExerciseSearchScreen composable
        composable(Screen.ExerciseSearch.route) {
            // Determine which screen we came from
            val previousRoute = navController.previousBackStackEntry?.destination?.route

            // If we came from active workout, get that viewModel
            val workoutViewModel = if (previousRoute == Screen.ActiveWorkout.route) {
                hiltViewModel<WorkoutViewModel>(navController.getBackStackEntry(Screen.ActiveWorkout.route))
            } else null

            // If we came from create routine, get that viewModel
            val createRoutineViewModel = if (previousRoute == Screen.CreateRoutine.route) {
                hiltViewModel<CreateRoutineViewModel>(navController.getBackStackEntry(Screen.CreateRoutine.route))
            } else null

            ExerciseSearchScreen(
                onBack = { navController.popBackStack() },
                onExerciseClick = { exercise ->
                    // Add the selected exercise to the appropriate screen
                    when (previousRoute) {
                        Screen.ActiveWorkout.route -> {
                            workoutViewModel?.addExercise(exercise)
                            navController.popBackStack()
                        }
                        Screen.CreateRoutine.route -> {
                            createRoutineViewModel?.addExercise(exercise)
                            navController.popBackStack()
                        }
                        else -> navController.popBackStack()
                    }
                }
            )
        }
    }
}