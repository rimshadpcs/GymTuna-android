package com.rimapps.gymlog.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rimapps.gymlog.R
import com.rimapps.gymlog.domain.model.Workout
import com.rimapps.gymlog.domain.model.WorkoutState
import com.rimapps.gymlog.ui.theme.vag

@Composable
fun HomeScreen(
    onSignOut: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    onStartWorkout: (String?) -> Unit,
    onNavigateToSearch: () -> Unit // New parameter
) {
    val workoutState by viewModel.workoutState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
            .systemBarsPadding()
    ) {
        TopBar()
        QuickStartSection(
            onStartWorkout = onStartWorkout,
            onSearchExercises = onNavigateToSearch
        )

        // Update RoutinesSection
        RoutinesSection(
            onNewRoutine = { onStartWorkout(null) },
            onStartWorkout = onStartWorkout // Pass the onStartWorkout function
        )
        WorkoutsList(workoutState, onStartWorkout)
    }
}
@Composable
fun TopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Image(
                painter = painterResource(id = R.drawable.gymloglogo),
                contentDescription = "App Logo",
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "GYM LOG",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = vag,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.padding(end = 4.dp)
        ) {
            // Theme Toggle Button
            Box(
                modifier = Modifier
                    .size(36.dp)

                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { /* Toggle theme */ },
                    modifier = Modifier.size(28.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.moon),
                        contentDescription = "User Settings",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Settings Button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .border(
                        width = 2.dp,
                        color = Color.Black,
                        shape = CircleShape
                    )
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { /* Settings */ },
                    modifier = Modifier.size(28.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.usersetting),
                        contentDescription = "User Settings",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
@Composable
fun QuickStartSection(
    onStartWorkout: (String?) -> Unit,
    onSearchExercises: () -> Unit) {
    Text(
        text = "Quick Start",
        style = MaterialTheme.typography.titleLarge,
        color = Color.Black,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(vertical = 16.dp)
    )

    Button(
        onClick = {onStartWorkout(null) },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        border = BorderStroke(1.dp, Color.Black),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Start Empty Workout"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Start Empty Workout",
                fontFamily = vag,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun RoutinesSection(
    onNewRoutine: () -> Unit,
    onStartWorkout: (String?) -> Unit,) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Routines",
            style = MaterialTheme.typography.titleLarge,
            color = Color.Black,
            fontWeight = FontWeight.SemiBold
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = {onStartWorkout(null) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            border = BorderStroke(1.dp, Color.Black),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.gym),
                    contentDescription = "dumbbell",
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "New Routine",
                    fontFamily = vag,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}@Composable
private fun WorkoutsList(
    workoutState: WorkoutState,
    onStartWorkout: (String?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "My Routines",
            style = MaterialTheme.typography.titleLarge,
            color = Color.Black,
            fontWeight = FontWeight.SemiBold
        )
    }

    when (workoutState) {
        is WorkoutState.Loading -> {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.Black)
            }
        }
        is WorkoutState.Success -> {
            if (workoutState.workouts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No saved routines yet. Create one to get started!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(workoutState.workouts) { workout ->
                        WorkoutItem(
                            workout = workout,
                            onStartClick = { onStartWorkout(workout.id) }
                        )
                    }
                }
            }
        }
        is WorkoutState.Error -> {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = workoutState.message,
                    color = Color.Red
                )
            }
        }
        else -> Unit
    }
}