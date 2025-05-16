import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rimapps.gymlog.presentation.settings.WeightUnit
import com.rimapps.gymlog.ui.theme.vag

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Color.Black,
            fontFamily = vag,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            border =
                BorderStroke(1.dp, Color.Black)
        ) {
            content()
        }
    }
}

@Composable
fun SettingsItem(
    @DrawableRes icon: Int,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    endContent: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = vag,
                    fontWeight = FontWeight.Medium
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }
        endContent?.invoke() ?: Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = "Navigate",
            tint = Color.Gray
        )
    }
}
//
//@Composable
//fun EditProfileDialog(
//    currentName: String,
//    onDismiss: () -> Unit,
//    onSave: (String) -> Unit
//) {
//    var name by remember { mutableStateOf(currentName) }
//
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = {
//            Text(
//                "Edit Profile",
//                fontFamily = vag,
//                fontWeight = FontWeight.SemiBold
//            )
//        },
//        text = {
//            TextField(
//                value = name,
//                onValueChange = { name = it },
//                label = { Text("Name") },
//                singleLine = true
//            )
//        },
//        confirmButton = {
//            Button(
//                onClick = { onSave(name) },
//                enabled = name.isNotBlank()
//            ) {
//                Text("Save")
//            }
//        },
//        dismissButton = {
//            TextButton(onClick = onDismiss) {
//                Text("Cancel")
//            }
//        }
//    )
//}

@Composable
fun WeightUnitDialog(
    currentUnit: WeightUnit,
    onDismiss: () -> Unit,
    onUnitSelected: (WeightUnit) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Weight Unit",
                fontFamily = vag,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column {
                WeightUnit.values().forEach { unit ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onUnitSelected(unit) }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(unit.displayName)
                        if (unit == currentUnit) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color.Black
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {}
    )
}
enum class AppTheme {
    LIGHT,
    NEUTRAL,
    DARK
}

@Composable
fun ThemeSelector(
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Light Theme Button
            Button(
                onClick = { onThemeSelected(AppTheme.LIGHT) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                border = BorderStroke(
                    width = if (currentTheme == AppTheme.LIGHT) 2.dp else 1.dp,
                    color = if (currentTheme == AppTheme.LIGHT) Color.Black else Color.Gray
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Light",
                    fontFamily = vag,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }

            // Neutral Theme Button
            Button(
                onClick = { onThemeSelected(AppTheme.NEUTRAL) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF6F1EB), // Sand color
                    contentColor = Color.Black
                ),
                border = BorderStroke(
                    width = if (currentTheme == AppTheme.NEUTRAL) 2.dp else 1.dp,
                    color = if (currentTheme == AppTheme.NEUTRAL) Color.Black else Color.Gray
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Neutral",
                    fontFamily = vag,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }

            // Dark Theme Button
            Button(
                onClick = { onThemeSelected(AppTheme.DARK) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                border = BorderStroke(
                    width = if (currentTheme == AppTheme.DARK) 2.dp else 1.dp,
                    color = if (currentTheme == AppTheme.DARK) Color.Black else Color.Gray
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Dark",
                    fontFamily = vag,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }
}