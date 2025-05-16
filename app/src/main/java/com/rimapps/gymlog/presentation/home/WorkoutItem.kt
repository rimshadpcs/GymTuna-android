package com.rimapps.gymlog.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rimapps.gymlog.domain.model.Exercise
import com.rimapps.gymlog.domain.model.ExerciseSet
import com.rimapps.gymlog.domain.model.Workout
import com.rimapps.gymlog.presentation.createRoutine.RoutineOptionsBottomSheet
import com.rimapps.gymlog.ui.theme.GymLogTheme
import com.rimapps.gymlog.ui.theme.vag
@Composable
fun WorkoutItem(
    workout: Workout,
    onStartClick: (String) -> Unit,
    onDuplicate: (Workout) -> Unit,
    onEdit: (Workout) -> Unit,
    onDelete: (Workout) -> Unit
) {
    var showOptions by remember { mutableStateOf(false) }

    // Adjustable dimensions - modify these values to change the card size
    val cardHeight = 92.dp        // Increase this for taller cards
    val cardCornerRadius = 12.dp  // Decrease this for less rounded corners
    val horizontalPadding = 12.dp // Adjust this for card width (smaller value = wider card)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight)
            .padding(horizontal = 0.dp), // No horizontal padding here to allow full width
        shape = RoundedCornerShape(cardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.5.dp, Color.Black)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
            ) {
                Text(
                    text = workout.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = vag,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = "${workout.exercises.size} exercises",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Button(
                    onClick = { onStartClick(workout.id) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp)
                ) {
                    Text(
                        "Start",
                        fontFamily = vag,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(
                    onClick = { showOptions = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = Color.Black
                    )
                }
            }
        }
    }

    if (showOptions) {
        RoutineOptionsBottomSheet(
            onDismiss = { showOptions = false },
            onDuplicate = { onDuplicate(workout) },
            onEdit = { onEdit(workout) },
            onDelete = { onDelete(workout) },
            routineName = workout.name
        )
    }
}