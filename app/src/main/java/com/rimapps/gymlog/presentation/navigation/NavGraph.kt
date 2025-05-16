package com.rimapps.gymlog.presentation.navigation

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rimapps.gymlog.presentation.auth.AuthViewModel
import com.rimapps.gymlog.presentation.home.HomeScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.rimapps.gymlog.domain.model.AuthState
import com.rimapps.gymlog.presentation.auth.AuthScreen
import com.rimapps.gymlog.presentation.exercise.ExerciseSearchScreen

import com.rimapps.gymlog.presentation.home.HomeViewModel
import com.rimapps.gymlog.presentation.routine.CreateRoutineScreen
import com.rimapps.gymlog.presentation.routine.CreateRoutineViewModel
import com.rimapps.gymlog.presentation.settings.SettingsScreen
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
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                Log.d("NavGraph", "Auth success, navigating to Home")
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Auth.route) { inclusive = true }
                }
            }

            is AuthState.Error -> {
                Log.e("NavGraph", "Auth error: ${(authState as AuthState.Error).message}")
            }

            else -> {} // Handle other states if needed
        }
    }

    DisposableEffect(navController) {
        val listener =
            NavController.OnDestinationChangedListener { controller, destination, arguments ->
                Log.d(
                    "NavGraph",
                    "Navigation to ${destination.route}, current backstack: ${controller.currentBackStack.value.map { it.destination.route }}"
                )
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
            AuthScreen(
                viewModel = authViewModel,
                onGoogleSignInClick = onGoogleSignInClick,
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
                }
            )
        }

        composable(Screen.Home.route) {
            val viewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                onSignOut = {
                    viewModel.signOut()
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                viewModel = viewModel,
                onStartEmptyWorkout = {
                    navController.navigate(Screen.ActiveWorkout.route)
                },
                onStartRoutine = { routineId ->
                    navController.navigate("${Screen.ActiveWorkout.route}?routineId=$routineId")
                },
                onNewRoutine = {
                    navController.navigate(Screen.CreateRoutine.route)
                },
                onEditRoutine = { routineId ->
                    navController.navigate("${Screen.CreateRoutine.route}?routineId=$routineId")
                },
                onNavigateToSearch = {
                    navController.navigate(Screen.ExerciseSearch.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        composable(
            route = "${Screen.CreateRoutine.route}?routineId={routineId}",
            arguments = listOf(
                navArgument("routineId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val routineId = backStackEntry.arguments?.getString("routineId")
            val viewModel: CreateRoutineViewModel = hiltViewModel()

            CreateRoutineScreen(
                routineId = routineId, // Pass routineId to distinguish between create and edit modes
                onBack = { navController.popBackStack() },
                onRoutineCreated = { navController.popBackStack() },
                onAddExercise = {
                    navController.navigate(Screen.ExerciseSearch.route)
                }
            )
        }

        composable(
            route = "${Screen.ActiveWorkout.route}?routineId={routineId}",
            arguments = listOf(
                navArgument("routineId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val routineId = backStackEntry.arguments?.getString("routineId")
            Log.d("NavGraph", "Composing WorkoutScreen with routineId: $routineId")

            val workoutViewModel: WorkoutViewModel = hiltViewModel()

            WorkoutScreen(
                routineId = routineId,
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


        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onSignOut = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        // In NavGraph.kt, update the ExerciseSearchScreen composable
        composable(Screen.ExerciseSearch.route) {
            val previousRoute =
                navController.previousBackStackEntry?.destination?.route?.substringBefore("?")

            val workoutViewModel = if (previousRoute == Screen.ActiveWorkout.route) {
                hiltViewModel<WorkoutViewModel>(
                    navController.getBackStackEntry(
                        navController.previousBackStackEntry?.destination?.route
                            ?: Screen.ActiveWorkout.route
                    )
                )
            } else null

            val createRoutineViewModel = if (previousRoute == Screen.CreateRoutine.route) {
                hiltViewModel<CreateRoutineViewModel>(
                    navController.getBackStackEntry(Screen.CreateRoutine.route)
                )
            } else null

            ExerciseSearchScreen(
                onBack = { navController.popBackStack() },
                onExerciseClick = { exercise ->
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