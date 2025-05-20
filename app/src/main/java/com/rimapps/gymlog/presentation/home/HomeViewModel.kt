package com.rimapps.gymlog.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rimapps.gymlog.domain.model.WeeklyCalendarDay
import com.rimapps.gymlog.domain.model.Workout
import com.rimapps.gymlog.domain.model.WorkoutState
import com.rimapps.gymlog.domain.repository.AuthRepository
import com.rimapps.gymlog.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _workoutState = MutableStateFlow<WorkoutState>(WorkoutState.Initial)
    val workoutState: StateFlow<WorkoutState> = _workoutState.asStateFlow()

    private val _currentWorkout = MutableStateFlow<Workout?>(null)
    val currentWorkout: StateFlow<Workout?> = _currentWorkout.asStateFlow()

    private val _weeklyCalendar = MutableStateFlow<List<WeeklyCalendarDay>>(emptyList())
    val weeklyCalendar: StateFlow<List<WeeklyCalendarDay>> = _weeklyCalendar.asStateFlow()
    init {
        viewModelScope.launch {
            try {
                val userId = authRepository.getCurrentUser()?.uid
                    ?: throw Exception("User not authenticated")

                // Add debug logging
                Log.d(TAG, "Starting to collect weekly calendar data")
                workoutRepository.getWeeklyCalendar().collect { calendar ->
                    Log.d(TAG, "Received calendar data: ${calendar.size} days")
                    _weeklyCalendar.value = calendar

                    calendar.forEach {
                        Log.d(TAG, "Day ${it.date}  colorHex=${it.colorHex}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error collecting calendar data", e)
                _workoutState.value = WorkoutState.Error(e.message ?: "Unknown error")
            }
        }
    }
    init {
        loadWorkouts()
    }

    private fun loadWorkouts() {
        viewModelScope.launch {
            try {
                _workoutState.value = WorkoutState.Loading
                val userId = authRepository.getCurrentUser()?.uid
                    ?: throw Exception("User not authenticated")

                workoutRepository.getWorkouts(userId)
                    .catch { e ->
                        Log.e(TAG, "Error loading workouts", e)
                        _workoutState.value = WorkoutState.Error("Failed to load workouts: ${e.message}")
                    }
                    .collect { workouts ->
                        _workoutState.value = WorkoutState.Success(workouts)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error in loadWorkouts", e)
                _workoutState.value = WorkoutState.Error("Failed to load workouts: ${e.message}")
            }
        }
    }

    fun duplicateWorkout(workout: Workout) {
        viewModelScope.launch {
            try {
                val newName = "${workout.name} (Copy)"
                val newWorkout = workout.copy(
                    id = newName.toLowerCase().replace(" ", "_"),
                    name = newName,
                    createdAt = System.currentTimeMillis()
                )
                workoutRepository.createWorkout(newWorkout)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteWorkout(workoutId: String) {
        viewModelScope.launch {
            try {
                workoutRepository.deleteWorkout(workoutId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
//    fun startWorkout(workoutId: String?) {
//        viewModelScope.launch {
//            try {
//                val userId = authRepository.getCurrentUser()?.uid
//                    ?: throw Exception("User not authenticated")
//
//                workoutId?.let {
//                    when (val state = _workoutState.value) {
//                        is WorkoutState.Success -> {
//                            _currentWorkout.value = state.workouts.find { it.id == workoutId }
//                        }
//                        else -> {
//                            Log.w(TAG, "Attempted to start workout while not in success state")
//                        }
//                    }
//                } ?: run {
//                    _currentWorkout.value = Workout(
//                        id = UUID.randomUUID().toString(),
//                        name = "Quick Workout",
//                        userId = userId,
//                        exercises = emptyList(),
//                        createdAt = TODO(),
//                        colorHex = TODO(),
//                        lastPerformed = TODO()
//                    )
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "Error starting workout", e)
//                _workoutState.value = WorkoutState.Error("Failed to start workout: ${e.message}")
//            }
//        }
//    }

    fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                _workoutState.value = WorkoutState.Initial
                _currentWorkout.value = null
            } catch (e: Exception) {
                Log.e(TAG, "Error signing out", e)
                _workoutState.value = WorkoutState.Error("Sign out failed: ${e.message}")
            }
        }
    }

    fun retryLoadWorkouts() {
        loadWorkouts()
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }
}