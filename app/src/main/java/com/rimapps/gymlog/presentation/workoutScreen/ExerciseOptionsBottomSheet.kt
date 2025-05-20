package com.rimapps.gymlog.presentation.workoutScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rimapps.gymlog.R
import com.rimapps.gymlog.ui.theme.vag

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseOptionsBottomSheet(
    onDismiss: () -> Unit,
    onReArrange: () -> Unit,
    onReplace: () -> Unit,
    onAddToSuperset: () -> Unit,
    onRemove: () -> Unit,
    exerciseName: String
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title
            Text(
                text = exerciseName,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = vag,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Reorder Button
            Button(
                onClick = {
                    onReArrange()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.reorder),
                        contentDescription = "ReArrange",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "Re arrange Exercise",
                        fontFamily = vag,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Replace Button
            Button(
                onClick = {
                    onReplace()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.replace),
                        contentDescription = "Replace",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "Replace Exercise",
                        fontFamily = vag,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Add to Superset Button
            Button(
                onClick = {
                    onAddToSuperset()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.superset),
                        contentDescription = "Superset",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "Add to Superset",
                        fontFamily = vag,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Remove Button
            Button(
                onClick = {
                    onRemove()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.trash),
                        contentDescription = "Remove",
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "Remove Exercise",
                        fontFamily = vag,
                        fontWeight = FontWeight.Medium,
                        color = Color.Red
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}