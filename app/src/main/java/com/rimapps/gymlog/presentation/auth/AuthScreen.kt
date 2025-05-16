package com.rimapps.gymlog.presentation.auth

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.rimapps.gymlog.R
import com.rimapps.gymlog.domain.model.AuthState
import com.rimapps.gymlog.ui.theme.vag
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onGoogleSignInClick: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Debug log for state changes
    LaunchedEffect(authState) {
        Log.d("AuthScreen", "Auth state changed to: $authState")
    }

    // Collect navigation events
    LaunchedEffect(Unit) {
        Log.d("AuthScreen", "Starting navigation event collection")
        viewModel.navigationEvent.collect {
            Log.d("AuthScreen", "Navigation event received, navigating to home")
            onNavigateToHome()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.gymlogauthpage),
            contentDescription = "App Logo",
            modifier = Modifier.size(360.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "GYM LOG",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontFamily = vag
        )

        Spacer(modifier = Modifier.height(48.dp))

        when (authState) {
            is AuthState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(56.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            is AuthState.Error -> {
                val errorMessage = (authState as AuthState.Error).message
                if (!errorMessage?.contains("cancelled", ignoreCase = true)!!) {
                    LaunchedEffect(errorMessage) {
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
                SignInButton(onGoogleSignInClick)
            }
            else -> {
                SignInButton(onGoogleSignInClick)
            }
        }
    }
}

@Composable
private fun SignInButton(onClick: () -> Unit) {
    Button(
        onClick = {
            Log.d("AuthScreen", "Sign in button clicked")
            onClick()
        },
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
                painter = painterResource(id = R.drawable.google_ic),
                contentDescription = "Google Icon",
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Sign in with Google",
                fontFamily = vag,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}