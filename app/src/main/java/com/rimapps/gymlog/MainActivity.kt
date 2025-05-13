package com.rimapps.gymlog

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.rimapps.gymlog.presentation.auth.AuthViewModel
import com.rimapps.gymlog.presentation.navigation.NavGraph
import com.rimapps.gymlog.ui.theme.GymLogTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: AuthViewModel by viewModels()

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        val resultCode = result.resultCode
        val data = result.data

        Log.d("MainActivity", "Sign in result code: $resultCode")

        when (resultCode) {
            RESULT_OK -> {
                if (data != null) {
                    Log.d("MainActivity", "Got sign in data, processing...")
                    viewModel.signInWithGoogleIntent(data)
                } else {
                    Log.e("MainActivity", "Sign in failed: No data received")
                    Toast.makeText(
                        this,
                        "Sign in failed: No data received",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            RESULT_CANCELED -> {
                Log.e("MainActivity", "Sign in was cancelled")
                Toast.makeText(
                    this,
                    "Sign in was cancelled",
                    Toast.LENGTH_LONG
                ).show()
            }
            else -> {
                Log.e("MainActivity", "Sign in failed with code: $resultCode")
                Toast.makeText(
                    this,
                    "Sign in failed",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
         val authViewModel: AuthViewModel by viewModels()
        setContent {
            GymLogTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph(
                        authViewModel = authViewModel,
                        onGoogleSignInClick = {
                            lifecycleScope.launch {
                                try {
                                    Log.d("MainActivity", "Starting Google Sign In")
                                    val intentSender = viewModel.startGoogleSignIn()
                                    if (intentSender != null) {
                                        Log.d("MainActivity", "Launching sign in intent")
                                        googleSignInLauncher.launch(
                                            IntentSenderRequest.Builder(intentSender).build()
                                        )
                                    } else {
                                        Log.e("MainActivity", "No intent sender received")
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Couldn't start Google Sign-In",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                } catch (e: Exception) {
                                    Log.e("MainActivity", "Error launching sign in", e)
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Error: ${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}