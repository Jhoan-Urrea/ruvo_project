package com.example.ruvo_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ruvo_app.core.navigation.AppNavGraph
import com.example.ruvo_app.core.theme.Ruvo_appTheme
import com.example.ruvo_app.data.local.UserPreferencesManager
import com.example.ruvo_app.data.repository.AuthRepositoryImpl
import com.example.ruvo_app.data.repository.UserRepositoryImpl
import com.example.ruvo_app.domain.usecase.*
import com.example.ruvo_app.features.auth.AuthUiState
import com.example.ruvo_app.features.auth.AuthViewModel
import com.example.ruvo_app.features.auth.SplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Manual DI for now (Replace with Hilt/Koin later)
        val userRepository = UserRepositoryImpl()
        val authRepository = AuthRepositoryImpl(userRepository = userRepository)
        val userPreferencesManager = UserPreferencesManager(this)
        
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(
                    loginUseCase = LoginUseCase(authRepository),
                    registerUseCase = RegisterUseCase(authRepository),
                    resetPasswordUseCase = ResetPasswordUseCase(authRepository),
                    signOutUseCase = SignOutUseCase(authRepository),
                    getCurrentUserUseCase = GetCurrentUserUseCase(authRepository),
                    validateEmailUseCase = ValidateEmailUseCase(),
                    validatePasswordUseCase = ValidatePasswordUseCase(),
                    validateUsernameUseCase = ValidateUsernameUseCase(),
                    userRepository = userRepository,
                    userPreferencesManager = userPreferencesManager
                ) as T
            }
        }

        setContent {
            Ruvo_appTheme {
                val authViewModel: AuthViewModel = viewModel(factory = factory)
                val authState by authViewModel.uiState.collectAsState()

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