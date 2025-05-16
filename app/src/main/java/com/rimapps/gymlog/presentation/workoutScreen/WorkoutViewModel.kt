package com.rimapps.gymlog.presentation.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rimapps.gymlog.data.repository.FirestoreRepository
import com.rimapps.gymlog.domain.model.*
import com.rimapps.gymlog.domain.repository.AuthRepository
import com.rimapps.gymlog.domain.repository.WorkoutHistoryRepository
import com.rimapps.gymlog.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val workoutHistoryRepository: WorkoutHistoryRepository,
    private val workoutRepository: WorkoutRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _workoutDuration = MutableStateFlow("0s")
    val workoutDuration: StateFlow<String> = _workoutDuration.asStateFlow()

    private val _totalVolume = MutableStateFlow(0)
    val totalVolume: StateFlow<Int> = _totalVolume.asStateFlow()

    private val _totalSets = MutableStateFlow(0)
    val totalSets: StateFlow<Int> = _totalSets.asStateFlow()

    private val _exercises = MutableStateFlow<List<WorkoutExercise>>(emptyList())
    val exercises: StateFlow<List<WorkoutExercise>> = _exercises.asStateFlow()

    private val _isFinishing = MutableStateFlow(false)
    val isFinishing: StateFlow<Boolean> = _isFinishing.asStateFlow()

    private val _workoutState = MutableStateFlow<ActiveWorkoutState>(ActiveWorkoutState.Initial)
    val workoutState: StateFlow<ActiveWorkoutState> = _workoutState.asStateFlow()


    private var startTime = System.currentTimeMillis()
    private var isWorkoutActive = true
    private var currentRoutineId: String? = null

    init {
        startWorkoutTimer()
    }
    fun initializeFromRoutine(routineId: String) {
        viewModelScope.launch {
            try {
                val userId = authRepository.getCurrentUser()?.uid ?: return@launch
                val routine = workoutRepository.getWorkoutById(routineId)

                routine?.let { savedRoutine ->
                    currentRoutineId = routineId

                    val workoutExercises = savedRoutine.exercises.map { exercise ->
                        WorkoutExercise(
                            exercise = exercise,
                            sets = List(3) { setNumber -> // Changed from exercise.defaultSets to 3
                                ExerciseSet(
                                    setNumber = setNumber + 1,
                                    reps = exercise.defaultReps,
                                    weight = if (exercise.usesWeight && !exercise.isBodyweight) 0.0 else 0.0,
                                    isCompleted = false,
                                    previousReps = null,
                                    previousWeight = null
                                )
                            }
                        )
                    }

                    _exercises.value = workoutExercises
                }
            } catch (e: Exception) {
                _workoutState.value = ActiveWorkoutState.Error(e.message ?: "Failed to load routine")
            }
        }
    }


    fun finishWorkout(routineName: String? = null, saveAsRoutine: Boolean = false, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _workoutState.value = ActiveWorkoutState.Loading

                // Get current user ID
                val userId = authRepository.getCurrentUser()?.uid
                    ?: throw Exception("User not authenticated")

                // Convert workout exercises to completed exercises
                val completedExercises = _exercises.value
                    .filter { exercise -> exercise.sets.any { it.isCompleted } }
                    .map { workoutExercise ->
                        CompletedExercise(
                            exerciseId = workoutExercise.exercise.name,
                            name = workoutExercise.exercise.name,
                            sets = workoutExercise.sets
                                .filter { it.isCompleted }
                                .map { set ->
                                    CompletedSet(
                                        setNumber = set.setNumber,
                                        weight = set.weight,
                                        reps = set.reps
                                    )
                                },
                            notes = workoutExercise.notes
                        )
                    }

                if (completedExercises.isEmpty()) {
                    throw Exception("No completed exercises in workout")
                }

                // Save workout history
                val workoutHistory = WorkoutHistory(
                    name = routineName ?: generateWorkoutName(),
                    userId = userId,
                    startTime = startTime,
                    endTime = System.currentTimeMillis(),
                    exercises = completedExercises,
                    totalVolume = _totalVolume.value.toDouble(),
                    totalSets = _totalSets.value
                )

                // Save to workout history
                workoutHistoryRepository.saveWorkoutHistory(workoutHistory)

                // If saving as routine, create a new routine
                if (saveAsRoutine && routineName != null) {
                    val routine = Workout(
                        id = routineName.toLowerCase().replace(" ", "_"),
                        name = routineName,
                        exercises = _exercises.value.map { it.exercise },
                        userId = userId,
                        createdAt = System.currentTimeMillis()
                    )
                    workoutRepository.createWorkout(routine)
                }

                // Clear workout state
                isWorkoutActive = false
                _workoutState.value = ActiveWorkoutState.Success
                onSuccess()

            } catch (e: Exception) {
                _workoutState.value = ActiveWorkoutState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun deleteSet(workoutExercise: WorkoutExercise, setNumber: Int) {
        val currentExercises = _exercises.value.toMutableList()
        val exerciseIndex = currentExercises.indexOfFirst { it.exercise.name == workoutExercise.exercise.name }

        if (exerciseIndex != -1) {
            val exerciseSets = currentExercises[exerciseIndex].sets.toMutableList()
            exerciseSets.removeAll { it.setNumber == setNumber }

            // Reorder set numbers
            exerciseSets.forEachIndexed { index, set ->
                exerciseSets[index] = set.copy(setNumber = index + 1)
            }

            currentExercises[exerciseIndex] = currentExercises[exerciseIndex].copy(sets = exerciseSets)
            _exercises.value = currentExercises
            calculateStats()
        }
    }



    init {
        startWorkoutTimer()
    }


    fun addExercise(exercise: Exercise) {
        val currentList = _exercises.value.toMutableList()
        // Create workout exercise with 3 initial sets
        val workoutExercise = WorkoutExercise(
            exercise = exercise,
            sets = List(3) { setNumber ->
                ExerciseSet(
                    setNumber = setNumber + 1,
                    reps = exercise.defaultReps,
                    previousReps = null
                )
            }
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

    private fun startWorkoutTimer() {
        viewModelScope.launch {
            startTime = System.currentTimeMillis()
            while (isWorkoutActive) {
                val elapsedMillis = System.currentTimeMillis() - startTime
                _workoutDuration.value = formatDuration(elapsedMillis)
                kotlinx.coroutines.delay(1000) // Update every second
            }
        }
    }

    private fun formatDuration(millis: Long): String {
        val seconds = java.util.concurrent.TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        val minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val hours = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(millis)

        return when {
            hours > 0 -> "${hours}h ${minutes}m ${seconds}s"
            minutes > 0 -> "${minutes}m ${seconds}s"
            else -> "${seconds}s"
        }
    }


    private fun generateWorkoutName(): String {
        val muscleGroups = _exercises.value
            .map { it.exercise.muscleGroup }
            .distinct()
            .take(2)

        return when {
            muscleGroups.isEmpty() -> "Quick Workout"
            muscleGroups.size == 1 -> "${muscleGroups[0]} Workout"
            else -> "${muscleGroups[0]} & ${muscleGroups[1]} Workout"
        }
    }

    fun discardWorkout() {
        isWorkoutActive = false
        // Additional cleanup if needed
    }

    override fun onCleared() {
        super.onCleared()
        isWorkoutActive = false
    }
}