package com.rimapps.gymlog.presentation.settings

import SettingsItem
import SettingsSection
import ThemeSelector
import WeightUnitDialog
import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rimapps.gymlog.R
import com.rimapps.gymlog.ui.theme.vag

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    var showEditProfileDialog by remember { mutableStateOf(false) }
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    var showWeightUnitDialog by remember { mutableStateOf(false) }
    val currentTheme by viewModel.currentTheme.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = vag,
                fontWeight = FontWeight.SemiBold
            )
            Box(modifier = Modifier.size(48.dp))
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Profile Section
            item {
                SettingsSection(title = "Profile") {
                    SettingsItem(
                        icon = R.drawable.profilepic,
                        title = userProfile.displayName ?: "Set Name",
                        subtitle = userProfile.email ?: "",
                        onClick = { showEditProfileDialog = true }
                    )
                }
            }

            // Subscription Button
            item {
                Button(
                    onClick = { /* Navigate to subscription management */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    border = BorderStroke(1.dp, Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.subscription),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Manage Subscription",
                                fontFamily = vag,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Free Plan",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Weight Unit Button
            item {
                Button(
                    onClick = { showWeightUnitDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    border = BorderStroke(1.dp, Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.weight),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Weight Unit",
                                fontFamily = vag,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = viewModel.weightUnit.value.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Theme Section
            item {
                SettingsSection(title = "Theme") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        ThemeSelector(
                            currentTheme = currentTheme,
                            onThemeSelected = { theme ->
                                viewModel.updateTheme(theme)
                            }
                        )
                    }
                }
            }

            // App Info Section
            item {
                SettingsSection(title = "Info & Legal") {
                    SettingsItem(
                        icon = R.drawable.info,
                        title = "About",
                        subtitle = "1.0.0",
                        onClick = { }
                    )
                    SettingsItem(
                        icon = R.drawable.privacy,
                        title = "Privacy Policy",
                        onClick = { /* Open privacy policy */ }
                    )
                    SettingsItem(
                        icon = R.drawable.terms,
                        title = "Terms of Service",
                        onClick = { /* Open terms */ }
                    )
                    SettingsItem(
                        icon = R.drawable.share,
                        title = "Share App",
                        onClick = { /* Share app link */ }
                    )
                }
            }

            // Sign Out Button
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onSignOut,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Red
                    ),
                    border = BorderStroke(1.dp, Color.Red),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Sign Out",
                        fontFamily = vag,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }

    if (showWeightUnitDialog) {
        WeightUnitDialog(
            currentUnit = viewModel.weightUnit.value,
            onDismiss = { showWeightUnitDialog = false },
            onUnitSelected = { unit ->
                viewModel.updateWeightUnit(unit)
                showWeightUnitDialog = false
            }
        )
    }
}