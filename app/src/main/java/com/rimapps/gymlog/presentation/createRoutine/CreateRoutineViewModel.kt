package com.rimapps.gymlog.presentation.routine

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
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateRoutineViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _selectedExercises = MutableStateFlow<List<Exercise>>(emptyList())
    val selectedExercises: StateFlow<List<Exercise>> = _selectedExercises.asStateFlow()

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
                    id = UUID.randomUUID().toString(),
                    name = name,
                    exercises = _selectedExercises.value,
                    userId = userId,
                    createdAt = System.currentTimeMillis()
                )
                workoutRepository.createWorkout(workout)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}