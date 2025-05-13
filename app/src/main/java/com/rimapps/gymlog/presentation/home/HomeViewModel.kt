package com.rimapps.gymlog.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rimapps.gymlog.domain.model.Workout
import com.rimapps.gymlog.domain.model.WorkoutState
import com.rimapps.gymlog.domain.repository.AuthRepository
import com.rimapps.gymlog.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
open class HomeViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _workoutState = MutableStateFlow<WorkoutState>(WorkoutState.Idle)
    open val workoutState = _workoutState.asStateFlow()

    private val _currentWorkout = MutableStateFlow<Workout?>(null)
    val currentWorkout = _currentWorkout.asStateFlow()

    init {
        loadWorkouts()
    }

//    private fun loadWorkouts() {
//        viewModelScope.launch {
//            try {
//                _workoutState.value = WorkoutState.Loading
//                val userId = authRepository.getCurrentUser()?.uid
//                    ?: throw Exception("User not authenticated")
//
//                workoutRepository.getWorkouts(userId).collect { workouts ->
//                    _workoutState.value = WorkoutState.Success(workouts)
//                }
//            } catch (e: Exception) {
//                _workoutState.value = WorkoutState.Error(e.message ?: "Unknown error")
//            }
//        }
//    }
private fun loadWorkouts() {
    viewModelScope.launch {
        try {
            _workoutState.value = WorkoutState.Loading
            val userId = authRepository.getCurrentUser()?.uid ?: "anonymous"

            workoutRepository.getWorkouts(userId).collect { workouts ->
                if (workouts.isEmpty()) {
                    // Return an empty success state when no workouts exist
                    _workoutState.value = WorkoutState.Success(emptyList())
                } else {
                    _workoutState.value = WorkoutState.Success(workouts)
                }
            }
        } catch (e: Exception) {
            _workoutState.value = WorkoutState.Error(e.message ?: "Unknown error")
        }
    }
}

    open fun startWorkout(workoutId: String?) {
        viewModelScope.launch {
            workoutId?.let {
                val workouts = (_workoutState.value as? WorkoutState.Success)?.workouts
                _currentWorkout.value = workouts?.find { it.id == workoutId }
            } ?: run {
                _currentWorkout.value = Workout(
                    id = UUID.randomUUID().toString(),
                    name = "Quick Workout",
                    userId = authRepository.getCurrentUser()?.uid ?: return@launch
                )
            }
        }
    }

    open fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}