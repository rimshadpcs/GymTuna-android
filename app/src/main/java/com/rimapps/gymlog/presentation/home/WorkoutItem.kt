package com.rimapps.gymlog.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.rimapps.gymlog.ui.theme.GymLogTheme
import com.rimapps.gymlog.ui.theme.vag


@Composable
fun WorkoutItem(
    workout: Workout,
    onStartClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Black, RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = workout.name,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black,
                fontWeight = FontWeight.SemiBold,
                fontFamily = vag
            )
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = Color.Black
                )
            }
        }

        Text(
            text = workout.exercises.joinToString(", ") { it.name },
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onStartClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                "Start Routine",
                fontFamily = vag,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
//@Preview(showBackground = true)
//@Composable
//fun WorkoutItemPreview() {
//    GymLogTheme {
//        val sampleWorkout = remember {
//            Workout(
//                id = "1",
//                name = "Shoulder+leg A",
//                exercises = listOf(
//                    Exercise(
//                        id = "1",
//                        name = "Seated Overhead Press (Barbell)",
//                        equipment = "Barbell",
//                        primaryMuscle = "Shoulders",
//                        sets = listOf(
//                            ExerciseSet(1, 30.0, 12),
//                            ExerciseSet(2, 30.0, 12),
//                            ExerciseSet(3, 30.0, 12)
//                        )
//                    ),
//                    Exercise(
//                        id = "2",
//                        name = "Lateral Raise (Dumbbell)",
//                        equipment = "Dumbbell",
//                        primaryMuscle = "Shoulders",
//                        sets = listOf(
//                            ExerciseSet(1, 20.0, 15),
//                            ExerciseSet(2, 24.0, 12),
//                            ExerciseSet(3, 20.0, 12)
//                        )
//                    )
//                )
//            )
//        }
//        WorkoutItem(
//            workout = sampleWorkout,
//            onStartClick = {}
//        )
//    }
//}
