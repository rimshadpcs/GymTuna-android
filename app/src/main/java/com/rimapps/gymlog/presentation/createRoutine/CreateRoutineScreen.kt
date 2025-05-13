package com.rimapps.gymlog.presentation.routine

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rimapps.gymlog.R
import com.rimapps.gymlog.domain.model.Exercise
import com.rimapps.gymlog.ui.theme.vag

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoutineScreen(
    onBack: () -> Unit,
    onRoutineCreated: () -> Unit,
    onAddExercise: () -> Unit,
    viewModel: CreateRoutineViewModel = hiltViewModel()
) {
    var routineName by remember { mutableStateOf("") }
    val selectedExercises by viewModel.selectedExercises.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
    ) {
        // Top bar with Cancel and Save buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text(
                    "Cancel",
                    color = Color.Black,
                    fontFamily = vag,
                    fontWeight = FontWeight.Normal
                )
            }

            Text(
                text = "Create Routine",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Black,
                fontFamily = vag,
                fontWeight = FontWeight.Medium
            )

            Button(
                onClick = {
                    viewModel.saveRoutine(routineName)
                    onRoutineCreated()
                },
                enabled = routineName.isNotBlank() && selectedExercises.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "Save",
                    fontFamily = vag,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }


        // Routine title input
        TextField(
            value = routineName,
            onValueChange = { routineName = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, Color.Black, RoundedCornerShape(12.dp)),
            placeholder = { Text("Routine title") },
            singleLine = true,
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // Exercise list or empty state
        if (selectedExercises.isEmpty()) {
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
                        text = "Get started by adding an exercise to your routine.",
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
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(selectedExercises) { exercise ->
                    SelectedExerciseItem(
                        exercise = exercise,
                        onRemove = { viewModel.removeExercise(exercise) }
                    )
                }
            }
        }

        // Add exercise button
        Button(
            onClick = onAddExercise,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.Black)
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
                    text = "Add exercise",
                    fontFamily = vag,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun SelectedExerciseItem(
    exercise: Exercise,
    onRemove: () -> Unit
) {
    Card(
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                    fontFamily = vag,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${exercise.muscleGroup} • ${exercise.equipment}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove exercise",
                    tint = Color.Black
                )
            }
        }
    }
}