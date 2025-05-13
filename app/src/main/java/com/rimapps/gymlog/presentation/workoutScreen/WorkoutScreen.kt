package com.rimapps.gymlog.presentation.workout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rimapps.gymlog.R
import com.rimapps.gymlog.domain.model.Exercise
import com.rimapps.gymlog.domain.model.ExerciseSet
import com.rimapps.gymlog.domain.model.WorkoutExercise
import com.rimapps.gymlog.ui.theme.vag
@Composable
fun WorkoutScreen(
    onBack: () -> Unit,
    onFinish: () -> Unit,
    onAddExercise: () -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val workoutDuration by viewModel.workoutDuration.collectAsStateWithLifecycle()
    val totalVolume by viewModel.totalVolume.collectAsStateWithLifecycle()
    val totalSets by viewModel.totalSets.collectAsStateWithLifecycle()
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
                Text(
                    text = "Log Workout",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black,
                    fontFamily = vag,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Timer icon
                Icon(
                    painter = painterResource(id = R.drawable.timer), // Use your timer icon resource
                    contentDescription = "Timer",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Finish button
                Button(
                    onClick = onFinish,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Finish",
                        fontFamily = vag,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Workout stats
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Duration
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = "Duration",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Text(
                    text = workoutDuration,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                    fontFamily = vag,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Sets
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = "Sets",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Text(
                    text = "$totalSets",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                    fontFamily = vag,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Divider(color = Color.LightGray)

        // Exercise list or empty state
        if (exercises.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Dumbbell icon
                    Icon(
                        painter = painterResource(id = R.drawable.gym),
                        contentDescription = "Dumbbell",
                        modifier = Modifier.size(48.dp),
                        tint = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Get started",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        fontFamily = vag,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Add an exercise to start your workout",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(exercises) { workoutExercise ->
                    ExerciseItem(
                        workoutExercise = workoutExercise,
                        onAddSet = { viewModel.addSet(workoutExercise) },
                        onSetCompleted = { set, isCompleted ->
                            viewModel.setCompleted(workoutExercise, set, isCompleted)
                        },
                        onUpdateReps = { set, reps ->
                            viewModel.updateReps(workoutExercise, set, reps)
                        },
                        onUpdateWeight = { set, weight ->
                            viewModel.updateWeight(workoutExercise, set, weight)
                        },
                        onUpdateNotes = { notes ->
                            viewModel.updateNotes(workoutExercise, notes)
                        }
                    )
                }
            }
        }

        // Bottom buttons
        Button(
            onClick = onAddExercise,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            border = BorderStroke(1.dp, Color.Black),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Exercise"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add Exercise",
                    fontFamily = vag,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Button(
                onClick = {
                    viewModel.discardWorkout()
                    onBack()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Red
                ),
                border = BorderStroke(1.dp, Color.Red),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Discard Workout",
                    fontFamily = vag,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Red
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseItem(
    workoutExercise: WorkoutExercise,
    onAddSet: () -> Unit,
    onSetCompleted: (ExerciseSet, Boolean) -> Unit,
    onUpdateWeight: (ExerciseSet, Double) -> Unit,
    onUpdateReps: (ExerciseSet, Int) -> Unit,
    onUpdateNotes: (String) -> Unit
) {
    var notes by remember { mutableStateOf(workoutExercise.notes) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Exercise header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Exercise icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color.Black, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(
                            id = getMuscleGroupIcon(workoutExercise.exercise.muscleGroup)
                        ),
                        contentDescription = "Muscle group",
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Exercise name
                Text(
                    text = workoutExercise.exercise.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black,
                    fontFamily = vag,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // More options
            IconButton(onClick = { /* TODO: Show options */ }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = Color.Black
                )
            }
        }

        // Notes field
        TextField(
            value = notes,
            onValueChange = {
                notes = it
                onUpdateNotes(it)
            },
            placeholder = { Text("Add notes here...", color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            singleLine = true
        )

        // Rest timer
        Button(
            onClick = { /* TODO: Toggle rest timer */ },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.Black
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.timer),
                contentDescription = "Rest Timer",
                tint = Color.Black,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Rest Timer: OFF",
                fontFamily = vag,
                fontWeight = FontWeight.Medium
            )
        }

        // Sets header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "SET",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Text(
                text = "LAST WORKOUT",  // Changed from PREVIOUS to LAST WORKOUT
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Text(
                text = "REPS",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Box(modifier = Modifier.size(24.dp)) // Space for check
        }

        // Sets
        // Sets
        workoutExercise.sets.forEach { set ->
            SetRow(
                setNumber = set.setNumber,
                set = set,
                usesWeight = workoutExercise.exercise.usesWeight, // Use the actual usesWeight property
                onCompleted = { isCompleted -> onSetCompleted(set, isCompleted) },
                onUpdateReps = { reps -> onUpdateReps(set, reps) },
                onUpdateWeight = { weight -> onUpdateWeight(set, weight) }
            )
        }

        // Add set button
        OutlinedButton(
            onClick = onAddSet,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            border = BorderStroke(1.dp, Color.Gray),
            shape = RoundedCornerShape(32.dp) // More rounded for pill shape
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Set",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Add Set",
                fontFamily = vag,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }

    Divider(color = Color.LightGray)
}

@Composable
fun SetRow(
    setNumber: Int,
    set: ExerciseSet,
    usesWeight: Boolean,
    onCompleted: (Boolean) -> Unit,
    onUpdateReps: (Int) -> Unit,
    onUpdateWeight: (Double) -> Unit
) {
    var reps by remember { mutableStateOf(set.reps.toString()) }
    var weight by remember { mutableStateOf(set.weight.toString()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(
                if (setNumber % 2 == 0) Color(0xFFF5F5F5) else Color.White,
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Set number
        Text(
            text = "$setNumber",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(24.dp) // Slightly narrower to make room for weight
        )

        // Previous - clarify this is from last week
        if (usesWeight) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(50.dp)
            ) {
                Text(
                    text = "${set.previousWeight ?: 0}kg",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = "x${set.previousReps ?: 0}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        } else {
            // If no weight, just show reps
            Text(
                text = "x ${set.previousReps ?: 0}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.width(50.dp)
            )
        }

        // Weight field (if exercise uses weight)
        if (usesWeight) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(55.dp)
            ) {
                BasicTextField(
                    value = weight,
                    onValueChange = {
                        // Allow numbers and decimal point
                        if (it.isEmpty() || it.all { char -> char.isDigit() || char == '.' }) {
                            weight = it
                            onUpdateWeight(it.toDoubleOrNull() ?: 0.0)
                        }
                    },
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "kg",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        // Reps field
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(if (usesWeight) 45.dp else 60.dp)
        ) {
            BasicTextField(
                value = reps,
                onValueChange = {
                    // Only allow numbers
                    if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                        reps = it
                        onUpdateReps(it.toIntOrNull() ?: 0)
                    }
                },
                textStyle = TextStyle(
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Completed check
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(4.dp))
                .border(
                    width = 1.dp,
                    color = if (set.isCompleted) Color.Black else Color.Gray,
                    shape = RoundedCornerShape(4.dp)
                )
                .background(
                    if (set.isCompleted) Color.Black else Color.White
                )
                .clickable { onCompleted(!set.isCompleted) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Complete Set",
                tint = if (set.isCompleted) Color.White else Color.Gray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// Helper function for muscle group icons
private fun getMuscleGroupIcon(muscleGroup: String): Int {
    return when (muscleGroup.lowercase()) {
        "chest" -> R.drawable.chest
        "back" -> R.drawable.back
        "legs" -> R.drawable.leg
        "shoulders" -> R.drawable.shoulders
        "biceps" -> R.drawable.biceps
        "triceps" -> R.drawable.triceps
        "abs" -> R.drawable.core
        "calves" -> R.drawable.calves
        "forearms" -> R.drawable.forearms
        "glutes" -> R.drawable.glutes
        "full body" -> R.drawable.fullbody
        else -> R.drawable.fullbody
    }
}