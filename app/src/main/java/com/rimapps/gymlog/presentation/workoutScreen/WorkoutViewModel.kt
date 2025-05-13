package com.rimapps.gymlog.presentation.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rimapps.gymlog.domain.model.Exercise
import com.rimapps.gymlog.domain.model.ExerciseSet
import com.rimapps.gymlog.domain.model.WorkoutExercise
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class WorkoutViewModel @Inject constructor() : ViewModel() {

    private val _workoutDuration = MutableStateFlow("0s")
    val workoutDuration: StateFlow<String> = _workoutDuration.asStateFlow()

    private val _totalVolume = MutableStateFlow(0)
    val totalVolume: StateFlow<Int> = _totalVolume.asStateFlow()

    private val _totalSets = MutableStateFlow(0)
    val totalSets: StateFlow<Int> = _totalSets.asStateFlow()

    private val _exercises = MutableStateFlow<List<WorkoutExercise>>(emptyList())
    val exercises: StateFlow<List<WorkoutExercise>> = _exercises.asStateFlow()

    private var startTime = System.currentTimeMillis()
    private var isWorkoutActive = true

    init {
        startWorkoutTimer()
    }

    private fun startWorkoutTimer() {
        viewModelScope.launch(Dispatchers.Default) {
            startTime = System.currentTimeMillis()
            while (isWorkoutActive) {
                val elapsedMillis = System.currentTimeMillis() - startTime
                _workoutDuration.value = formatDuration(elapsedMillis)
                delay(1000) // Update every second
            }
        }
    }

    private fun formatDuration(millis: Long): String {
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val hours = TimeUnit.MILLISECONDS.toHours(millis)

        return when {
            hours > 0 -> "${hours}h ${minutes}m ${seconds}s"
            minutes > 0 -> "${minutes}m ${seconds}s"
            else -> "${seconds}s"
        }
    }

    fun addExercise(exercise: Exercise) {
        val currentList = _exercises.value.toMutableList()

        // Create initial set for the exercise
        val initialSet = ExerciseSet(
            setNumber = 1,
            reps = exercise.defaultReps,
            previousReps = null
        )

        // Create workout exercise
        val workoutExercise = WorkoutExercise(
            exercise = exercise,
            sets = listOf(initialSet)
        )

        currentList.add(workoutExercise)
        _exercises.value = currentList
        calculateStats()
    }

    fun addSet(workoutExercise: WorkoutExercise) {
        val currentExercises = _exercises.value.toMutableList()
        val index = currentExercises.indexOfFirst { it.exercise.name == workoutExercise.exercise.name }

        if (index != -1) {
            val exerciseSets = currentExercises[index].sets.toMutableList()
            val newSetNumber = exerciseSets.size + 1

            // Get the previous set's reps as the default for this one
            val previousSetReps = exerciseSets.lastOrNull()?.reps ?: workoutExercise.exercise.defaultReps

            exerciseSets.add(
                ExerciseSet(
                    setNumber = newSetNumber,
                    reps = previousSetReps,
                    previousReps = previousSetReps
                )
            )

            currentExercises[index] = currentExercises[index].copy(sets = exerciseSets)
            _exercises.value = currentExercises
            calculateStats()
        }
    }
    fun updateWeight(workoutExercise: WorkoutExercise, set: ExerciseSet, weight: Double) {
        val currentExercises = _exercises.value.toMutableList()
        val exerciseIndex = currentExercises.indexOfFirst { it.exercise.name == workoutExercise.exercise.name }

        if (exerciseIndex != -1) {
            val exerciseSets = currentExercises[exerciseIndex].sets.toMutableList()
            val setIndex = exerciseSets.indexOfFirst { it.setNumber == set.setNumber }

            if (setIndex != -1) {
                exerciseSets[setIndex] = exerciseSets[setIndex].copy(weight = weight)
                currentExercises[exerciseIndex] = currentExercises[exerciseIndex].copy(sets = exerciseSets)
                _exercises.value = currentExercises
                calculateStats()
            }
        }
    }
    fun setCompleted(workoutExercise: WorkoutExercise, set: ExerciseSet, isCompleted: Boolean) {
        val currentExercises = _exercises.value.toMutableList()
        val exerciseIndex = currentExercises.indexOfFirst { it.exercise.name == workoutExercise.exercise.name }

        if (exerciseIndex != -1) {
            val exerciseSets = currentExercises[exerciseIndex].sets.toMutableList()
            val setIndex = exerciseSets.indexOfFirst { it.setNumber == set.setNumber }

            if (setIndex != -1) {
                exerciseSets[setIndex] = exerciseSets[setIndex].copy(isCompleted = isCompleted)
                currentExercises[exerciseIndex] = currentExercises[exerciseIndex].copy(sets = exerciseSets)
                _exercises.value = currentExercises
                calculateStats()
            }
        }
    }

    fun updateReps(workoutExercise: WorkoutExercise, set: ExerciseSet, reps: Int) {
        val currentExercises = _exercises.value.toMutableList()
        val exerciseIndex = currentExercises.indexOfFirst { it.exercise.name == workoutExercise.exercise.name }

        if (exerciseIndex != -1) {
            val exerciseSets = currentExercises[exerciseIndex].sets.toMutableList()
            val setIndex = exerciseSets.indexOfFirst { it.setNumber == set.setNumber }

            if (setIndex != -1) {
                exerciseSets[setIndex] = exerciseSets[setIndex].copy(reps = reps)
                currentExercises[exerciseIndex] = currentExercises[exerciseIndex].copy(sets = exerciseSets)
                _exercises.value = currentExercises
                calculateStats()
            }
        }
    }

    fun updateNotes(workoutExercise: WorkoutExercise, notes: String) {
        val currentExercises = _exercises.value.toMutableList()
        val exerciseIndex = currentExercises.indexOfFirst { it.exercise.name == workoutExercise.exercise.name }

        if (exerciseIndex != -1) {
            currentExercises[exerciseIndex] = currentExercises[exerciseIndex].copy(notes = notes)
            _exercises.value = currentExercises
        }
    }

    private fun calculateStats() {
        var totalVolume = 0
        var completedSets = 0

        _exercises.value.forEach { workoutExercise ->
            workoutExercise.sets.forEach { set ->
                if (set.isCompleted) {
                    totalVolume += (set.weight * set.reps).toInt()
                    completedSets++
                }
            }
        }

        _totalVolume.value = totalVolume
        _totalSets.value = completedSets
    }

    fun discardWorkout() {
        isWorkoutActive = false
        // Additional cleanup if needed
    }

    fun finishWorkout() {
        isWorkoutActive = false
        // Save workout data
    }

    override fun onCleared() {
        super.onCleared()
        isWorkoutActive = false
    }
}