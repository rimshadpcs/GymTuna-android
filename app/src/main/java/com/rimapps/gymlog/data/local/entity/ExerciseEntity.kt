package com.rimapps.gymlog.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rimapps.gymlog.domain.model.Exercise

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey
    val name: String,  // Using name as primary key since we don't have IDs
    val equipment: String,
    val muscleGroup: String,
    val defaultReps: Int,
    val defaultSets: Int,
    val isBodyweight: Boolean,
    val usesWeight: Boolean
)

fun ExerciseEntity.toExercise() = Exercise(
    name = name,
    equipment = equipment,
    muscleGroup = muscleGroup,
    defaultReps = defaultReps,
    defaultSets = defaultSets,
    isBodyweight = isBodyweight,
    usesWeight = usesWeight
)