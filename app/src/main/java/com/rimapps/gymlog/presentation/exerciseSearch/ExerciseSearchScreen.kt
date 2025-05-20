package com.rimapps.gymlog.presentation.exercise

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rimapps.gymlog.R
import com.rimapps.gymlog.domain.model.Exercise
import com.rimapps.gymlog.domain.model.ExerciseSet
import com.rimapps.gymlog.domain.model.WorkoutExercise
import com.rimapps.gymlog.presentation.exerciseSearch.ExerciseSearchViewModel
import com.rimapps.gymlog.ui.theme.vag

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseSearchScreen(
    onBack: () -> Unit,
    viewModel: ExerciseSearchViewModel = hiltViewModel(),
    onExerciseClick: (Exercise) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
            .systemBarsPadding()
    ) {
        // Search Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
            TextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.searchExercises(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Black, RoundedCornerShape(12.dp)),
                placeholder = { Text("Search exercises") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.Black
                    )
                },
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Exercise List or No Results
        if (exercises.isEmpty() && searchQuery.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.gym),
                        contentDescription = "No results",
                        modifier = Modifier.size(48.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No exercises found",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray,
                        fontFamily = vag,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(exercises) { exercise ->
                    ExerciseSearchItem(
                        exercise = exercise,
                        onClick = { onExerciseClick(exercise) }
                    )
                }
            }
        }
    }
}
@Composable
fun ExerciseSearchItem( // Renamed to avoid confusion
    exercise: Exercise,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Black, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Muscle group image
            Image(
                painter = painterResource(
                    id = getMuscleGroupIcon(exercise.muscleGroup)
                ),
                contentDescription = "Muscle group",
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = vag,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = exercise.equipment,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}

private fun getMuscleGroupIcon(muscleGroup: String): Int {
    return when (muscleGroup.lowercase()) {
        "chest" -> R.drawable.chest
        "back" -> R.drawable.back
        "legs" -> R.drawable.leg
        "shoulders" -> R.drawable.shoulders
        "biceps" -> R.drawable.biceps
        "triceps" -> R.drawable.triceps
        "abs" -> R.drawable.core
        "calves"-> R.drawable.calves
        "forearms" -> R.drawable.forearms
        "glutes" -> R.drawable.glutes
        "full body" -> R.drawable.fullbody
        else -> R.drawable.fullbody
    }
}