package com.example.ruvo_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ruvo_app.core.navigation.AppNavGraph
import com.example.ruvo_app.core.theme.Ruvo_appTheme
import com.example.ruvo_app.features.auth.AuthUiState
import com.example.ruvo_app.features.auth.AuthViewModel
import com.example.ruvo_app.features.auth.SplashScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Habilita el modo de pantalla completa real (Edge-to-Edge)
        enableEdgeToEdge()
        
        setContent {
            Ruvo_appTheme {
                val authViewModel: AuthViewModel = viewModel()
                val authState by authViewModel.uiState.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }

                // Listener global para notificaciones en tiempo real
                LaunchedEffect(Unit) {
                    authViewModel.newNotificationEvent.collectLatest { notification ->
                        snackbarHostState.showSnackbar(
                            message = notification.message,
                            withDismissAction = true
                        )
                    }
                }

                // Usamos contentWindowInsets = WindowInsets(0.dp) para evitar que el Scaffold 
                // principal "empuje" el contenido hacia abajo automáticamente.
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    contentWindowInsets = WindowInsets(0.dp) 
                ) { _ ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (authState) {
                            is AuthUiState.Loading -> {
                                SplashScreen()
                            }
                            else -> {
                                AppNavGraph(authViewModel = authViewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
