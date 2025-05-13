package com.rimapps.gymlog.domain.model

data class WorkoutExercise(
    val exercise: Exercise,
    val sets: List<ExerciseSet> = emptyList(),
    val notes: String = ""
)