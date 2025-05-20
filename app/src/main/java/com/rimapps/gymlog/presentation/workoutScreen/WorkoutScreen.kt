package com.rimapps.gymlog.presentation.workoutScreen

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rimapps.gymlog.R
import com.rimapps.gymlog.domain.model.*
import com.rimapps.gymlog.presentation.workoutScreen.FinishWorkoutDialog
import com.rimapps.gymlog.ui.theme.Black
import com.rimapps.gymlog.ui.theme.vag

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    routineId: String? = null,
    routineName: String? = null,
    onBack: () -> Unit,
    onFinish: () -> Unit,
    onAddExercise: () -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val initialized = remember { mutableStateOf(false) }
    val workoutDuration by viewModel.workoutDuration.collectAsStateWithLifecycle()
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()
    val totalVolume by viewModel.totalVolume.collectAsStateWithLifecycle()
    val totalSets by viewModel.totalSets.collectAsStateWithLifecycle()
    var showErrorDialog by remember { mutableStateOf<String?>(null) }
    val showUpdateRoutineDialog by viewModel.showUpdateRoutineDialog.collectAsStateWithLifecycle()

    var showFinishDialog by remember { mutableStateOf(false) }
    val workoutState by viewModel.workoutState.collectAsStateWithLifecycle()
    val isFinishing = workoutState is ActiveWorkoutState.Loading

    LaunchedEffect(routineId) {
        if (!initialized.value && routineId != null) {
            viewModel.initializeFromRoutine(routineId)
            initialized.value = true
        }
    }
    LaunchedEffect(workoutState) {
        if (workoutState is ActiveWorkoutState.Success) {
            onFinish()
        }
    }
    LaunchedEffect(workoutState) {
        when (workoutState) {
            is ActiveWorkoutState.Success -> {
                showFinishDialog = false
                onFinish()
            }
            is ActiveWorkoutState.Error -> showErrorDialog = (workoutState as ActiveWorkoutState.Error).message
            else -> {}
        }
    }

    /* ---------- Error dialog ---------- */
    showErrorDialog?.let { errorMessage ->
        AlertDialog(
            onDismissRequest = { showErrorDialog = null },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = { Button(onClick = { showErrorDialog = null }) { Text("OK") } }
        )
    }

    /* ---------- Finish dialog ---------- */
    if (showFinishDialog) {
        FinishWorkoutDialog(
            onDismiss = { showFinishDialog = false },
            onConfirm = {
                viewModel.finishWorkout {
                    showFinishDialog = false
                    onFinish()
                }
            },
            isLoading = isFinishing
        )
    }


    if (showUpdateRoutineDialog) {
        UpdateRoutineDialog(
            onDismiss = {
                viewModel.dismissUpdateDialog()
                showFinishDialog = false
                onFinish()
            },
            onConfirm = {
                viewModel.updateRoutine {
                    viewModel.dismissUpdateDialog()
                    showFinishDialog = false
                    onFinish()
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
    ) {
        /* ---- Top bar ---- */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.Black)
                }
                Text(
                    text = routineName ?: "Quick Workout",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black,
                    fontFamily = vag,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    workoutDuration,
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = vag,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.width(16.dp))
                Button(
                    onClick = { showFinishDialog = true },
                    enabled = !isFinishing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isFinishing) {
                        CircularProgressIndicator(Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Finish", fontFamily = vag, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        /* ---- Stats ---- */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Volume", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text(
                    "$totalVolume kg",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = vag,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Column {
                Text("Sets", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text(
                    "$totalSets",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = vag,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        /* ---- Exercise list or empty state ---- */
        if (exercises.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painterResource(id = R.drawable.gym),
                        "Empty state",
                        modifier = Modifier.size(48.dp),
                        tint = Color.Gray
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Get started",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.Black,
                        fontFamily = vag,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Add an exercise to start your workout",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(exercises) { exercise ->
                    ExerciseItem(
                        workoutExercise = exercise,
                        onAddSet = { viewModel.addSet(exercise) },
                        onSetCompleted = { set, isCompleted ->
                            viewModel.setCompleted(exercise, set, isCompleted)
                        },
                        onUpdateReps = { set, reps ->
                            viewModel.updateReps(exercise, set, reps)
                        },
                        onUpdateWeight = { set, weight ->
                            viewModel.updateWeight(exercise, set, weight)
                        },
                        onUpdateNotes = { notes -> viewModel.updateNotes(exercise, notes) },
                        onDeleteSet = { ex, setNumber -> viewModel.deleteSet(ex, setNumber) },
                        onArrangeExercise = { },
                        onReplaceExercise = { },
                        onAddToSuperset = { },
                        onRemoveExercise = { }
                    )
                }
            }
        }

        /* ---- Bottom buttons ---- */
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
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(Icons.Default.Add, "Add")
                Spacer(Modifier.width(8.dp))
                Text("Add Exercise", fontFamily = vag, fontWeight = FontWeight.SemiBold)
            }
        }

        Button(
            onClick = {
                viewModel.discardWorkout()
                onBack()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Red
            ),
            border = BorderStroke(1.dp, Color.Red),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Discard Workout", fontFamily = vag, fontWeight = FontWeight.SemiBold)
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
    onUpdateNotes: (String) -> Unit,
    onDeleteSet: (WorkoutExercise, Int) -> Unit,
    onArrangeExercise: (WorkoutExercise) -> Unit,
    onReplaceExercise: (WorkoutExercise) -> Unit,
    onAddToSuperset: (WorkoutExercise) -> Unit,
    onRemoveExercise: (WorkoutExercise) -> Unit
) {
    var notes by remember { mutableStateOf(workoutExercise.notes) }
    var showOptions by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
        /* ---- Header ---- */
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color.Black, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painterResource(id = getMuscleGroupIcon(workoutExercise.exercise.muscleGroup)),
                        "Muscle group",
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    workoutExercise.exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                    fontFamily = vag,
                    fontWeight = FontWeight.SemiBold
                )
            }
            IconButton(onClick = { showOptions = true }) {
                Icon(Icons.Default.MoreVert, "More", tint = Color.Black)
            }
        }

        /* ---- Notes ---- */
        TextField(
            value = notes,
            onValueChange = {
                notes = it
                onUpdateNotes(it)
            },
            placeholder = { Text("Add notes here...", color = Color.Gray, fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            textStyle = TextStyle(fontSize = 12.sp),
            singleLine = true
        )

        /* ---- Rest timer button ---- */
        Button(
            onClick = { /* TODO */ },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.Black
            ),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.height(28.dp)
        ) {
            Icon(
                painterResource(id = R.drawable.timer),
                "Rest",
                tint = Color.Black,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                "Rest Timer: OFF",
                fontFamily = vag,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            )
        }

        /* ---- Sets header ---- */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding( 16.dp, 16.dp,8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            // Set column - match exact position of values in SetRow
            Text(
                "SET", 
                style = MaterialTheme.typography.bodyMedium, 
                color = Color.Gray,
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.Center
            )
            
            // Last column
            Text(
                "LAST", 
                style = MaterialTheme.typography.bodyMedium, 
                color = Color.Gray,
                modifier = Modifier.width(56.dp),
                textAlign = TextAlign.Center
            )
            
            // Best column
            Text(
                "BEST", 
                style = MaterialTheme.typography.bodyMedium, 
                color = Color.Gray,
                modifier = Modifier.width(56.dp),
                textAlign = TextAlign.Center
            )
            
            // KG column
            if (workoutExercise.exercise.usesWeight) {
                Text(
                    "KG", 
                    style = MaterialTheme.typography.bodyMedium, 
                    color = Color.Gray,
                    modifier = Modifier.width(64.dp),
                    textAlign = TextAlign.Center
                )
            }
            
            // Reps column
            Text(
                "REPS", 
                style = MaterialTheme.typography.bodyMedium, 
                color = Color.Gray,
                modifier = Modifier.width(64.dp),
                textAlign = TextAlign.Center
            )
            
            // Space for checkbox - this aligns with the Spacer in SetRow
            Spacer(Modifier.weight(1f))
        }

        /* ---- Sets list ---- */
        workoutExercise.sets.forEach { set ->
            SetRow(
                setNumber = set.setNumber,
                set = set,
                usesWeight = workoutExercise.exercise.usesWeight,
                onCompleted = { isCompleted -> onSetCompleted(set, isCompleted) },
                onUpdateReps = { reps -> onUpdateReps(set, reps) },
                onUpdateWeight = { weight -> onUpdateWeight(set, weight) },
                onDelete = { onDeleteSet(workoutExercise, set.setNumber) }
            )
        }

        /* ---- Add set button ---- */
        OutlinedButton(
            onClick = onAddSet,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            border = BorderStroke(1.dp, Color.Gray),
            shape = RoundedCornerShape(32.dp)
        ) {
            Icon(Icons.Default.Add, "Add", modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Add Set", fontFamily = vag, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
    }
}

private fun getMuscleGroupIcon(muscleGroup: String): Int =
    when (muscleGroup.lowercase()) {
        "chest" -> R.drawable.chest
        "back", "middle back", "lower back" -> R.drawable.back
        "legs", "hamstrings", "quadriceps", "adductors", "hip flexors" -> R.drawable.leg
        "shoulders", "traps", "neck" -> R.drawable.shoulders
        "biceps", "arms" -> R.drawable.biceps
        "triceps" -> R.drawable.triceps
        "core", "abs", "abdominals", "obliques" -> R.drawable.core
        "calves" -> R.drawable.calves
        "forearms" -> R.drawable.forearms
        "glutes" -> R.drawable.glutes
        "full body" -> R.drawable.fullbody
        else -> R.drawable.fullbody
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetRow(
    setNumber: Int,
    set: ExerciseSet,
    usesWeight: Boolean,
    onCompleted: (Boolean) -> Unit,
    onUpdateReps: (Int) -> Unit,
    onUpdateWeight: (Double) -> Unit,
    onDelete: () -> Unit
) {
    // Initialize with empty string to show placeholder if weight is 0
    var reps by remember { 
        mutableStateOf(if (set.reps > 0) set.reps.toString() else "") 
    }
    var weight by remember { 
        mutableStateOf(if (set.weight > 0.0) set.weight.toString() else "") 
    }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp)  // Add small padding to prevent edge visibility
                    .clip(RoundedCornerShape(8.dp))  // Match the row's corner radius
                    .background(Color.Black),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Default.Delete, "Delete", tint = Color.White)
            }
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .height(48.dp)
                .background(
                    if (setNumber % 2 == 0) Color(0xFFF5F5F5) else Color.White,
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Set number
            Text(
                "$setNumber",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(40.dp)
            )

            // Previous workout
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.width(56.dp)
            ) {
                // Always show the LAST column, with direct value checking
                val prevText = if (set.previousWeight != null && set.previousReps != null) {
                    "${set.previousWeight}×${set.previousReps}"
                } else "-"
                
                Text(
                    text = prevText,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }

            // Best set
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.width(56.dp)
            ) {
                // Always show the BEST column, with direct value checking
                val bestText = if (set.bestWeight != null && set.bestReps != null) {
                    "${set.bestWeight}×${set.bestReps}"
                } else "-"
                
                Text(
                    text = bestText,
                    fontSize = 12.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            // Weight input with numerical keyboard
            if (usesWeight) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .width(64.dp)
                        .background(Color(0xFFF9F9F9), RoundedCornerShape(4.dp))
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(4.dp))
                ) {
                    // Use placeholder text (hint) from previous workout if available
                    val placeholder = set.previousWeight?.toString() ?: ""
                    
                    BasicTextField(
                        value = weight,
                        onValueChange = {
                            if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                weight = it
                                it.toDoubleOrNull()?.let { value -> onUpdateWeight(value) }
                            }
                        },
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { /* Close keyboard */ }
                        ),
                        decorationBox = { innerTextField ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                            ) {
                                if (weight.isEmpty()) {
                                    Text(
                                        text = placeholder,
                                        fontSize = 14.sp,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                innerTextField()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Reps input with numerical keyboard
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(64.dp)
                    .background(Color(0xFFF9F9F9), RoundedCornerShape(4.dp))
                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(4.dp))
            ) {
                // Use placeholder text (hint) from previous workout if available
                val placeholder = set.previousReps?.toString() ?: ""
                
                BasicTextField(
                    value = reps,
                    onValueChange = {
                        if (it.isEmpty() || it.all { c -> c.isDigit() }) {
                            reps = it
                            it.toIntOrNull()?.let { value -> onUpdateReps(value) }
                        }
                    },
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { /* Close keyboard */ }
                    ),
                    decorationBox = { innerTextField ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            if (reps.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                            innerTextField()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Space between reps and checkbox
            Spacer(Modifier.weight(1f))

            // Completion checkbox
            Box(
                modifier = Modifier.size(32.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .border(1.dp, if (set.isCompleted) Color.Black else Color.Gray, RoundedCornerShape(4.dp))
                    .background(if (set.isCompleted) Color.Black else Color.White)
                    .clickable { onCompleted(!set.isCompleted) },
                contentAlignment = Alignment.Center
            ) {
                if (set.isCompleted) {
                    Icon(
                        Icons.Default.Check,
                        "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
@Composable
fun UpdateRoutineDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Update Routine",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = vag,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                "Would you like to update the original routine with these changes?",
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Text(
                    "Update Routine",
                    fontFamily = vag,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Keep Original",
                    fontFamily = vag,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }
        }
    )
}