package com.rimapps.gymlog.presentation.routine

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rimapps.gymlog.domain.model.Exercise
import com.rimapps.gymlog.domain.model.Workout
import com.rimapps.gymlog.domain.repository.AuthRepository
import com.rimapps.gymlog.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateRoutineViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val authRepository: AuthRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val routineId: String? = savedStateHandle.get<String>("routineId")
    private val _routineName = MutableStateFlow(savedStateHandle.get<String>("routineName") ?: "")
    val routineName: StateFlow<String> = _routineName.asStateFlow()

    private val _selectedExercises = MutableStateFlow<List<Exercise>>(emptyList())
    val selectedExercises: StateFlow<List<Exercise>> = _selectedExercises.asStateFlow()

    init {
        // Load existing routine if in edit mode
        routineId?.let { id ->
            viewModelScope.launch {
                workoutRepository.getWorkoutById(id)?.let { workout ->
                    setRoutineName(workout.name) // Use the new setRoutineName function
                    _selectedExercises.value = workout.exercises
                }
            }
        }
    }

    fun initializeRoutine(routineId: String) {
        viewModelScope.launch {
            try {
                val routine = workoutRepository.getWorkoutById(routineId)
                routine?.let { workout ->
                    setRoutineName(workout.name) // Use the new setRoutineName function
                    _selectedExercises.value = workout.exercises
                }
            } catch (e: Exception) {
                Log.e("CreateRoutineViewModel", "Error loading routine", e)
            }
        }
    }
    fun setRoutineName(name: String) {
        _routineName.value = name
        // Save the routine name to SavedStateHandle
        savedStateHandle["routineName"] = name
    }





    fun addExercise(exercise: Exercise) {
        val currentList = _selectedExercises.value.toMutableList()
        if (!currentList.any { it.name == exercise.name }) {
            currentList.add(exercise)
            _selectedExercises.value = currentList
        }
    }

    fun removeExercise(exercise: Exercise) {
        val currentList = _selectedExercises.value.toMutableList()
        currentList.removeAll { it.name == exercise.name }
        _selectedExercises.value = currentList
    }



    fun saveRoutine(name: String) {
        if (name.isBlank() || _selectedExercises.value.isEmpty()) return

        viewModelScope.launch {
            try {
                val userId = authRepository.getCurrentUser()?.uid ?: "anonymous"
                val workout = Workout(
                    id = routineId ?: name.toLowerCase(Locale.ROOT).replace(" ", "_"),
                    name = name,
                    exercises = _selectedExercises.value,
                    userId = userId,
                    createdAt = System.currentTimeMillis()
                )
                workoutRepository.createWorkout(workout)
                // Clear the saved routine name after successful save
                savedStateHandle.remove<String>("routineName")
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}