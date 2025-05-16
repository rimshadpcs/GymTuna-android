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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rimapps.gymlog.R
import com.rimapps.gymlog.domain.model.Exercise
import com.rimapps.gymlog.ui.theme.vag
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoutineScreen(
    routineId: String? = null,  // Add this parameter
    onBack: () -> Unit,
    onRoutineCreated: () -> Unit,
    onAddExercise: () -> Unit,
    viewModel: CreateRoutineViewModel = hiltViewModel()
) {
    var routineName by remember { mutableStateOf("") }
    val selectedExercises by viewModel.selectedExercises.collectAsState()

    LaunchedEffect(routineId) {
        routineId?.let { id ->
            viewModel.initializeRoutine(id)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.routineName.collect { name ->
            routineName = name
        }
    }

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
                    contentColor = Color.White,
                    disabledContainerColor = Color.LightGray,
                    disabledContentColor = Color.White
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

        // Routine title input - IMPROVED TO MATCH SEARCH FIELD
        Spacer(modifier = Modifier.height(16.dp))

        // Styled like your search field with border and clean look
        TextField(
            value = routineName,
            onValueChange = { newValue ->
                routineName = newValue
                viewModel.setRoutineName(newValue) // Call the new function
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .border(1.dp, Color.Black, RoundedCornerShape(12.dp)),
            placeholder = {
                Text(
                    "Routine title",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            singleLine = true,
            textStyle = TextStyle(
                color = Color.Black,
                fontSize = 16.sp,
                fontFamily = vag,
                fontWeight = FontWeight.Normal
            ),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color.Black
            ),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Exercise list or empty state - takes most of the screen space
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (selectedExercises.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
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
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
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
        }

        // Add exercise button - fixed at bottom with proper padding
        Button(
            onClick = onAddExercise,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            border = BorderStroke(1.dp, Color.Black),
            shape = RoundedCornerShape(32.dp) // More rounded corners for pill shape
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Exercise",
                    modifier = Modifier.size(16.dp)
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