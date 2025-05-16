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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rimapps.gymlog.ui.theme.vag


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinishWorkoutDialog(
    onDismiss: () -> Unit,
    onConfirm: (String?, Boolean) -> Unit, // Modified to include routine name and save as routine flag
    isLoading: Boolean
) {
    var saveAsRoutine by remember { mutableStateOf(false) }
    var routineName by remember { mutableStateOf("") }

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
            Column {
                if (isLoading) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Saving workout...")
                    }
                } else {
                    Text(
                        "Would you like to save this workout?",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Checkbox to save as routine
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = saveAsRoutine,
                            onCheckedChange = { saveAsRoutine = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color.Black,
                                uncheckedColor = Color.Gray
                            )
                        )
                        Text(
                            "Save as routine",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    // Routine name input field (only shown when checkbox is checked)
                    if (saveAsRoutine) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = routineName,
                            onValueChange = { routineName = it },
                            placeholder = { Text("Enter routine name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = TextFieldDefaults.textFieldColors(
                                containerColor = Color.White,
                                focusedIndicatorColor = Color.Black,
                                unfocusedIndicatorColor = Color.Gray
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        if (saveAsRoutine) routineName.takeIf { it.isNotBlank() } else null,
                        saveAsRoutine
                    )
                },
                enabled = !isLoading && (!saveAsRoutine || routineName.isNotBlank()),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White,
                    disabledContainerColor = Color.Gray
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
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isLoading,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Black
                )
            ) {
                Text(
                    "Cancel",
                    fontFamily = vag,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}