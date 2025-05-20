package com.rimapps.gymlog.presentation.workoutScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rimapps.gymlog.ui.theme.vag

@Composable
fun FinishWorkoutDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isLoading: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Finish Workout",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = vag,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            if (isLoading) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Saving workout...")
                }
            } else {
                Text(
                    "Are you sure you want to finish this workout?\n" +
                            "This will save your progress and end the session.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Text(
                    "Finish",
                    fontFamily = vag,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text(
                    "Cancel",
                    fontFamily = vag,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }
        }
    )
}