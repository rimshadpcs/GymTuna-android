package com.rimapps.gymlog.presentation.exerciseSearch

import android.util.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rimapps.gymlog.domain.model.Exercise
import com.rimapps.gymlog.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class ExerciseSearchViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {


    private val searchQuery = MutableStateFlow("")
    private val allExercises = MutableStateFlow<List<Exercise>>(emptyList())

    private fun String.normalizeForSearch(): String {
        return this
            .lowercase()
            .replace("-", "")
            .replace(" ", "")
            .trim()
    }

    init {
        // Load all exercises once when ViewModel is created
        viewModelScope.launch {
            workoutRepository.getExercises()
                .catch { e ->
                    Log.e("ExerciseSearch", "Error loading exercises", e)
                }
                .collect { exercises ->
                    allExercises.value = exercises
                }
        }
    }

    @OptIn(FlowPreview::class)
    val exercises = searchQuery
        .debounce(300L)
        .combine(allExercises) { query, exercises ->
            performSearch(query, exercises)
        }
        .onEach {
            Log.d("ExerciseSearch", "Search returned ${it.size} exercises")
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    private fun performSearch(query: String, exercises: List<Exercise>): List<Exercise> {
        if (query.isBlank()) return exercises

        val normalizedQuery = query.normalizeForSearch()

        return exercises.filter { exercise ->
            val normalizedName = exercise.name.normalizeForSearch()
            val normalizedEquipment = exercise.equipment.normalizeForSearch()
            val normalizedMuscleGroup = exercise.muscleGroup.normalizeForSearch()

            // Check if query matches any part of the exercise data
            normalizedName.contains(normalizedQuery) ||
                    normalizedEquipment.contains(normalizedQuery) ||
                    normalizedMuscleGroup.contains(normalizedQuery) ||
                    // Check for partial word matches
                    exercise.name.split(Regex("[-\\s]"))
                        .any { it.normalizeForSearch().contains(normalizedQuery) } ||
                    // Check for acronym match (e.g., "bp" matches "bench press")
                    createAcronym(exercise.name).contains(normalizedQuery)
        }.sortedBy { it.name }
    }

    private fun createAcronym(text: String): String {
        return text.split(Regex("[-\\s]"))
            .filter { it.isNotEmpty() }
            .map { it.first().lowercase() }
            .joinToString("")
    }

    fun searchExercises(query: String) {
        searchQuery.value = query
    }
    companion object {
        private fun String.normalizeForSearch(): String {
            return this
                .lowercase()
                .replace("-", "")
                .replace(" ", "")
                .trim()
        }
    }
}