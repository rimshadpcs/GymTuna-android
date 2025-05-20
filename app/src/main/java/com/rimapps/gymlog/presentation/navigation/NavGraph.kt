/*  com.rimapps.gymlog.presentation.navigation.NavGraph  */

@file:Suppress("UnrememberedGetBackStackEntry", "RestrictedApi")

package com.rimapps.gymlog.presentation.navigation

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import androidx.navigation.compose.*

import com.rimapps.gymlog.domain.model.AuthState
import com.rimapps.gymlog.presentation.auth.*
import com.rimapps.gymlog.presentation.home.*
import com.rimapps.gymlog.presentation.exercise.ExerciseSearchScreen
import com.rimapps.gymlog.presentation.routine.*
import com.rimapps.gymlog.presentation.settings.SettingsScreen
import com.rimapps.gymlog.presentation.history.HistoryScreen
import com.rimapps.gymlog.presentation.workoutScreen.WorkoutScreen
import com.rimapps.gymlog.presentation.workoutScreen.WorkoutViewModel

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NavGraph(
    authViewModel     : AuthViewModel,
    navController     : NavHostController = rememberNavController(),
    startDestination  : String            = Screen.Auth.route,
    onGoogleSignInClick: () -> Unit
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success ->
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Auth.route) { inclusive = true }
                }
            is AuthState.Error ->
                Log.e("NavGraph", "Auth error: ${(authState as AuthState.Error).message}")
            else -> Unit
        }
    }

    /* optional back-stack logger */
    DisposableEffect(navController) {
        val l = NavController.OnDestinationChangedListener { c, d, _ ->
            Log.d("NavGraph", "Navigated to ${d.route}. stack=${c.currentDestination?.route}")
        }
        navController.addOnDestinationChangedListener(l)
        onDispose { navController.removeOnDestinationChangedListener(l) }
    }
    NavHost(navController, startDestination) {

        /* ---------- Auth ---------- */
        composable(Screen.Auth.route) {
            AuthScreen(
                viewModel = authViewModel,
                onGoogleSignInClick = onGoogleSignInClick,
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            val vm: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = vm,
                onSignOut = {
                    vm.signOut()
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onStartEmptyWorkout = { navController.navigate(Screen.ActiveWorkout.route) },
                onStartRoutine      = { id -> navController.navigate(Screen.ActiveWorkout.withRoutineId(id)) },
                onNewRoutine        = { navController.navigate(Screen.CreateRoutine.route) },
                onEditRoutine       = { id -> navController.navigate(Screen.CreateRoutine.withRoutineId(id)) },
                onNavigateToSearch  = { navController.navigate(Screen.ExerciseSearch.route) },
                onNavigateToSettings= { navController.navigate(Screen.Settings.route) },
                /* ---------- NEW: “History” from top-right button ---------- */
                onNavigateToHistory = { navController.navigate(Screen.History.route) }
            )
        }

        composable(Screen.History.route) {
            val vm = hiltViewModel<com.rimapps.gymlog.presentation.history.HistoryViewModel>()
            HistoryScreen(
                viewModel = vm,
                onBack    = { navController.popBackStack() }
            )
        }

        composable(
            "${Screen.CreateRoutine.route}?routineId={routineId}",
            arguments = listOf(navArgument("routineId") {
                type = NavType.StringType; nullable = true; defaultValue = null
            })
        ) { entry ->
            val routineId = entry.arguments?.getString("routineId")
            val vm: CreateRoutineViewModel = hiltViewModel()
            CreateRoutineScreen(
                routineId        = routineId,
                onBack           = { navController.popBackStack() },
                onRoutineCreated = { navController.popBackStack() },
                onAddExercise    = { navController.navigate(Screen.ExerciseSearch.route) }
            )
        }

        composable(
            "${Screen.ActiveWorkout.route}?routineId={routineId}",
            arguments = listOf(navArgument("routineId") {
                type = NavType.StringType; nullable = true; defaultValue = null
            })
        ) { entry ->
            val routineId = entry.arguments?.getString("routineId")
            val vm: WorkoutViewModel = hiltViewModel()
            val routineName by vm.routineName.collectAsStateWithLifecycle()

            WorkoutScreen(
                routineId   = routineId,
                routineName = routineName,
                onBack      = { navController.popBackStack() },
                onFinish    = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onAddExercise = { navController.navigate(Screen.ExerciseSearch.route) }
            )
        }

        composable(Screen.ExerciseSearch.route) {
            val prevRoute = navController.previousBackStackEntry
                ?.destination?.route?.substringBefore("?")

            val workoutVm = if (prevRoute == Screen.ActiveWorkout.route)
                hiltViewModel<WorkoutViewModel>(
                    navController.getBackStackEntry(Screen.ActiveWorkout.route)
                ) else null

            val routineVm = if (prevRoute == Screen.CreateRoutine.route)
                hiltViewModel<CreateRoutineViewModel>(
                    navController.getBackStackEntry(Screen.CreateRoutine.route)
                ) else null

            ExerciseSearchScreen(
                onBack = { navController.popBackStack() },
                onExerciseClick = { ex ->
                    when (prevRoute) {
                        Screen.ActiveWorkout.route -> workoutVm?.addExercise(ex)
                        Screen.CreateRoutine.route -> routineVm?.addExercise(ex)
                    }
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack    = { navController.popBackStack() },
                onSignOut = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
