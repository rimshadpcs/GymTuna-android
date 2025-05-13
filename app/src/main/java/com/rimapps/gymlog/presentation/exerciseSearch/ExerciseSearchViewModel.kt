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
import javax.inject.Inject
@HiltViewModel
class ExerciseSearchViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    @OptIn(FlowPreview::class)
    val exercises = searchQuery
        .debounce(300L)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                Log.d("ExerciseSearch", "Getting all exercises")
                workoutRepository.getExercises()
            } else {
                Log.d("ExerciseSearch", "Searching exercises with query: $query")
                workoutRepository.searchExercises(query)
            }
        }
        .onEach {
            Log.d("ExerciseSearch", "Received ${it.size} exercises")
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun searchExercises(query: String) {
        searchQuery.value = query
    }
}