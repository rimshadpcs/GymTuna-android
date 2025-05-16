package com.rimapps.gymlog.presentation.createRoutine


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.rimapps.gymlog.R
import com.rimapps.gymlog.ui.theme.vag
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineOptionsBottomSheet(
    onDismiss: () -> Unit,
    onDuplicate: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    routineName: String
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
                text = routineName,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = vag,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Duplicate Button
            Button(
                onClick = {
                    onDuplicate()
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
                        painter = painterResource(id = R.drawable.copy),
                        contentDescription = "Duplicate",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "Duplicate",
                        fontFamily = vag,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Edit Button
            Button(
                onClick = {
                    onEdit()
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
                        painter = painterResource(id = R.drawable.edit),
                        contentDescription = "Edit",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "Edit",
                        fontFamily = vag,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Delete Button
            Button(
                onClick = {
                    onDelete()
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
                        contentDescription = "Delete",
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "Delete",
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